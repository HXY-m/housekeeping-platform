<template>
  <div class="page-stack">
    <div class="console-overview console-overview--worker">
      <div>
        <el-tag type="warning" round>服务工作台</el-tag>
        <h1>接单、履约与过程记录</h1>
        <p>订单推进需要你和用户共同确认。你负责接单、打卡、上传凭证和提交完工，关键节点由用户确认。</p>
      </div>
      <div class="hero-actions">
        <el-button plain @click="router.push('/worker/messages')">查看消息</el-button>
        <el-button type="primary" @click="loadOrders">刷新订单</el-button>
      </div>
    </div>

    <div class="metric-strip">
      <div class="metric-chip">
        <span class="metric-chip__label">分配订单</span>
        <strong>{{ summary.total }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待接单</span>
        <strong>{{ summary.pending }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待用户确认</span>
        <strong>{{ summary.accepted }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待开工</span>
        <strong>{{ summary.confirmed }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待确认完工</span>
        <strong>{{ summary.waitingCompletion }}</strong>
      </div>
    </div>

    <el-alert
      v-if="!workerQualified"
      :title="qualificationBanner.title"
      :description="qualificationBanner.description"
      :type="qualificationBanner.type"
      show-icon
      :closable="false"
    >
      <template #default>
        <div class="hero-actions">
          <el-button type="primary" @click="router.push('/worker/qualification')">去填写资质信息</el-button>
        </div>
      </template>
    </el-alert>

    <div class="page-grid--sidebar worker-order-grid">
      <div class="page-stack">
        <el-card shadow="never" class="page-panel">
          <div class="card-header-between">
            <div>
              <strong>履约筛选</strong>
              <p class="section-caption">优先处理待接单和待开工订单，服务中订单建议持续补充现场记录。</p>
            </div>
          </div>

          <div class="order-filter-pills">
            <el-radio-group v-model="statusFilter" size="small" class="order-filter-pills__group">
              <el-radio-button label="ALL">全部</el-radio-button>
              <el-radio-button label="PENDING">待接单</el-radio-button>
              <el-radio-button label="ACCEPTED">待用户确认</el-radio-button>
              <el-radio-button label="CONFIRMED">待开工</el-radio-button>
              <el-radio-button label="IN_SERVICE">服务中</el-radio-button>
              <el-radio-button label="WAITING_USER_CONFIRMATION">待确认完工</el-radio-button>
              <el-radio-button label="COMPLETED">已完成</el-radio-button>
            </el-radio-group>
          </div>
        </el-card>

        <div v-if="loading && !pagedOrders.length" class="order-skeleton-list">
          <el-skeleton v-for="index in 3" :key="index" animated class="order-skeleton-card">
            <template #template>
              <el-skeleton-item variant="rect" style="width: 100%; height: 300px; border-radius: 24px;" />
            </template>
          </el-skeleton>
        </div>

        <el-empty v-else-if="!filteredOrders.length" description="当前没有匹配的服务订单" class="empty-surface" />

        <div v-else class="order-card-grid">
          <el-card v-for="order in pagedOrders" :key="order.id" shadow="never" class="order-card order-card--immersive">
            <div class="order-card__header">
              <div class="order-card__identity">
                <el-avatar :size="54" class="order-card__avatar">
                  {{ order.customerName?.slice(0, 1) || '客' }}
                </el-avatar>
                <div>
                  <div class="order-card__title">{{ order.serviceName }}</div>
                  <div class="order-card__subtitle">订单号 #{{ order.id }} · 用户 {{ order.customerName }}</div>
                </div>
              </div>
              <div class="order-card__header-tags">
                <el-tag :type="getOrderStatusTagType(order.status)">{{ getOrderStatusLabel(order.status) }}</el-tag>
                <el-tag :type="getWorkerOrderFlowMeta(order).ownerType" effect="plain">
                  {{ getWorkerOrderFlowMeta(order).title }}
                </el-tag>
              </div>
            </div>

            <div class="order-card__meta-grid">
              <div class="order-meta-item">
                <span class="order-meta-item__label">预约时间</span>
                <strong>{{ order.bookingDate }} {{ order.bookingSlot }}</strong>
              </div>
              <div class="order-meta-item">
                <span class="order-meta-item__label">联系方式</span>
                <strong>{{ order.customerName }} · {{ order.contactPhone }}</strong>
              </div>
              <div class="order-meta-item order-meta-item--wide">
                <span class="order-meta-item__label">服务地址</span>
                <strong>{{ order.serviceAddress }}</strong>
              </div>
            </div>

            <el-steps :active="getOrderStepProgress(order.status)" finish-status="success" simple class="order-steps">
              <el-step v-for="step in orderSteps" :key="step.key" :title="step.title" />
            </el-steps>

            <div class="order-stage-callout order-stage-callout--glass">
              <div>
                <div class="order-stage-callout__title">{{ getWorkerOrderFlowMeta(order).title }}</div>
                <div class="order-stage-callout__desc">{{ getWorkerOrderFlowMeta(order).description }}</div>
                <div class="order-stage-callout__hint">{{ getWorkerOrderFlowMeta(order).hint }}</div>
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
                  <span class="muted-line">上门打卡、过程照片和完工证明</span>
                </div>
              </template>
              <OrderServiceRecordTimeline :records="order.serviceRecords" />
            </el-card>

            <div v-if="order.remark || order.reviewed" class="order-card__extra-grid">
              <div v-if="order.remark" class="info-panel">
                <span class="info-panel__label">用户备注</span>
                <span class="muted-line">{{ order.remark }}</span>
              </div>
              <div v-if="order.reviewed" class="info-panel">
                <span class="info-panel__label">用户评价</span>
                <el-rate :model-value="order.reviewRating" disabled />
                <span class="muted-line">{{ order.reviewContent }}</span>
              </div>
            </div>

            <div class="order-card__actions">
              <el-button plain @click="goToMessageCenter(order.id)">订单沟通</el-button>
              <el-button
                v-if="workerQualified && normalizeOrderStatus(order.status) === 'PENDING'"
                type="primary"
                @click="handleAction(order.id, 'accept')"
              >
                确认接单
              </el-button>
              <el-button
                v-if="workerQualified && normalizeOrderStatus(order.status) === 'CONFIRMED'"
                plain
                @click="openRecordDialog(order, 'CHECK_IN')"
              >
                上传上门打卡
              </el-button>
              <el-button
                v-if="workerQualified && normalizeOrderStatus(order.status) === 'CONFIRMED'"
                type="warning"
                @click="handleAction(order.id, 'start')"
              >
                开始服务
              </el-button>
              <el-button
                v-if="workerQualified && normalizeOrderStatus(order.status) === 'IN_SERVICE'"
                plain
                @click="openRecordDialog(order, 'SERVICE_PROOF')"
              >
                上传过程凭证
              </el-button>
              <el-button
                v-if="workerQualified && normalizeOrderStatus(order.status) === 'IN_SERVICE'"
                plain
                @click="openRecordDialog(order, 'FINISH_PROOF')"
              >
                上传完工凭证
              </el-button>
              <el-button
                v-if="workerQualified && normalizeOrderStatus(order.status) === 'IN_SERVICE'"
                type="success"
                @click="handleAction(order.id, 'complete')"
              >
                提交完工
              </el-button>
              <span v-if="normalizeOrderStatus(order.status) === 'ACCEPTED'" class="muted-line">
                已接单，等待用户确认预约安排
              </span>
              <span v-if="normalizeOrderStatus(order.status) === 'WAITING_USER_CONFIRMATION'" class="muted-line">
                已提交完工，等待用户确认
              </span>
              <span v-if="normalizeOrderStatus(order.status) === 'COMPLETED'" class="muted-line">
                {{ order.reviewed ? '订单已闭环，可以继续处理新的预约。' : '订单已完成，等待用户评价或售后。' }}
              </span>
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
            <strong>履约提示</strong>
            <el-tag effect="plain" type="warning">{{ activeFilterLabel }}</el-tag>
          </div>
          <div class="info-stack">
            <div class="info-panel">
              <span class="info-panel__label">当前重点</span>
              <strong>{{ workerHint.title }}</strong>
              <span class="muted-line">{{ workerHint.description }}</span>
            </div>
            <div class="info-panel">
              <span class="info-panel__label">服务建议</span>
              <span class="muted-line">关键节点尽量上传打卡或现场凭证，这会让用户确认和售后判断都更顺畅。</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <el-dialog v-model="recordDialogVisible" title="上传服务记录" width="560px">
      <el-form label-position="top">
        <el-form-item label="记录阶段">
          <el-select v-model="recordForm.stage" style="width: 100%">
            <el-option
              v-for="item in availableRecordStages"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="记录说明">
          <el-input
            v-model.trim="recordForm.description"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="例如：已到达现场并完成打卡，或已完成厨房重油污清洁。"
          />
        </el-form-item>
        <el-form-item label="现场图片">
          <div class="page-stack full-width">
            <div class="hero-actions">
              <el-button plain @click="recordImageInputRef?.click()">选择图片</el-button>
              <span class="muted-line">建议上传现场打卡、过程照片或完工证明，单张不超过 5MB。</span>
            </div>
            <div v-if="recordImagePreview" class="attachment-editor-item">
              <el-image :src="recordImagePreview" fit="cover" preview-teleported class="attachment-thumb" />
              <div class="attachment-meta">
                <span>{{ recordForm.file?.name || '现场图片' }}</span>
                <span class="muted-line">{{ recordForm.file ? formatFileSize(recordForm.file.size) : '' }}</span>
              </div>
              <el-button link type="danger" @click="clearRecordFile">移除</el-button>
            </div>
            <span v-else class="muted-line">可不上传图片，但建议关键节点保留现场凭证。</span>
          </div>
        </el-form-item>
      </el-form>
      <input
        ref="recordImageInputRef"
        type="file"
        accept="image/*"
        class="visually-hidden-input"
        @change="handleRecordImageChange"
      />
      <template #footer>
        <el-button @click="closeRecordDialog">取消</el-button>
        <el-button type="primary" :loading="submittingRecord" @click="submitRecord">
          上传记录
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import OrderServiceRecordTimeline from '../../components/common/OrderServiceRecordTimeline.vue'
import ListPagination from '../../components/common/ListPagination.vue'
import { useServerPagination } from '../../composables/useServerPagination'
import {
  acceptWorkerOrder,
  completeWorkerOrder,
  fetchCurrentWorkerProfile,
  fetchWorkerOrderSummary,
  fetchWorkerOrders,
  startWorkerOrder,
  uploadWorkerServiceRecord
} from '../../api'
import { getWorkerOrderFlowMeta, getOrderStepProgress, getOrderSteps } from '../../utils/orderFlow'
import { getOrderStatusLabel, getOrderStatusTagType, normalizeOrderStatus } from '../../utils/order'
import { getWorkerQualificationNotice, isWorkerQualificationApproved } from '../../utils/workerApplication'

const router = useRouter()
const statusFilter = ref('ALL')
const orders = ref([])
const loading = ref(false)
const workerProfile = ref(null)
const orderSteps = getOrderSteps()
const summary = ref({
  total: 0,
  pending: 0,
  accepted: 0,
  confirmed: 0,
  waitingCompletion: 0
})

const recordDialogVisible = ref(false)
const submittingRecord = ref(false)
const selectedOrder = ref(null)
const recordImageInputRef = ref(null)
const recordImagePreview = ref('')
const recordForm = reactive({
  stage: 'CHECK_IN',
  description: '',
  file: null
})

const stageOptions = {
  CHECK_IN: '上门打卡',
  SERVICE_PROOF: '服务过程',
  FINISH_PROOF: '完工凭证'
}

const filteredOrders = computed(() => {
  const sorted = [...orders.value].sort((left, right) => {
    const rankMap = {
      PENDING: 0,
      ACCEPTED: 1,
      CONFIRMED: 2,
      IN_SERVICE: 3,
      WAITING_USER_CONFIRMATION: 4,
      COMPLETED: 5
    }
    return (rankMap[normalizeOrderStatus(left.status)] ?? 99) - (rankMap[normalizeOrderStatus(right.status)] ?? 99)
  })

  if (statusFilter.value === 'ALL') {
    return sorted
  }
  return sorted.filter((item) => normalizeOrderStatus(item.status) === statusFilter.value)
})

const { currentPage, pageSize, pageSizes, total, buildParams, applyPageResult, resetPage } = useServerPagination(6)
const pagedOrders = filteredOrders
const workerQualified = computed(() => isWorkerQualificationApproved(workerProfile.value?.qualificationStatus))
const qualificationBanner = computed(() => {
  const status = workerProfile.value?.qualificationStatus || 'UNSUBMITTED'
  if (status === 'PENDING') {
    return {
      title: '资质审核中，暂时不能接单',
      description: '请等待管理员审核。审核通过后，接单、开工和服务记录上传入口会自动开放。',
      type: 'warning'
    }
  }
  if (status === 'REJECTED') {
    return {
      title: '资质审核未通过，暂时不能接单',
      description: '请根据驳回意见补充服务信息和资质文件，重新提交审核后再接单。',
      type: 'error'
    }
  }
  return {
    title: '请先填写资质信息并提交审核',
    description: '服务类型、服务区域、接单时段和资质附件都需要在资质材料页提交。审核通过前不能接单。',
    type: 'info'
  }
})

const availableRecordStages = computed(() => {
  const status = normalizeOrderStatus(selectedOrder.value?.status)
  if (status === 'CONFIRMED') {
    return [{ value: 'CHECK_IN', label: stageOptions.CHECK_IN }]
  }
  if (status === 'IN_SERVICE') {
    return [
      { value: 'SERVICE_PROOF', label: stageOptions.SERVICE_PROOF },
      { value: 'FINISH_PROOF', label: stageOptions.FINISH_PROOF }
    ]
  }
  return Object.entries(stageOptions).map(([value, label]) => ({ value, label }))
})

const activeFilterLabel = computed(() => {
  const labelMap = {
    ALL: '全部订单',
    PENDING: '待接单',
    ACCEPTED: '待用户确认',
    CONFIRMED: '待开工',
    IN_SERVICE: '服务中',
    WAITING_USER_CONFIRMATION: '待确认完工',
    COMPLETED: '已完成'
  }
  return labelMap[statusFilter.value] || '全部订单'
})

const workerHint = computed(() => {
  if (statusFilter.value === 'PENDING' || summary.value.pending > 0) {
    return {
      title: '先处理待接单订单',
      description: '及时确认是否接单，能减少用户等待时间，也能避免预约流失。'
    }
  }
  if (statusFilter.value === 'CONFIRMED' || summary.value.confirmed > 0) {
    return {
      title: '待开工订单需要打卡',
      description: '开工前建议先上传上门打卡，后续流程会更清晰，也更容易建立信任。'
    }
  }
  return {
    title: '持续补充服务记录',
    description: '服务进行中和待确认完工阶段，建议上传过程凭证和完工证明。'
  }
})

watch(statusFilter, () => {
  if (currentPage.value !== 1) {
    resetPage()
    return
  }
  loadOrders()
})

function goToMessageCenter(orderId) {
  router.push({
    path: '/worker/messages',
    query: { orderId: String(orderId) }
  })
}

async function loadOrders() {
  loading.value = true
  try {
    const params = buildParams({
      status: statusFilter.value === 'ALL' ? '' : statusFilter.value
    })
    const [profileResult, result, summaryResult] = await Promise.all([
      fetchCurrentWorkerProfile().catch(() => null),
      fetchWorkerOrders(params),
      fetchWorkerOrderSummary({
        status: statusFilter.value === 'ALL' ? '' : statusFilter.value
      })
    ])
    workerProfile.value = profileResult
    orders.value = applyPageResult(result)
    summary.value = {
      total: Number(summaryResult?.total || 0),
      pending: Number(summaryResult?.pending || 0),
      accepted: Number(summaryResult?.accepted || 0),
      confirmed: Number(summaryResult?.confirmed || 0),
      waitingCompletion: Number(summaryResult?.waitingUserConfirmation || 0)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取服务订单失败')
  } finally {
    loading.value = false
  }
}

async function handleAction(id, action) {
  if (!workerQualified.value) {
    ElMessage.warning('资质审核通过后才能接单和推进订单')
    router.push('/worker/qualification')
    return
  }
  try {
    if (action === 'accept') {
      await acceptWorkerOrder(id)
    } else if (action === 'start') {
      await startWorkerOrder(id)
    } else {
      await completeWorkerOrder(id)
    }
    ElMessage.success('订单状态已更新')
    await loadOrders()
  } catch (error) {
    ElMessage.error(error.message || '订单流转失败')
  }
}

function openRecordDialog(order, stage) {
  if (!workerQualified.value) {
    ElMessage.warning('资质审核通过后才能上传服务记录')
    router.push('/worker/qualification')
    return
  }
  selectedOrder.value = order
  recordForm.stage = stage
  recordForm.description = ''
  clearRecordFile()
  recordDialogVisible.value = true
}

function clearRecordFile() {
  if (recordImagePreview.value) {
    URL.revokeObjectURL(recordImagePreview.value)
  }
  recordImagePreview.value = ''
  recordForm.file = null
  if (recordImageInputRef.value) {
    recordImageInputRef.value.value = ''
  }
}

function closeRecordDialog() {
  recordDialogVisible.value = false
  clearRecordFile()
}

function handleRecordImageChange(event) {
  const file = event.target.files?.[0]
  if (!file) {
    return
  }
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('当前仅支持上传图片格式的现场凭证')
    event.target.value = ''
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('现场图片不能超过 5MB')
    event.target.value = ''
    return
  }
  clearRecordFile()
  recordForm.file = file
  recordImagePreview.value = URL.createObjectURL(file)
}

function formatFileSize(size) {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

async function submitRecord() {
  if (!recordForm.description.trim()) {
    ElMessage.warning('请填写记录说明')
    return
  }

  submittingRecord.value = true
  try {
    await uploadWorkerServiceRecord(selectedOrder.value.id, {
      stage: recordForm.stage,
      description: recordForm.description.trim(),
      file: recordForm.file
    })
    ElMessage.success('服务记录上传成功')
    closeRecordDialog()
    await loadOrders()
  } catch (error) {
    ElMessage.error(error.message || '服务记录上传失败')
  } finally {
    submittingRecord.value = false
  }
}

watch([currentPage, pageSize], () => {
  loadOrders()
})

onMounted(loadOrders)
onBeforeUnmount(clearRecordFile)
</script>
