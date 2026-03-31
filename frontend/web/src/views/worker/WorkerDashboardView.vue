<template>
  <div class="page-stack">
    <div class="console-overview console-overview--worker compact-overview">
      <div>
        <el-tag :type="qualificationTagType" round>{{ qualificationLabel }}</el-tag>
        <h1>{{ workerQualified ? '服务人员数据中心' : '请先完成资质认证' }}</h1>
        <p>{{ workerQualified ? '收入、订单和待办都集中在这里，处理效率会更高。' : qualificationNotice.description }}</p>
      </div>
      <div class="hero-actions">
        <el-button v-if="workerQualified" type="primary" @click="router.push('/worker/orders')">进入订单台</el-button>
        <el-button type="primary" plain @click="router.push('/worker/qualification')">
          {{ workerQualified ? '查看资质资料' : qualificationNotice.buttonText }}
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

    <div class="metric-strip metric-strip--dense">
      <div class="metric-chip">
        <span class="metric-chip__label">总订单</span>
        <strong>{{ dashboard.totalOrders }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待接单</span>
        <strong>{{ dashboard.pendingOrders }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">服务中</span>
        <strong>{{ dashboard.inServiceOrders }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待用户确认</span>
        <strong>{{ dashboard.waitingUserConfirmationOrders }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">累计营业额</span>
        <strong>{{ formatCurrency(dashboard.totalRevenue) }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">今日流水</span>
        <strong>{{ formatCurrency(dashboard.todayRevenue) }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">本月流水</span>
        <strong>{{ formatCurrency(dashboard.monthRevenue) }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">已支付订单</span>
        <strong>{{ dashboard.paidOrderCount }}</strong>
      </div>
    </div>

    <div v-if="workerQualified && todoItems.length" class="summary-grid">
      <el-card v-for="item in todoItems" :key="item.key" shadow="never" class="summary-card summary-card--todo">
        <div class="compact-stat">
          <strong>{{ item.value }}</strong>
          <span>{{ item.label }}</span>
        </div>
        <div class="muted-line">{{ item.hint }}</div>
      </el-card>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :xl="8">
        <el-card shadow="never" class="dashboard-card dashboard-card--compact">
          <template #header><strong>订单状态</strong></template>
          <AppChart :option="statusChartOption" height="240px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="8">
        <el-card shadow="never" class="dashboard-card dashboard-card--compact">
          <template #header><strong>近 7 天流水</strong></template>
          <AppChart :option="revenueChartOption" height="240px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="8">
        <el-card shadow="never" class="dashboard-card dashboard-card--compact">
          <template #header><strong>服务成交结构</strong></template>
          <AppChart :option="serviceChartOption" height="240px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :xl="9">
        <el-card shadow="never" class="dashboard-card dashboard-card--compact">
          <template #header><strong>{{ workerQualified ? '最近营业额流水' : '当前资质提醒' }}</strong></template>

          <div v-if="workerQualified" class="compact-flow-list">
            <div v-if="dashboard.recentPayments.length" v-for="item in dashboard.recentPayments" :key="item.paymentId" class="compact-flow-item">
              <div>
                <strong>{{ item.serviceName }}</strong>
                <div class="muted-line">{{ item.customerName }} · {{ item.paidAt || '待支付' }}</div>
              </div>
              <div class="compact-flow-item__right">
                <strong>{{ formatCurrency(item.amount) }}</strong>
                <span>{{ getPaymentMethodLabel(item.paymentMethod) }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无营业额流水" />
          </div>

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

      <el-col :xs="24" :xl="15">
        <el-card shadow="never" class="dashboard-card dashboard-card--compact">
          <template #header><strong>{{ workerQualified ? '工作重点' : '接单前准备' }}</strong></template>

          <div v-if="workerQualified" class="info-stack compact-info-stack">
            <div class="info-panel">
              <span class="info-panel__label">当前优先级</span>
              <strong>{{ primaryTodoTitle }}</strong>
              <span class="muted-line">{{ primaryTodoDescription }}</span>
            </div>
            <el-table :data="statusRows" size="small" stripe>
              <el-table-column prop="label" label="状态" min-width="140" />
              <el-table-column prop="count" label="订单数" width="90" />
              <el-table-column prop="hint" label="处理建议" min-width="220" show-overflow-tooltip />
            </el-table>
          </div>

          <div v-else class="dashboard-guide-list">
            <div class="info-panel">
              <span class="info-panel__label">第一步</span>
              <strong>完善服务信息</strong>
              <span class="muted-line">填写服务类型、服务区域、接单时段和个人简介。</span>
            </div>
            <div class="info-panel">
              <span class="info-panel__label">第二步</span>
              <strong>上传资质文件</strong>
              <span class="muted-line">提交身份证明、培训证明、健康证明等审核材料。</span>
            </div>
            <div class="info-panel">
              <span class="info-panel__label">第三步</span>
              <strong>等待管理员审核</strong>
              <span class="muted-line">审核通过后，订单入口会自动开放，并可直接从通知进入订单台。</span>
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
          <span class="muted-line">1. 先处理待接单订单 2. 开工前上传上门打卡 3. 服务中补充过程凭证。</span>
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
import { fetchWorkerDashboard } from '../../api'
import { authStore } from '../../stores/auth'
import { buildCompactDonutOption } from '../../utils/dashboard'
import { formatCurrency } from '../../utils/format'
import { getOrderStatusLabel } from '../../utils/order'
import { getPaymentMethodLabel } from '../../utils/payment'
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
const guideDialogVisible = ref(false)
const dashboard = ref({
  qualificationStatus: 'UNSUBMITTED',
  totalOrders: 0,
  pendingOrders: 0,
  confirmedOrders: 0,
  inServiceOrders: 0,
  waitingUserConfirmationOrders: 0,
  completedOrders: 0,
  todoOrders: 0,
  paidOrderCount: 0,
  totalRevenue: 0,
  todayRevenue: 0,
  monthRevenue: 0,
  serviceSales: {},
  statusDistribution: {},
  revenueTrend: [],
  recentPayments: []
})

const qualificationStatus = computed(() =>
  normalizeWorkerQualificationStatus(dashboard.value.qualificationStatus)
)
const workerQualified = computed(() => isWorkerQualificationApproved(qualificationStatus.value))
const qualificationLabel = computed(() => getWorkerQualificationLabel(qualificationStatus.value))
const qualificationTagType = computed(() => getWorkerQualificationTagType(qualificationStatus.value))
const qualificationNotice = computed(() => getWorkerQualificationNotice(qualificationStatus.value))

const todoItems = computed(() =>
  getWorkerTodoItems({
    pending: dashboard.value.pendingOrders,
    confirmed: dashboard.value.confirmedOrders,
    inService: dashboard.value.inServiceOrders,
    waitingUserConfirmation: dashboard.value.waitingUserConfirmationOrders
  }).filter((item) => item.value > 0)
)

const primaryTodoTitle = computed(() => {
  const firstPending = todoItems.value[0]
  return firstPending ? `${firstPending.label}优先处理` : '当前没有紧急待办'
})

const primaryTodoDescription = computed(() => {
  const firstPending = todoItems.value[0]
  return firstPending ? firstPending.hint : '可以先查看消息中心，或等待新的用户预约。'
})

const statusRows = computed(() => [
  {
    label: getOrderStatusLabel('PENDING'),
    count: Number(dashboard.value.pendingOrders || 0),
    hint: '尽快确认是否接单，减少用户等待时间。'
  },
  {
    label: getOrderStatusLabel('CONFIRMED'),
    count: Number(dashboard.value.confirmedOrders || 0),
    hint: '用户已确认预约，建议先安排上门时间。'
  },
  {
    label: getOrderStatusLabel('IN_SERVICE'),
    count: Number(dashboard.value.inServiceOrders || 0),
    hint: '及时补充服务过程记录和现场凭证。'
  },
  {
    label: getOrderStatusLabel('WAITING_USER_CONFIRMATION'),
    count: Number(dashboard.value.waitingUserConfirmationOrders || 0),
    hint: '用户确认完工后，这些订单会进入完成状态。'
  },
  {
    label: getOrderStatusLabel('COMPLETED'),
    count: Number(dashboard.value.completedOrders || 0),
    hint: '可继续关注评价和新增预约。'
  }
])

const statusChartOption = computed(() =>
  buildCompactDonutOption(
    Object.entries(dashboard.value.statusDistribution || {})
      .filter(([, value]) => Number(value) > 0)
      .map(([key, value]) => ({
        name: getOrderStatusLabel(key),
        value
      })),
    {
      centerLabel: '订单',
      unit: '单',
      centerX: '30%',
      colors: ['#0071e3', '#5e9eff', '#8cc0ff', '#b6d8ff', '#d5e9ff']
    }
  )
)

const revenueChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 42, right: 14, top: 20, bottom: 28 },
  xAxis: {
    type: 'category',
    data: dashboard.value.revenueTrend.map((item) => item.label),
    axisLabel: { color: '#6e6e73' }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: 'rgba(110, 110, 115, 0.12)' } },
    axisLabel: { color: '#6e6e73' }
  },
  series: [
    {
      type: 'line',
      smooth: true,
      data: dashboard.value.revenueTrend.map((item) => item.amount),
      lineStyle: { width: 3, color: '#0071e3' },
      itemStyle: { color: '#0071e3' },
      areaStyle: { color: 'rgba(0, 113, 227, 0.14)' }
    }
  ]
}))

const serviceChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 46, right: 12, top: 16, bottom: 16 },
  xAxis: {
    type: 'value',
    minInterval: 1,
    splitLine: { lineStyle: { color: 'rgba(110, 110, 115, 0.12)' } },
    axisLabel: { color: '#6e6e73' }
  },
  yAxis: {
    type: 'category',
    data: Object.keys(dashboard.value.serviceSales || {}).slice(0, 6),
    axisLabel: { color: '#6e6e73' }
  },
  series: [
    {
      type: 'bar',
      barWidth: 14,
      data: Object.values(dashboard.value.serviceSales || {}).slice(0, 6),
      itemStyle: {
        borderRadius: [0, 8, 8, 0],
        color: '#34aadc'
      }
    }
  ]
}))

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
    dashboard.value = await fetchWorkerDashboard()
    tryOpenApprovedGuide()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.compact-overview {
  padding-bottom: 18px;
}

.metric-strip--dense {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.dashboard-card--compact {
  background: rgba(255, 255, 255, 0.76);
  backdrop-filter: blur(18px);
}

.compact-flow-list {
  display: grid;
  gap: 12px;
}

.compact-flow-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid rgba(29, 29, 31, 0.08);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.62);
}

.compact-flow-item__right {
  display: grid;
  gap: 4px;
  justify-items: end;
  color: #6e6e73;
  font-size: 12px;
}

.compact-stat {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.compact-stat strong {
  font-size: 28px;
}

.compact-info-stack {
  gap: 12px;
}

@media (max-width: 1200px) {
  .metric-strip--dense {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .metric-strip--dense {
    grid-template-columns: 1fr;
  }

  .compact-flow-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .compact-flow-item__right {
    justify-items: start;
  }
}
</style>
