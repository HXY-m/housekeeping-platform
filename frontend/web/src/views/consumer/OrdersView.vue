<template>
  <div class="page-stack">
    <div class="console-overview">
      <div>
        <el-tag type="success" round>我的订单</el-tag>
        <h1>订单进度与履约记录</h1>
        <p>订单由你和服务人员共同推进。页面会明确标记当前责任方、下一步操作和关键凭证。</p>
      </div>
      <div class="hero-actions">
        <el-button plain @click="router.push('/workers')">继续预约服务</el-button>
        <el-button type="primary" @click="loadData">刷新订单</el-button>
      </div>
    </div>

    <div class="metric-strip">
      <div class="metric-chip">
        <span class="metric-chip__label">订单总数</span>
        <strong>{{ orderSummary.total }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">处理中</span>
        <strong>{{ orderSummary.active }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待我确认</span>
        <strong>{{ orderSummary.needUserAction }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">售后工单</span>
        <strong>{{ orderSummary.afterSales }}</strong>
      </div>
    </div>

    <div class="page-grid--sidebar order-console-grid">
      <div class="page-stack">
        <el-card shadow="never" class="page-panel">
          <div class="card-header-between">
            <div>
              <strong>订单筛选</strong>
              <p class="section-caption">按履约阶段切换列表，优先处理等待你确认的订单。</p>
            </div>
          </div>

          <div class="order-filter-pills">
            <el-radio-group v-model="statusFilter" size="small" class="order-filter-pills__group">
              <el-radio-button label="ALL">全部</el-radio-button>
              <el-radio-button label="PENDING">待接单</el-radio-button>
              <el-radio-button label="ACCEPTED">待我确认</el-radio-button>
              <el-radio-button label="CONFIRMED">待上门</el-radio-button>
              <el-radio-button label="IN_SERVICE">服务中</el-radio-button>
              <el-radio-button label="WAITING_USER_CONFIRMATION">待确认完工</el-radio-button>
              <el-radio-button label="COMPLETED">已完成</el-radio-button>
            </el-radio-group>
          </div>
        </el-card>

        <div v-if="loading && !pagedOrders.length" class="order-skeleton-list">
          <el-skeleton v-for="index in 3" :key="index" animated class="order-skeleton-card">
            <template #template>
              <el-skeleton-item variant="rect" style="width: 100%; height: 280px; border-radius: 24px;" />
            </template>
          </el-skeleton>
        </div>

        <el-empty v-else-if="!filteredOrders.length" description="当前没有匹配的订单" class="empty-surface" />

        <div v-else class="order-card-grid">
          <el-card v-for="row in pagedOrders" :key="row.id" shadow="never" class="order-card order-card--immersive">
            <div class="order-card__header">
              <div class="order-card__identity">
                <el-avatar :size="54" class="order-card__avatar">
                  {{ row.workerName?.slice(0, 1) || '师' }}
                </el-avatar>
                <div>
                  <div class="order-card__title">{{ row.serviceName }}</div>
                  <div class="order-card__subtitle">
                    订单号 #{{ row.id }} · 服务人员 {{ row.workerName || '待分配' }}
                  </div>
                </div>
              </div>
              <div class="order-card__header-tags">
                <el-tag :type="getOrderStatusTagType(row.status)">{{ getOrderStatusLabel(row.status) }}</el-tag>
                <el-tag :type="getUserOrderFlowMeta(row).ownerType" effect="plain">
                  当前由{{ getUserOrderFlowMeta(row).owner }}推进
                </el-tag>
              </div>
            </div>

            <div class="order-card__meta-grid">
              <div class="order-meta-item">
                <span class="order-meta-item__label">预约时间</span>
                <strong>{{ row.bookingDate }} {{ row.bookingSlot }}</strong>
              </div>
              <div class="order-meta-item">
                <span class="order-meta-item__label">联系人</span>
                <strong>{{ row.customerName }} · {{ row.contactPhone }}</strong>
              </div>
              <div class="order-meta-item order-meta-item--wide">
                <span class="order-meta-item__label">服务地址</span>
                <strong>{{ row.serviceAddress }}</strong>
              </div>
            </div>

            <el-steps :active="getOrderStepProgress(row.status)" finish-status="success" simple class="order-steps">
              <el-step v-for="step in orderSteps" :key="step.key" :title="step.title" />
            </el-steps>

            <div class="order-stage-callout order-stage-callout--glass">
              <div>
                <div class="order-stage-callout__title">{{ getUserOrderFlowMeta(row).title }}</div>
                <div class="order-stage-callout__desc">{{ getUserOrderFlowMeta(row).description }}</div>
                <div class="order-stage-callout__hint">{{ getUserOrderFlowMeta(row).hint }}</div>
              </div>
              <div class="order-stage-callout__progress">
                <span class="muted-line">当前进度</span>
                <strong>{{ row.progressNote || '等待系统同步最新进度' }}</strong>
              </div>
            </div>

            <el-card v-if="row.serviceRecords?.length" shadow="never" class="order-inline-card">
              <template #header>
                <div class="card-header-between">
                  <strong>服务过程时间线</strong>
                  <span class="muted-line">服务人员上传的打卡、过程凭证和完工记录</span>
                </div>
              </template>
              <OrderServiceRecordTimeline :records="row.serviceRecords" />
            </el-card>

            <div v-if="row.reviewed || row.afterSale || row.remark" class="order-card__extra-grid">
              <div v-if="row.reviewed" class="info-panel">
                <span class="info-panel__label">我的评价</span>
                <el-rate :model-value="row.reviewRating" disabled />
                <span class="muted-line">{{ row.reviewContent }}</span>
              </div>
              <div v-if="row.afterSale" class="info-panel">
                <span class="info-panel__label">售后进度</span>
                <el-tag :type="getAfterSaleStatusTagType(row.afterSale.status)">
                  {{ getAfterSaleStatusLabel(row.afterSale.status) }}
                </el-tag>
                <span class="muted-line">{{ row.afterSale.issueType }}</span>
                <AttachmentGallery
                  :attachments="row.afterSale.attachments"
                  compact
                  empty-text="未上传凭证"
                />
                <span v-if="row.afterSale.adminRemark" class="muted-line">处理备注：{{ row.afterSale.adminRemark }}</span>
              </div>
              <div v-if="row.remark" class="info-panel">
                <span class="info-panel__label">下单备注</span>
                <span class="muted-line">{{ row.remark }}</span>
              </div>
            </div>

            <div class="order-card__actions">
              <el-button plain @click="goToMessageCenter(row.id)">订单沟通</el-button>
              <el-button
                v-if="normalizeOrderStatus(row.status) === 'ACCEPTED'"
                type="primary"
                @click="handleUserAction(row.id, 'confirm')"
              >
                确认预约安排
              </el-button>
              <el-button
                v-if="normalizeOrderStatus(row.status) === 'WAITING_USER_CONFIRMATION'"
                type="success"
                @click="handleUserAction(row.id, 'complete')"
              >
                确认完工
              </el-button>
              <el-button
                v-if="canReview(row)"
                type="primary"
                plain
                @click="openReview(row)"
              >
                提交评价
              </el-button>
              <el-button
                v-if="canCreateAfterSale(row)"
                plain
                @click="openAfterSale(row)"
              >
                申请售后
              </el-button>
              <span v-if="!hasPrimaryAction(row)" class="muted-line">当前暂无可执行操作</span>
            </div>
          </el-card>
        </div>

        <ListPagination
          v-if="filteredOrders.length"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="pageSizes"
          :total="total"
        />
      </div>

      <div class="page-stack">
        <el-card shadow="never" class="page-panel order-helper-panel">
          <div class="card-header-between">
            <strong>当前视图</strong>
            <el-tag effect="plain" type="success">{{ activeFilterLabel }}</el-tag>
          </div>
          <div class="info-stack">
            <div class="info-panel">
              <span class="info-panel__label">优先处理</span>
              <strong>{{ priorityHint.title }}</strong>
              <span class="muted-line">{{ priorityHint.description }}</span>
            </div>
            <div class="info-panel">
              <span class="info-panel__label">温馨提示</span>
              <span class="muted-line">关键节点需要你和服务人员共同确认，异常情况可先保留凭证，再走售后流程。</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>

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
            placeholder="例如：是否准时、是否细致、沟通是否顺畅"
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
          <el-input v-model="afterSaleForm.contactPhone" placeholder="便于客服回访" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input
            v-model="afterSaleForm.content"
            type="textarea"
            :rows="5"
            maxlength="300"
            show-word-limit
            placeholder="请尽量描述清楚问题经过、影响范围和你的诉求"
          />
        </el-form-item>
        <el-form-item label="凭证图片">
          <div class="page-stack full-width">
            <div class="hero-actions">
              <el-button plain @click="openAttachmentPicker" :disabled="draftAttachments.length >= maxAttachmentCount">
                选择凭证图片
              </el-button>
              <span class="muted-line">支持 jpg/png/webp，最多 3 张，每张不超过 5MB</span>
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
            <span v-else class="muted-line">未选择图片也可以先创建售后工单。</span>
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
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AttachmentGallery from '../../components/common/AttachmentGallery.vue'
import ListPagination from '../../components/common/ListPagination.vue'
import OrderServiceRecordTimeline from '../../components/common/OrderServiceRecordTimeline.vue'
import { useServerPagination } from '../../composables/useServerPagination'
import {
  confirmUserOrder,
  confirmUserOrderCompletion,
  createAfterSale,
  fetchMyAfterSales,
  fetchOrderSummary,
  fetchOrders,
  submitOrderReview,
  uploadAfterSaleAttachment
} from '../../api'
import { getAfterSaleStatusLabel, getAfterSaleStatusTagType } from '../../utils/afterSale'
import { getUserOrderFlowMeta, getOrderStepProgress, getOrderSteps } from '../../utils/orderFlow'
import { getOrderStatusLabel, getOrderStatusTagType, normalizeOrderStatus } from '../../utils/order'

const router = useRouter()
const maxAttachmentCount = 3
const maxAttachmentSize = 5 * 1024 * 1024

const orders = ref([])
const afterSales = ref([])
const loading = ref(false)
const submittingAfterSale = ref(false)
const statusFilter = ref('ALL')
const orderSummary = ref({
  total: 0,
  active: 0,
  needUserAction: 0,
  afterSales: 0
})

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

const orderSteps = getOrderSteps()

const afterSaleMap = computed(() =>
  Object.fromEntries((afterSales.value || []).map((item) => [item.orderId, item]))
)

const orderRows = computed(() =>
  (orders.value || []).map((item) => ({
    ...item,
    afterSale: afterSaleMap.value[item.id] || null
  }))
)

const filteredOrders = computed(() => {
  if (statusFilter.value === 'ALL') {
    return orderRows.value
  }
  return orderRows.value.filter((item) => normalizeOrderStatus(item.status) === statusFilter.value)
})

const { currentPage, pageSize, pageSizes, total, buildParams, applyPageResult, resetPage } = useServerPagination(6)
const pagedOrders = filteredOrders

const activeFilterLabel = computed(() => {
  const labelMap = {
    ALL: '全部订单',
    PENDING: '待接单',
    ACCEPTED: '待我确认',
    CONFIRMED: '待上门',
    IN_SERVICE: '服务中',
    WAITING_USER_CONFIRMATION: '待确认完工',
    COMPLETED: '已完成'
  }
  return labelMap[statusFilter.value] || '全部订单'
})

const priorityHint = computed(() => {
  if (statusFilter.value === 'ACCEPTED' || orderSummary.value.needUserAction > 0) {
    return {
      title: '优先确认预约或完工',
      description: '这类订单需要你推进，确认后才能进入下一步流程。'
    }
  }
  if (statusFilter.value === 'IN_SERVICE') {
    return {
      title: '关注过程记录',
      description: '服务进行中时，建议留意服务人员上传的过程凭证。'
    }
  }
  return {
    title: '按阶段有序处理',
    description: '可以先看待我确认和售后相关订单，再统一查看已完成记录。'
  }
})

watch(statusFilter, () => {
  if (currentPage.value !== 1) {
    resetPage()
    return
  }
  loadData()
})

const draftAttachmentPreviewList = computed(() => draftAttachments.value.map((item) => item.url))

function canReview(order) {
  return normalizeOrderStatus(order.status) === 'COMPLETED' && !order.reviewed
}

function canCreateAfterSale(order) {
  if (order.afterSale) return false
  return normalizeOrderStatus(order.status) !== 'PENDING'
}

function hasPrimaryAction(order) {
  return normalizeOrderStatus(order.status) === 'ACCEPTED'
    || normalizeOrderStatus(order.status) === 'WAITING_USER_CONFIRMATION'
    || canReview(order)
    || canCreateAfterSale(order)
}

function goToMessageCenter(orderId) {
  router.push({
    path: '/user/messages',
    query: { orderId: String(orderId) }
  })
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
      ElMessage.warning(`文件 ${file.name} 超过 5MB 限制`)
      continue
    }
    if (nextAttachments.length >= maxAttachmentCount) {
      ElMessage.warning('售后凭证最多上传 3 张')
      break
    }

    nextAttachments.push({
      uid: `${Date.now()}-${Math.random()}`,
      file,
      name: file.name,
      size: file.size,
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
    const params = buildParams({
      status: statusFilter.value === 'ALL' ? '' : statusFilter.value
    })
    const [orderResult, afterSaleResult, summaryResult] = await Promise.all([
      fetchOrders(params),
      fetchMyAfterSales(),
      fetchOrderSummary({
        status: statusFilter.value === 'ALL' ? '' : statusFilter.value
      })
    ])
    orders.value = applyPageResult(orderResult)
    afterSales.value = afterSaleResult
    orderSummary.value = {
      total: Number(summaryResult?.total || 0),
      active: Number(summaryResult?.active || 0),
      needUserAction: Number(summaryResult?.needUserAction || 0),
      afterSales: Number(summaryResult?.afterSales || 0)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取订单数据失败')
  } finally {
    loading.value = false
  }
}

async function handleUserAction(id, action) {
  try {
    if (action === 'confirm') {
      await confirmUserOrder(id)
      ElMessage.success('预约安排已确认')
    } else {
      await confirmUserOrderCompletion(id)
      ElMessage.success('订单已确认完工')
    }
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '订单流转失败')
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
      } catch {
        failedUploads.push(attachment.name)
      }
    }

    if (failedUploads.length) {
      ElMessage.warning(`售后工单已创建，但有 ${failedUploads.length} 张凭证上传失败`)
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

watch([currentPage, pageSize], () => {
  loadData()
})

onMounted(loadData)
onBeforeUnmount(resetDraftAttachments)
</script>
