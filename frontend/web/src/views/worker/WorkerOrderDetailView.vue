<template>
  <div class="page-stack">
    <div v-if="!embedded" class="console-overview console-overview--worker">
      <div>
        <el-tag type="warning" round>订单详情</el-tag>
        <h1>{{ order?.serviceName || '订单详情' }}</h1>
        <p>在这里处理接单、开工、服务记录上传和完工提交。</p>
      </div>
      <div class="hero-actions">
        <el-button plain @click="goToConversation">订单沟通</el-button>
        <el-button @click="loadData">刷新详情</el-button>
      </div>
    </div>

    <div v-else class="card-header-between">
      <div>
        <strong>{{ order?.serviceName || '订单详情' }}</strong>
        <p class="section-caption">在这里处理接单、开工和服务记录上传。</p>
      </div>
      <div class="hero-actions">
        <el-button plain @click="goToConversation">订单沟通</el-button>
        <el-button @click="loadData">刷新</el-button>
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
          <el-button type="primary" @click="router.push('/worker/qualification')">
            {{ qualificationBanner.buttonText }}
          </el-button>
        </div>
      </template>
    </el-alert>

    <el-empty v-if="loading && !order" description="订单详情加载中" class="empty-surface" />

    <template v-else-if="order">
      <el-card shadow="never" class="order-card order-card--immersive">
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
              当前由{{ getWorkerOrderFlowMeta(order).owner }}推进
            </el-tag>
          </div>
        </div>

        <div class="order-card__meta-grid">
          <div class="order-meta-item">
            <span class="order-meta-item__label">预约时间</span>
            <strong>{{ formatBookingDateTime(order.bookingDate, order.bookingSlot) }}</strong>
          </div>
          <div class="order-meta-item">
            <span class="order-meta-item__label">联系方式</span>
            <strong>{{ order.customerName }} · {{ order.contactPhone }}</strong>
          </div>
          <div class="order-meta-item order-meta-item--wide">
            <span class="order-meta-item__label">服务地址</span>
            <strong>{{ order.serviceAddress || '--' }}</strong>
          </div>
          <div class="order-meta-item">
            <span class="order-meta-item__label">支付状态</span>
            <strong>{{ order.paymentStatus === 'PAID' ? '已支付' : '未支付' }}</strong>
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
              <span class="muted-line">查看你已上传的打卡、过程凭证和完工证明</span>
            </div>
          </template>
          <OrderServiceRecordTimeline :records="order.serviceRecords" />
        </el-card>

        <div class="order-card__extra-grid">
          <div v-if="order.remark" class="info-panel">
            <span class="info-panel__label">用户备注</span>
            <span class="muted-line">{{ order.remark }}</span>
          </div>
          <div v-if="order.reviewed" class="info-panel">
            <span class="info-panel__label">用户评价</span>
            <el-rate :model-value="order.reviewRating" disabled />
            <span class="muted-line">{{ order.reviewContent || '--' }}</span>
          </div>
        </div>

        <div class="order-card__actions">
          <el-button
            v-if="workerQualified && normalizeOrderStatus(order.status) === 'PENDING'"
            type="primary"
            @click="handleAction('accept')"
          >
            确认接单
          </el-button>
          <el-button
            v-if="workerQualified && normalizeOrderStatus(order.status) === 'CONFIRMED'"
            plain
            @click="openRecordDialog('CHECK_IN')"
          >
            上传上门打卡
          </el-button>
          <el-button
            v-if="workerQualified && normalizeOrderStatus(order.status) === 'CONFIRMED'"
            type="warning"
            @click="handleAction('start')"
          >
            开始服务
          </el-button>
          <el-button
            v-if="workerQualified && normalizeOrderStatus(order.status) === 'IN_SERVICE'"
            plain
            @click="openRecordDialog('SERVICE_PROOF')"
          >
            上传过程凭证
          </el-button>
          <el-button
            v-if="workerQualified && normalizeOrderStatus(order.status) === 'IN_SERVICE'"
            plain
            @click="openRecordDialog('FINISH_PROOF')"
          >
            上传完工凭证
          </el-button>
          <el-button
            v-if="workerQualified && normalizeOrderStatus(order.status) === 'IN_SERVICE'"
            type="success"
            @click="handleAction('complete')"
          >
            提交完工
          </el-button>
        </div>
      </el-card>
    </template>

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
            <span v-else class="muted-line">可以不上传图片，但建议关键节点保留现场凭证。</span>
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
        <el-button type="primary" :loading="submittingRecord" @click="submitRecord">上传记录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import OrderServiceRecordTimeline from '../../components/common/OrderServiceRecordTimeline.vue'
