<template>
  <div class="page-stack">
    <div class="console-overview console-overview--worker">
      <div>
        <el-tag :type="qualificationTagType" round>{{ qualificationLabel }}</el-tag>
        <h1>{{ workerQualified ? '履约节奏一屏掌握' : '先完成资质认证，再开始接单' }}</h1>
        <p>{{ qualificationNotice.description }}</p>
      </div>
      <div class="hero-actions">
        <el-button v-if="workerQualified" type="primary" @click="router.push('/worker/orders')">进入订单列表</el-button>
        <el-button type="primary" plain @click="router.push('/worker/qualification')">
          {{ qualificationNotice.buttonText }}
        </el-button>
      </div>
    </div>

    <el-alert
      v-if="!workerQualified"
      :title="qualificationNotice.title"
      :description="qualificationNotice.description"
      :type="qualificationNotice.type"
      show-icon
      :closable="false"
    />

    <div v-if="workerQualified && todoItems.length" class="summary-grid">
      <el-card v-for="item in todoItems" :key="item.key" shadow="never" class="summary-card summary-card--todo">
        <el-statistic :title="item.label" :value="item.value" />
        <div class="muted-line">{{ item.hint }}</div>
      </el-card>
    </div>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="资质状态" :value="qualificationLabel" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="分配订单" :value="summary.total" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="待接单" :value="summary.pending" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="已完成" :value="summary.completed" />
      </el-card>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :xl="10">
        <el-card shadow="never" class="dashboard-card">
          <template #header><strong>订单状态结构</strong></template>
          <AppChart :option="statusChartOption" height="330px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="14">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <strong>{{ workerQualified ? '未来服务排期' : '接单前准备清单' }}</strong>
          </template>
          <AppChart v-if="workerQualified" :option="bookingChartOption" height="330px" />
          <div v-else class="dashboard-guide-list">
            <div class="info-panel">
              <span class="info-panel__label">第一步</span>
              <strong>完善服务信息</strong>
              <span class="muted-line">填写服务类型、服务区域、接单时段和个人介绍。</span>
            </div>
            <div class="info-panel">
              <span class="info-panel__label">第二步</span>
              <strong>上传资质文件</strong>
              <span class="muted-line">提交身份证明、职业培训或健康证明等审核材料。</span>
            </div>
            <div class="info-panel">
              <span class="info-panel__label">第三步</span>
              <strong>等待管理员审核</strong>
              <span class="muted-line">审核通过后，订单处理页会自动开放。</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :xl="12">
        <el-card shadow="never" class="dashboard-card">
          <template #header><strong>服务类型接单量</strong></template>
          <AppChart :option="serviceChartOption" height="300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="12">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <strong>{{ workerQualified ? '待优先处理订单' : '当前认证提示' }}</strong>
          </template>
          <el-table v-if="workerQualified" :data="priorityOrders" size="small">
            <el-table-column prop="serviceName" label="服务项目" min-width="120" />
            <el-table-column prop="customerName" label="联系人" width="100" />
            <el-table-column prop="bookingDate" label="日期" width="120" />
            <el-table-column prop="bookingSlot" label="时段" width="120" />
            <el-table-column prop="progressNote" label="当前进度" min-width="180" show-overflow-tooltip />
          </el-table>
          <div v-else class="dashboard-guide-list">
            <div class="info-panel">
              <span class="info-panel__label">当前状态</span>
              <strong>{{ qualificationNotice.title }}</strong>
              <span class="muted-line">{{ qualificationNotice.description }}</span>
            </div>
            <div class="hero-actions">
              <el-button type="primary" @click="router.push('/worker/qualification')">
                {{ qualificationNotice.buttonText }}
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="guideDialogVisible"
      width="560px"
      title="资质审核已通过"
      class="worker-guide-dialog"
      :close-on-click-modal="false"
    >
      <div class="dashboard-guide-list">
        <div class="info-panel">
          <span class="info-panel__label">现在可以做什么</span>
          <strong>开始接单并维护服务记录</strong>
          <span class="muted-line">你的服务信息已经对外展示，用户现在可以预约你提供的家政服务。</span>
        </div>
        <div class="info-panel">
          <span class="info-panel__label">建议顺序</span>
          <span class="muted-line">1. 先查看待接单订单 2. 开工前上传上门打卡 3. 服务中补充过程凭证。</span>
        </div>
        <div class="info-panel">
          <span class="info-panel__label">工作台提醒</span>
          <span class="muted-line">左侧订单入口和顶部待办角标会持续提示当前需要优先处理的事项。</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="dismissGuide(false)">稍后再看</el-button>
        <el-button type="primary" @click="dismissGuide(true)">去处理订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AppChart from '../../components/charts/AppChart.vue'
import { fetchCurrentWorkerProfile, fetchWorkerOrders, fetchWorkerOrderSummary } from '../../api'
import { authStore } from '../../stores/auth'
import {
  buildDateMap,
  buildOrderStatusSeriesData,
  buildServiceMap,
  mapToSortedRows
} from '../../utils/dashboard'
import { normalizeOrderStatus } from '../../utils/order'
import {
  getWorkerTodoItems,
  markWorkerApprovedGuideSeen,
  shouldShowWorkerApprovedGuide
} from '../../utils/workerConsole'
import {
  getWorkerQualificationLabel,
  getWorkerQualificationNotice,
  getWorkerQualificationTagType,
  isWorkerQualificationApproved,
  normalizeWorkerQualificationStatus
} from '../../utils/workerApplication'

