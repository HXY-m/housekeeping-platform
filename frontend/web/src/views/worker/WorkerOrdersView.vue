<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">服务订单工作台</h1>
          <p class="page-panel__desc">
            订单不再由服务人员单方面推进。你可以接单、补充打卡与过程记录、提交完工，但关键节点仍需用户确认。
          </p>
        </div>
        <el-button @click="loadOrders">刷新</el-button>
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
          <span class="metric-chip__label">待用户确认完工</span>
          <strong>{{ summary.waitingCompletion }}</strong>
        </div>
      </div>

      <div class="filter-row">
        <el-radio-group v-model="statusFilter" size="small">
          <el-radio-button label="ALL">全部</el-radio-button>
          <el-radio-button label="PENDING">待接单</el-radio-button>
          <el-radio-button label="ACCEPTED">待用户确认</el-radio-button>
          <el-radio-button label="CONFIRMED">待开工</el-radio-button>
          <el-radio-button label="IN_SERVICE">服务中</el-radio-button>
          <el-radio-button label="WAITING_USER_CONFIRMATION">待用户确认完工</el-radio-button>
          <el-radio-button label="COMPLETED">已完成</el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <el-empty v-if="!filteredOrders.length && !loading" description="当前没有匹配的服务订单" />

    <div v-else class="order-card-grid" v-loading="loading">
      <el-card v-for="order in pagedOrders" :key="order.id" shadow="never" class="order-card">
        <div class="order-card__header">
          <div>
            <div class="order-card__title">{{ order.serviceName }}</div>
            <div class="order-card__subtitle">订单号 #{{ order.id }} · 用户 {{ order.customerName }}</div>
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

        <div class="order-stage-callout">
          <div>
            <div class="order-stage-callout__title">{{ getWorkerOrderFlowMeta(order).title }}</div>
            <div class="order-stage-callout__desc">{{ getWorkerOrderFlowMeta(order).description }}</div>
            <div class="order-stage-callout__hint">{{ getWorkerOrderFlowMeta(order).hint }}</div>
          </div>
          <div class="order-stage-callout__progress">
            <span class="muted-line">当前进度</span>
            <strong>{{ order.progressNote }}</strong>
          </div>
        </div>

        <el-card v-if="order.serviceRecords?.length" shadow="never" class="order-inline-card">
          <template #header>
            <div class="card-header-between">
              <strong>服务过程时间线</strong>
              <span class="muted-line">上门打卡、过程凭证与完工证明</span>
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
            v-if="normalizeOrderStatus(order.status) === 'PENDING'"
            type="primary"
            @click="handleAction(order.id, 'accept')"
          >
            确认接单
          </el-button>
          <el-button
            v-if="normalizeOrderStatus(order.status) === 'CONFIRMED'"
            plain
            @click="openRecordDialog(order, 'CHECK_IN')"
          >
            上传上门打卡
          </el-button>
          <el-button
            v-if="normalizeOrderStatus(order.status) === 'CONFIRMED'"
            type="warning"
            @click="handleAction(order.id, 'start')"
          >
            开始服务
          </el-button>
          <el-button
            v-if="normalizeOrderStatus(order.status) === 'IN_SERVICE'"
            plain
            @click="openRecordDialog(order, 'SERVICE_PROOF')"
          >
            上传过程凭证
          </el-button>
          <el-button
            v-if="normalizeOrderStatus(order.status) === 'IN_SERVICE'"
            plain
            @click="openRecordDialog(order, 'FINISH_PROOF')"
          >
            上传完工凭证
          </el-button>
          <el-button
            v-if="normalizeOrderStatus(order.status) === 'IN_SERVICE'"
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
import { useClientPagination } from '../../composables/useClientPagination'
import {
  acceptWorkerOrder,
  completeWorkerOrder,
  fetchWorkerOrders,
  startWorkerOrder,
  uploadWorkerServiceRecord
} from '../../api'
import { getWorkerOrderFlowMeta, getOrderStepProgress, getOrderSteps } from '../../utils/orderFlow'
import { getOrderStatusLabel, getOrderStatusTagType, normalizeOrderStatus } from '../../utils/order'

const router = useRouter()
const statusFilter = ref('ALL')
const orders = ref([])
const loading = ref(false)
const orderSteps = getOrderSteps()

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

const summary = computed(() => ({
  total: orders.value.length,
  pending: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'PENDING').length,
  accepted: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'ACCEPTED').length,
  confirmed: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'CONFIRMED').length,
  waitingCompletion: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'WAITING_USER_CONFIRMATION').length
}))

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

const { currentPage, pageSize, pageSizes, total, pagedItems: pagedOrders, resetPage } = useClientPagination(filteredOrders, 6)

watch(statusFilter, () => resetPage())

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

function goToMessageCenter(orderId) {
  router.push({
    path: '/worker/messages',
    query: { orderId: String(orderId) }
  })
}

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await fetchWorkerOrders()
    resetPage()
  } catch (error) {
    ElMessage.error(error.message || '获取服务订单失败')
  } finally {
    loading.value = false
  }
}

async function handleAction(id, action) {
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

onMounted(loadOrders)
onBeforeUnmount(clearRecordFile)
</script>
