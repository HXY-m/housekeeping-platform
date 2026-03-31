<template>
  <div class="page-stack">
    <div v-if="!embedded" class="console-overview">
      <div>
        <el-tag type="success" round>订单详情</el-tag>
        <h1>{{ order?.serviceName || '订单详情' }}</h1>
        <p>在这里处理支付、预约确认、完工确认、评价和售后。</p>
      </div>
      <div class="hero-actions">
        <el-button plain @click="goToConversation">订单沟通</el-button>
        <el-button @click="loadData">刷新详情</el-button>
      </div>
    </div>

    <div v-else class="card-header-between">
      <div>
        <strong>{{ order?.serviceName || '订单详情' }}</strong>
        <p class="section-caption">在这里处理支付、确认、评价和售后。</p>
      </div>
      <div class="hero-actions">
        <el-button plain @click="goToConversation">订单沟通</el-button>
        <el-button @click="loadData">刷新</el-button>
      </div>
    </div>

    <el-empty v-if="loading && !order" description="订单详情加载中" class="empty-surface" />

    <template v-else-if="order">
      <el-card shadow="never" class="order-card order-card--immersive">
        <div class="order-card__header">
          <div class="order-card__identity">
            <el-avatar :size="54" class="order-card__avatar">
              {{ order.workerName?.slice(0, 1) || '家' }}
            </el-avatar>
            <div>
              <div class="order-card__title">{{ order.serviceName }}</div>
              <div class="order-card__subtitle">订单号 #{{ order.id }} · 服务人员 {{ order.workerName || '待分配' }}</div>
            </div>
          </div>
          <div class="order-card__header-tags">
            <el-tag :type="getOrderStatusTagType(order.status)">{{ getOrderStatusLabel(order.status) }}</el-tag>
            <el-tag :type="getPaymentStatusTagType(order.paymentStatus)" effect="plain">
              {{ getPaymentStatusLabel(order.paymentStatus) }}
            </el-tag>
          </div>
        </div>

        <div class="order-card__meta-grid">
          <div class="order-meta-item">
            <span class="order-meta-item__label">预约时间</span>
            <strong>{{ formatBookingDateTime(order.bookingDate, order.bookingSlot) }}</strong>
          </div>
          <div class="order-meta-item">
            <span class="order-meta-item__label">联系人</span>
            <strong>{{ order.customerName }} · {{ order.contactPhone }}</strong>
          </div>
          <div class="order-meta-item order-meta-item--wide">
            <span class="order-meta-item__label">服务地址</span>
            <strong>{{ order.serviceAddress || '--' }}</strong>
          </div>
          <div class="order-meta-item">
            <span class="order-meta-item__label">应付金额</span>
            <strong>{{ formatCurrency(order.payableAmount || 0) }}</strong>
          </div>
          <div class="order-meta-item">
            <span class="order-meta-item__label">支付方式</span>
            <strong>{{ getPaymentMethodLabel(order.paymentMethod) }}</strong>
          </div>
        </div>

        <el-steps :active="getOrderStepProgress(order.status)" finish-status="success" simple class="order-steps">
          <el-step v-for="step in orderSteps" :key="step.key" :title="step.title" />
        </el-steps>

        <div class="order-stage-callout order-stage-callout--glass">
          <div>
            <div class="order-stage-callout__title">{{ getUserOrderFlowMeta(order).title }}</div>
            <div class="order-stage-callout__desc">{{ getUserOrderFlowMeta(order).description }}</div>
            <div class="order-stage-callout__hint">{{ getUserOrderFlowMeta(order).hint }}</div>
          </div>
          <div class="order-stage-callout__progress">
            <span class="muted-line">当前进度</span>
            <strong>{{ order.progressNote || '等待系统同步最新进度' }}</strong>
          </div>
        </div>

        <el-card v-if="order.serviceRecords?.length" shadow="never" class="order-inline-card">
          <template #header>
            <div class="card-header-between">
              <strong>服务过程时间线</strong>
              <span class="muted-line">查看服务人员上传的打卡、过程凭证和完工记录</span>
            </div>
          </template>
          <OrderServiceRecordTimeline :records="order.serviceRecords" />
        </el-card>

        <div class="order-card__extra-grid">
          <div class="info-panel">
            <span class="info-panel__label">支付信息</span>
            <div class="hero-actions">
              <el-tag :type="getPaymentStatusTagType(order.paymentStatus)">
                {{ getPaymentStatusLabel(order.paymentStatus) }}
              </el-tag>
              <span class="muted-line">{{ getPaymentMethodLabel(order.paymentMethod) }}</span>
            </div>
            <span class="muted-line">
              {{ order.paidAt ? `支付时间：${order.paidAt}` : '订单创建后请尽快完成支付，支付成功后才能确认预约安排。' }}
            </span>
          </div>

          <div v-if="order.reviewed" class="info-panel">
            <span class="info-panel__label">我的评价</span>
            <el-rate :model-value="order.reviewRating" disabled />
            <span class="muted-line">{{ order.reviewContent || '--' }}</span>
          </div>

          <div v-if="currentAfterSale" class="info-panel">
            <span class="info-panel__label">售后进度</span>
            <el-tag :type="getAfterSaleStatusTagType(currentAfterSale.status)">
              {{ getAfterSaleStatusLabel(currentAfterSale.status) }}
            </el-tag>
            <span class="muted-line">{{ currentAfterSale.issueType }}</span>
            <AttachmentGallery
              :attachments="currentAfterSale.attachments"
              compact
              empty-text="未上传凭证"
            />
            <span v-if="currentAfterSale.adminRemark" class="muted-line">
              处理备注：{{ currentAfterSale.adminRemark }}
            </span>
          </div>

          <div v-if="order.remark" class="info-panel">
            <span class="info-panel__label">下单备注</span>
            <span class="muted-line">{{ order.remark }}</span>
          </div>
        </div>

        <div class="order-card__actions">
          <el-button
            v-if="canPay(order)"
            type="primary"
            plain
            @click="openPaymentDialog"
          >
            去支付
          </el-button>
          <el-button plain @click="openPaymentHistory">支付记录</el-button>
          <el-button
            v-if="normalizeOrderStatus(order.status) === 'ACCEPTED' && order.paymentStatus === 'PAID'"
            type="primary"
            @click="handleUserAction('confirm')"
          >
            确认预约安排
          </el-button>
          <el-button
            v-if="normalizeOrderStatus(order.status) === 'WAITING_USER_CONFIRMATION'"
            type="success"
            @click="handleUserAction('complete')"
          >
            确认完工
          </el-button>
          <el-button
            v-if="canReview(order)"
            type="primary"
            plain
            @click="openReview"
          >
            提交评价
          </el-button>
          <el-button
            v-if="canCreateAfterSale(order)"
            plain
            @click="openAfterSale"
          >
            申请售后
          </el-button>
        </div>
      </el-card>
    </template>

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

    <el-dialog v-model="paymentDialogVisible" title="订单支付" width="520px">
      <div v-if="order" class="page-stack">
        <div class="info-panel">
          <span class="info-panel__label">订单信息</span>
          <strong>{{ order.serviceName }}</strong>
          <span class="muted-line">订单号 #{{ order.id }} · 服务人员 {{ order.workerName }}</span>
        </div>
        <div class="order-card__meta-grid">
          <div class="order-meta-item">
            <span class="order-meta-item__label">应付金额</span>
            <strong>{{ formatCurrency(order.payableAmount || 0) }}</strong>
          </div>
          <div class="order-meta-item">
            <span class="order-meta-item__label">当前状态</span>
            <el-tag :type="getPaymentStatusTagType(order.paymentStatus)">
              {{ getPaymentStatusLabel(order.paymentStatus) }}
            </el-tag>
          </div>
        </div>
        <el-form label-position="top">
          <el-form-item label="支付方式">
            <el-radio-group v-model="paymentForm.paymentMethod" class="tag-list">
              <el-radio-button
                v-for="item in PAYMENT_METHOD_OPTIONS"
                :key="item.value"
                :label="item.value"
              >
                {{ item.label }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="paymentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingPayment" @click="submitPayment">确认支付</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="paymentHistoryDialogVisible" title="支付记录" width="560px">
      <div class="page-stack">
        <div v-if="paymentRecords.length" class="info-stack">
          <div v-for="record in paymentRecords" :key="record.id" class="info-panel">
            <div class="card-header-between">
              <strong>{{ getPaymentMethodLabel(record.paymentMethod) }}</strong>
              <el-tag :type="getPaymentStatusTagType(record.paymentStatus)">
                {{ getPaymentStatusLabel(record.paymentStatus) }}
              </el-tag>
            </div>
            <span class="muted-line">支付单号：{{ record.paymentNo }}</span>
            <span class="muted-line">金额：{{ formatCurrency(record.amount || 0) }}</span>
            <span class="muted-line">创建时间：{{ record.createdAt || '--' }}</span>
            <span class="muted-line">支付时间：{{ record.paidAt || '--' }}</span>
          </div>
        </div>
        <el-empty v-else description="当前订单还没有支付记录" class="empty-surface" />
      </div>
      <template #footer>
        <el-button @click="paymentHistoryDialogVisible = false">关闭</el-button>
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AttachmentGallery from '../../components/common/AttachmentGallery.vue'
import OrderServiceRecordTimeline from '../../components/common/OrderServiceRecordTimeline.vue'
import {
  confirmUserOrder,
  confirmUserOrderCompletion,
  createAfterSale,
  fetchMyAfterSales,
  fetchOrderDetail,
  fetchOrderPayments,
  payOrder,
  submitOrderReview,
  uploadAfterSaleAttachment
} from '../../api'
import { getAfterSaleStatusLabel, getAfterSaleStatusTagType } from '../../utils/afterSale'
import { formatBookingDateTime } from '../../utils/bookingSlots'
import { getOrderStepProgress, getOrderSteps, getUserOrderFlowMeta } from '../../utils/orderFlow'
import { getOrderStatusLabel, getOrderStatusTagType, normalizeOrderStatus } from '../../utils/order'
import { buildConversationPath } from '../../utils/orderNavigation'
import { formatCurrency } from '../../utils/format'
import { getPaymentMethodLabel, getPaymentStatusLabel, getPaymentStatusTagType, PAYMENT_METHOD_OPTIONS } from '../../utils/payment'

const props = defineProps({
  embedded: {
    type: Boolean,
    default: false
  },
  orderId: {
    type: [String, Number],
    default: ''
  }
})

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const order = ref(null)
const currentAfterSale = ref(null)
const paymentRecords = ref([])

const reviewDialogVisible = ref(false)
const reviewForm = reactive({
  rating: 5,
  content: ''
})

const paymentDialogVisible = ref(false)
const paymentHistoryDialogVisible = ref(false)
const submittingPayment = ref(false)
const paymentForm = reactive({
  paymentMethod: 'WECHAT_PAY'
})

const afterSaleDialogVisible = ref(false)
const submittingAfterSale = ref(false)
const attachmentInputRef = ref(null)
const draftAttachments = ref([])
const maxAttachmentCount = 3
const maxAttachmentSize = 5 * 1024 * 1024
const afterSaleForm = reactive({
  issueType: '服务质量不达标',
  content: '',
  contactPhone: ''
})

const orderSteps = getOrderSteps()
const draftAttachmentPreviewList = computed(() => draftAttachments.value.map((item) => item.url))
const resolvedOrderId = computed(() => String(props.orderId || route.params.id || ''))

async function loadData() {
  if (!resolvedOrderId.value) {
    order.value = null
    currentAfterSale.value = null
    return
  }

  loading.value = true
  try {
    const [orderResult, afterSales] = await Promise.all([
      fetchOrderDetail(resolvedOrderId.value),
      fetchMyAfterSales()
    ])
    order.value = orderResult
    currentAfterSale.value = (afterSales || []).find((item) => String(item.orderId) === resolvedOrderId.value) || null
  } catch (error) {
    ElMessage.error(error.message || '获取订单详情失败')
  } finally {
    loading.value = false
  }
}

function canReview(currentOrder) {
  return normalizeOrderStatus(currentOrder.status) === 'COMPLETED' && !currentOrder.reviewed
}

function canPay(currentOrder) {
  return currentOrder.paymentStatus !== 'PAID'
}

function canCreateAfterSale(currentOrder) {
  if (currentAfterSale.value) {
    return false
  }
  return normalizeOrderStatus(currentOrder.status) !== 'PENDING'
}

function goToConversation() {
  router.push(buildConversationPath('user', resolvedOrderId.value))
}

function openReview() {
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewDialogVisible.value = true
}

function openPaymentDialog() {
  paymentForm.paymentMethod = order.value?.paymentMethod || 'WECHAT_PAY'
  paymentDialogVisible.value = true
}

async function openPaymentHistory() {
  paymentHistoryDialogVisible.value = true
  try {
    paymentRecords.value = await fetchOrderPayments(resolvedOrderId.value)
  } catch (error) {
    paymentRecords.value = []
    ElMessage.error(error.message || '获取支付记录失败')
  }
}

function openAfterSale() {
  afterSaleForm.issueType = '服务质量不达标'
  afterSaleForm.content = ''
  afterSaleForm.contactPhone = order.value?.contactPhone || ''
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

async function submitPayment() {
  submittingPayment.value = true
  try {
    await payOrder(resolvedOrderId.value, {
      paymentMethod: paymentForm.paymentMethod
    })
    ElMessage.success('支付成功')
    paymentDialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '支付失败')
  } finally {
    submittingPayment.value = false
  }
}

async function handleUserAction(action) {
  try {
    if (action === 'confirm') {
      await confirmUserOrder(resolvedOrderId.value)
      ElMessage.success('预约安排已确认')
    } else {
      await confirmUserOrderCompletion(resolvedOrderId.value)
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
    await submitOrderReview(resolvedOrderId.value, {
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
      orderId: Number(resolvedOrderId.value),
      issueType: afterSaleForm.issueType,
      content: afterSaleForm.content.trim(),
      contactPhone: afterSaleForm.contactPhone.trim()
    })

    for (const attachment of draftAttachments.value) {
      await uploadAfterSaleAttachment(createdAfterSale.id, attachment.file)
    }

    ElMessage.success('售后反馈已提交')
    afterSaleDialogVisible.value = false
    resetDraftAttachments()
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '提交售后失败')
  } finally {
    submittingAfterSale.value = false
  }
}

watch(resolvedOrderId, () => {
  loadData()
}, { immediate: true })

onMounted(loadData)
onBeforeUnmount(resetDraftAttachments)
</script>
