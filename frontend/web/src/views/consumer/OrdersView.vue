<template>
  <div class="page-stack">
    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="订单总数" :value="orderSummary.total" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="已完成订单" :value="orderSummary.completed" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="售后工单" :value="orderSummary.afterSales" />
      </el-card>
    </div>

    <el-card shadow="never">
      <div class="section-title">
        <div>
          <h1>我的订单</h1>
          <p>查看预约进度、提交评价，并在出现问题时发起售后反馈。</p>
        </div>
        <el-button @click="loadData">刷新</el-button>
      </div>

      <el-table :data="orderRows" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单号" width="90" />
        <el-table-column prop="serviceName" label="服务项目" min-width="140" />
        <el-table-column prop="workerName" label="服务人员" width="120" />
        <el-table-column prop="bookingDate" label="预约日期" width="120" />
        <el-table-column prop="bookingSlot" label="时间段" width="140" />
        <el-table-column prop="serviceAddress" label="服务地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusTagType(row.status)">{{ getOrderStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progressNote" label="最新进度" min-width="220" show-overflow-tooltip />
        <el-table-column label="评价" min-width="180">
          <template #default="{ row }">
            <template v-if="row.reviewed">
              <div class="review-summary">
                <el-rate :model-value="row.reviewRating" disabled />
                <span>{{ row.reviewContent }}</span>
              </div>
            </template>
            <span v-else class="muted-line">暂未评价</span>
          </template>
        </el-table-column>
        <el-table-column label="售后情况" min-width="260">
          <template #default="{ row }">
            <template v-if="row.afterSale">
              <div class="stack-cell">
                <el-tag :type="getAfterSaleStatusTagType(row.afterSale.status)">
                  {{ getAfterSaleStatusLabel(row.afterSale.status) }}
                </el-tag>
                <span class="muted-line">{{ row.afterSale.issueType }}</span>
                <AttachmentGallery
                  :attachments="row.afterSale.attachments"
                  compact
                  empty-text="未上传凭证"
                />
                <span v-if="row.afterSale.adminRemark" class="muted-line">
                  处理备注：{{ row.afterSale.adminRemark }}
                </span>
              </div>
            </template>
            <span v-else class="muted-line">未发起售后</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-space wrap>
              <el-button
                v-if="canReview(row)"
                size="small"
                type="primary"
                @click="openReview(row)"
              >
                去评价
              </el-button>
              <el-button
                v-if="canCreateAfterSale(row)"
                size="small"
                plain
                @click="openAfterSale(row)"
              >
                申请售后
              </el-button>
              <span v-if="!canReview(row) && !canCreateAfterSale(row)" class="muted-line">无可用操作</span>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="reviewDialogVisible" title="提交订单评价" width="520px">
      <el-form label-position="top">
        <el-form-item label="服务评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="说说本次服务体验，例如是否准时、是否细致、沟通是否顺畅。"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="afterSaleDialogVisible" title="提交售后反馈" width="620px">
      <el-form label-position="top">
        <el-form-item label="问题类型">
          <el-select v-model="afterSaleForm.issueType" style="width: 100%">
            <el-option label="服务质量不达标" value="服务质量不达标" />
            <el-option label="服务人员迟到或爽约" value="服务人员迟到或爽约" />
            <el-option label="收费或沟通争议" value="收费或沟通争议" />
            <el-option label="物品损坏或安全问题" value="物品损坏或安全问题" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="afterSaleForm.contactPhone" placeholder="便于客服经理回访沟通" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input
            v-model="afterSaleForm.content"
            type="textarea"
            :rows="5"
            maxlength="300"
            show-word-limit
            placeholder="请尽量描述清楚问题经过、影响范围和你的诉求。"
          />
        </el-form-item>
        <el-form-item label="凭证图片">
          <div class="page-stack full-width">
            <div class="hero-actions">
              <el-button plain @click="openAttachmentPicker" :disabled="draftAttachments.length >= maxAttachmentCount">
                选择凭证图片
              </el-button>
              <span class="muted-line">支持 jpg/png/webp，最多 3 张，每张不超过 5MB。</span>
            </div>
            <div v-if="draftAttachments.length" class="attachment-editor-list">
              <div
                v-for="item in draftAttachments"
                :key="item.uid"
                class="attachment-editor-item"
              >
                <el-image
                  :src="item.url"
                  :preview-src-list="draftAttachmentPreviewList"
                  fit="cover"
                  preview-teleported
                  class="attachment-thumb"
                />
                <div class="attachment-meta">
                  <span>{{ item.name }}</span>
                  <span class="muted-line">{{ formatFileSize(item.size) }}</span>
                </div>
                <el-button link type="danger" @click="removeDraftAttachment(item.uid)">移除</el-button>
              </div>
            </div>
            <span v-else class="muted-line">未选择图片，平台仍然会先创建售后单。</span>
          </div>
        </el-form-item>
      </el-form>
      <input
        ref="attachmentInputRef"
        type="file"
        accept="image/*"
        multiple
        class="visually-hidden-input"
        @change="handleAttachmentInputChange"
      />
      <template #footer>
        <el-button @click="closeAfterSaleDialog">取消</el-button>
        <el-button type="primary" :loading="submittingAfterSale" @click="submitAfterSale">
          提交售后
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AttachmentGallery from '../../components/common/AttachmentGallery.vue'
import {
  createAfterSale,
  fetchMyAfterSales,
  fetchOrders,
  submitOrderReview,
  uploadAfterSaleAttachment
} from '../../api'
import {
  getAfterSaleStatusLabel,
  getAfterSaleStatusTagType
} from '../../utils/afterSale'
import {
  getOrderStatusLabel,
  getOrderStatusTagType,
  normalizeOrderStatus
} from '../../utils/order'

const maxAttachmentCount = 3
const maxAttachmentSize = 5 * 1024 * 1024

const orders = ref([])
const afterSales = ref([])
const loading = ref(false)
const submittingAfterSale = ref(false)

const reviewDialogVisible = ref(false)
const reviewOrderId = ref(null)
const reviewForm = reactive({
  rating: 5,
  content: ''
})

const afterSaleDialogVisible = ref(false)
const afterSaleOrderId = ref(null)
const attachmentInputRef = ref(null)
const draftAttachments = ref([])
const afterSaleForm = reactive({
  issueType: '服务质量不达标',
  content: '',
  contactPhone: ''
})

const afterSaleMap = computed(() =>
  Object.fromEntries((afterSales.value || []).map((item) => [item.orderId, item]))
)

const orderRows = computed(() =>
  (orders.value || []).map((item) => ({
    ...item,
    afterSale: afterSaleMap.value[item.id] || null
  }))
)

const orderSummary = computed(() => ({
  total: orderRows.value.length,
  completed: orderRows.value.filter((item) => normalizeOrderStatus(item.status) === 'COMPLETED').length,
  afterSales: orderRows.value.filter((item) => item.afterSale).length
}))

const draftAttachmentPreviewList = computed(() =>
  draftAttachments.value.map((item) => item.url)
)

function canReview(order) {
  return normalizeOrderStatus(order.status) === 'COMPLETED' && !order.reviewed
}

function canCreateAfterSale(order) {
  if (order.afterSale) return false
  return normalizeOrderStatus(order.status) !== 'PENDING'
}

function openReview(order) {
  reviewOrderId.value = order.id
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewDialogVisible.value = true
}

function openAfterSale(order) {
  afterSaleOrderId.value = order.id
  afterSaleForm.issueType = '服务质量不达标'
  afterSaleForm.content = ''
  afterSaleForm.contactPhone = order.contactPhone || ''
  resetDraftAttachments()
  afterSaleDialogVisible.value = true
}

function closeAfterSaleDialog() {
  afterSaleDialogVisible.value = false
  resetDraftAttachments()
}

function openAttachmentPicker() {
  attachmentInputRef.value?.click()
}

function handleAttachmentInputChange(event) {
  const files = Array.from(event.target.files || [])
  if (!files.length) {
    return
  }

  const nextAttachments = [...draftAttachments.value]
  for (const file of files) {
    if (!file.type.startsWith('image/')) {
      ElMessage.warning(`文件 ${file.name} 不是图片格式`)
      continue
    }
    if (file.size > maxAttachmentSize) {
      ElMessage.warning(`文件 ${file.name} 超过 5MB 大小限制`)
      continue
    }
    if (nextAttachments.length >= maxAttachmentCount) {
      ElMessage.warning('售后凭证最多上传 3 张')
      break
    }

    const duplicated = nextAttachments.some(
      (item) =>
        item.name === file.name &&
        item.size === file.size &&
        item.lastModified === file.lastModified
    )
    if (duplicated) {
      continue
    }

    nextAttachments.push({
      uid: `${Date.now()}-${Math.random()}`,
      file,
      name: file.name,
      size: file.size,
      lastModified: file.lastModified,
      url: URL.createObjectURL(file)
    })
  }

  draftAttachments.value = nextAttachments
  event.target.value = ''
}

function removeDraftAttachment(uid) {
  const current = draftAttachments.value.find((item) => item.uid === uid)
  if (current?.url) {
    URL.revokeObjectURL(current.url)
  }
  draftAttachments.value = draftAttachments.value.filter((item) => item.uid !== uid)
}

function resetDraftAttachments() {
  draftAttachments.value.forEach((item) => {
    if (item.url) {
      URL.revokeObjectURL(item.url)
    }
  })
  draftAttachments.value = []
  if (attachmentInputRef.value) {
    attachmentInputRef.value.value = ''
  }
}

function formatFileSize(size) {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

async function loadData() {
  loading.value = true
  try {
    const [orderResult, afterSaleResult] = await Promise.all([
      fetchOrders(),
      fetchMyAfterSales()
    ])
    orders.value = orderResult
    afterSales.value = afterSaleResult
  } catch (error) {
    ElMessage.error(error.message || '获取订单数据失败')
  } finally {
    loading.value = false
  }
}

async function submitReview() {
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }

  try {
    await submitOrderReview(reviewOrderId.value, {
      rating: reviewForm.rating,
      content: reviewForm.content.trim()
    })
    ElMessage.success('评价提交成功')
    reviewDialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '提交评价失败')
  }
}

async function submitAfterSale() {
  if (!afterSaleForm.content.trim()) {
    ElMessage.warning('请填写问题描述')
    return
  }

  submittingAfterSale.value = true
  try {
    const createdAfterSale = await createAfterSale({
      orderId: afterSaleOrderId.value,
      issueType: afterSaleForm.issueType,
      content: afterSaleForm.content.trim(),
      contactPhone: afterSaleForm.contactPhone.trim()
    })

    const failedUploads = []
    for (const attachment of draftAttachments.value) {
      try {
        await uploadAfterSaleAttachment(createdAfterSale.id, attachment.file)
      } catch (error) {
        failedUploads.push(attachment.name)
      }
    }

    if (failedUploads.length) {
      ElMessage.warning(`售后单已创建，但有 ${failedUploads.length} 张凭证上传失败`)
    } else {
      ElMessage.success('售后反馈已提交')
    }

    afterSaleDialogVisible.value = false
    resetDraftAttachments()
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '提交售后失败')
  } finally {
    submittingAfterSale.value = false
  }
}

onMounted(loadData)
onBeforeUnmount(resetDraftAttachments)
</script>