const router = useRouter()
const loading = ref(false)
const orders = ref([])
const workerProfile = ref(null)
const orderSummary = ref({
  total: 0,
  pending: 0,
  accepted: 0,
  confirmed: 0,
  inService: 0,
  waitingUserConfirmation: 0,
  todo: 0
})
const guideDialogVisible = ref(false)

const qualificationStatus = computed(() => normalizeWorkerQualificationStatus(workerProfile.value?.qualificationStatus))
const workerQualified = computed(() => isWorkerQualificationApproved(qualificationStatus.value))
const qualificationLabel = computed(() => getWorkerQualificationLabel(qualificationStatus.value))
const qualificationTagType = computed(() => getWorkerQualificationTagType(qualificationStatus.value))
const qualificationNotice = computed(() => getWorkerQualificationNotice(qualificationStatus.value))
const todoItems = computed(() => getWorkerTodoItems(orderSummary.value))

const summary = computed(() => ({
  total: Number(orderSummary.value.total || orders.value.length || 0),
  pending: Number(orderSummary.value.pending || 0),
  inService: Number(orderSummary.value.inService || 0),
  completed: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'COMPLETED').length
}))

const priorityOrders = computed(() =>
  orders.value
    .filter((item) => {
      const status = normalizeOrderStatus(item.status)
      return status === 'PENDING' || status === 'ACCEPTED' || status === 'CONFIRMED' || status === 'WAITING_USER_CONFIRMATION'
    })
    .slice(0, 6)
)

const statusChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { color: '#6e6e73' } },
  color: ['#0071e3', '#2997ff', '#69b6ff', '#9ed0ff', '#cfe7ff', '#e8f3ff'],
  series: [
    {
      type: 'pie',
      radius: ['42%', '70%'],
      itemStyle: { borderRadius: 12, borderColor: '#fff', borderWidth: 4 },
      data: buildOrderStatusSeriesData(orders.value),
      label: { formatter: '{b}\n{c} 单', color: '#1d1d1f' }
    }
  ]
}))

const bookingChartOption = computed(() => {
  const rows = mapToSortedRows(buildDateMap(orders.value)).slice(0, 7).reverse()
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 16, top: 24, bottom: 36 },
    xAxis: {
      type: 'category',
      data: rows.map((item) => item.name),
      axisLabel: { interval: 0, rotate: 20, color: '#6e6e73' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: 'rgba(110, 110, 115, 0.12)' } },
      axisLabel: { color: '#6e6e73' }
    },
    series: [
      {
        type: 'line',
        smooth: true,
        symbolSize: 10,
        data: rows.map((item) => item.value),
        lineStyle: { width: 4, color: '#0071e3' },
        itemStyle: { color: '#0071e3' },
        areaStyle: { color: 'rgba(0, 113, 227, 0.16)' }
      }
    ]
  }
})

const serviceChartOption = computed(() => {
  const rows = mapToSortedRows(buildServiceMap(orders.value)).slice(0, 6)
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 46, right: 16, top: 20, bottom: 20 },
    xAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: 'rgba(110, 110, 115, 0.12)' } },
      axisLabel: { color: '#6e6e73' }
    },
    yAxis: {
      type: 'category',
      data: rows.map((item) => item.name),
      axisLabel: { color: '#6e6e73' }
    },
    series: [
      {
        type: 'bar',
        data: rows.map((item) => item.value),
        barWidth: 16,
        itemStyle: {
          borderRadius: [0, 10, 10, 0],
          color: '#0071e3'
        }
      }
    ]
  }
})

function tryOpenApprovedGuide() {
  const userId = authStore.state.user?.id
  if (!workerQualified.value || !shouldShowWorkerApprovedGuide(userId)) {
    return
  }
  guideDialogVisible.value = true
}

function dismissGuide(goOrders = false) {
  markWorkerApprovedGuideSeen(authStore.state.user?.id)
  guideDialogVisible.value = false
  if (goOrders) {
    router.push('/worker/orders')
  }
}

watch(workerQualified, (qualified) => {
  if (qualified) {
    tryOpenApprovedGuide()
  }
})

onMounted(async () => {
  loading.value = true
  try {
    const [profileResult, orderResult, summaryResult] = await Promise.all([
      fetchCurrentWorkerProfile().catch(() => null),
      fetchWorkerOrders().catch(() => []),
      fetchWorkerOrderSummary().catch(() => ({}))
    ])
    workerProfile.value = profileResult
    orders.value = Array.isArray(orderResult?.records) ? orderResult.records : orderResult
    orderSummary.value = {
      total: Number(summaryResult?.total || orders.value.length || 0),
      pending: Number(summaryResult?.pending || 0),
      accepted: Number(summaryResult?.accepted || 0),
      confirmed: Number(summaryResult?.confirmed || 0),
      inService: Number(summaryResult?.inService || 0),
      waitingUserConfirmation: Number(summaryResult?.waitingUserConfirmation || 0),
      todo: Number(summaryResult?.todo || 0)
    }
    tryOpenApprovedGuide()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard-card {
  background: rgba(255, 255, 255, 0.76);
  backdrop-filter: blur(18px);
}

.dashboard-guide-list {
  display: grid;
  gap: 14px;
}

.summary-card--todo :deep(.el-statistic__content) {
  color: #0071e3;
}
</style>