import {
  acceptWorkerOrder,
  completeWorkerOrder,
  fetchCurrentWorkerProfile,
  fetchWorkerOrderDetail,
  startWorkerOrder,
  uploadWorkerServiceRecord
} from '../../api'
import { formatBookingDateTime } from '../../utils/bookingSlots'
import { getOrderStepProgress, getOrderSteps, getWorkerOrderFlowMeta } from '../../utils/orderFlow'
import { getOrderStatusLabel, getOrderStatusTagType, normalizeOrderStatus } from '../../utils/order'
import { buildConversationPath } from '../../utils/orderNavigation'
import { getWorkerQualificationNotice, isWorkerQualificationApproved } from '../../utils/workerApplication'

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
const workerProfile = ref(null)
const orderSteps = getOrderSteps()

const recordDialogVisible = ref(false)
const submittingRecord = ref(false)
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

const resolvedOrderId = computed(() => String(props.orderId || route.params.id || ''))
const workerQualified = computed(() => isWorkerQualificationApproved(workerProfile.value?.qualificationStatus))
const qualificationBanner = computed(() => getWorkerQualificationNotice(workerProfile.value?.qualificationStatus))

const availableRecordStages = computed(() => {
  const status = normalizeOrderStatus(order.value?.status)
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

async function loadData() {
  if (!resolvedOrderId.value) {
    order.value = null
    return
  }

  loading.value = true
  try {
    const [profileResult, orderResult] = await Promise.all([
      fetchCurrentWorkerProfile().catch(() => null),
      fetchWorkerOrderDetail(resolvedOrderId.value)
    ])
    workerProfile.value = profileResult
    order.value = orderResult
  } catch (error) {
    ElMessage.error(error.message || '获取订单详情失败')
  } finally {
    loading.value = false
  }
}

function goToConversation() {
  if (!workerQualified.value) {
    ElMessage.warning('资质审核通过后才能进入订单沟通')
    router.push('/worker/qualification')
    return
  }
  router.push(buildConversationPath('worker', resolvedOrderId.value))
}

async function handleAction(action) {
  if (!workerQualified.value) {
    ElMessage.warning('资质审核通过后才能处理订单')
    router.push('/worker/qualification')
    return
  }
  try {
    if (action === 'accept') {
      await acceptWorkerOrder(resolvedOrderId.value)
    } else if (action === 'start') {
      await startWorkerOrder(resolvedOrderId.value)
    } else {
      await completeWorkerOrder(resolvedOrderId.value)
    }
    ElMessage.success('订单状态已更新')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '订单流转失败')
  }
}

function openRecordDialog(stage) {
  if (!workerQualified.value) {
    ElMessage.warning('资质审核通过后才能上传服务记录')
    router.push('/worker/qualification')
    return
  }
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
    await uploadWorkerServiceRecord(resolvedOrderId.value, {
      stage: recordForm.stage,
      description: recordForm.description.trim(),
      file: recordForm.file
    })
    ElMessage.success('服务记录上传成功')
    closeRecordDialog()
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '服务记录上传失败')
  } finally {
    submittingRecord.value = false
  }
}

watch(resolvedOrderId, () => {
  loadData()
}, { immediate: true })

onMounted(loadData)
onBeforeUnmount(clearRecordFile)
</script>
