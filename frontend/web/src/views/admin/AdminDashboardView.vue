<template>
  <div class="page-stack">
    <div class="console-overview console-overview--admin compact-overview">
      <div>
        <el-tag type="danger" round>运营总览</el-tag>
        <h1>平台数据中心</h1>
        <p>订单、营业额和最近流水都集中展示，便于快速判断平台运行状态。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="router.push('/admin/orders')">查看订单监管</el-button>
        <el-button plain @click="router.push('/admin/reports')">导出报表</el-button>
      </div>
    </div>

    <div class="metric-strip metric-strip--dense">
      <div class="metric-chip">
        <span class="metric-chip__label">总订单</span>
        <strong>{{ dashboard.totalOrders }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">已完成</span>
        <strong>{{ dashboard.completedOrders }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">已支付</span>
        <strong>{{ dashboard.paidOrders }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">活跃服务人员</span>
        <strong>{{ dashboard.activeWorkers }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">累计营业额</span>
        <strong>{{ formatCurrency(dashboard.totalRevenue) }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">今日营业额</span>
        <strong>{{ formatCurrency(dashboard.todayRevenue) }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">本月营业额</span>
        <strong>{{ formatCurrency(dashboard.monthRevenue) }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">平均评分</span>
        <strong>{{ dashboard.averageRating.toFixed(2) }}</strong>
      </div>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :xl="8">
        <el-card shadow="never" class="dashboard-card dashboard-card--compact">
          <template #header><strong>订单状态结构</strong></template>
          <AppChart :option="statusChartOption" height="240px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="8">
        <el-card shadow="never" class="dashboard-card dashboard-card--compact">
          <template #header><strong>近 7 天营业额</strong></template>
          <AppChart :option="revenueChartOption" height="240px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="8">
        <el-card shadow="never" class="dashboard-card dashboard-card--compact">
          <template #header><strong>服务销量排行</strong></template>
          <AppChart :option="salesChartOption" height="240px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :xl="10">
        <el-card shadow="never" class="dashboard-card dashboard-card--compact">
          <template #header><strong>最近营业额流水</strong></template>
          <div class="compact-flow-list">
            <div v-if="dashboard.recentPayments.length" v-for="item in dashboard.recentPayments" :key="item.paymentId" class="compact-flow-item">
              <div>
                <strong>{{ item.serviceName }}</strong>
                <div class="muted-line">{{ item.customerName }} · {{ item.workerName }}</div>
                <div class="muted-line">{{ item.paidAt || '待支付' }}</div>
              </div>
              <div class="compact-flow-item__right">
                <strong>{{ formatCurrency(item.amount) }}</strong>
                <span>{{ getPaymentMethodLabel(item.paymentMethod) }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无营业额流水" />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="14">
        <el-card shadow="never" class="dashboard-card dashboard-card--compact">
          <template #header><strong>运营摘要</strong></template>
          <div class="info-stack compact-info-stack">
            <div class="info-panel">
              <span class="info-panel__label">当前重点</span>
              <strong>{{ topServiceLabel }}</strong>
              <span class="muted-line">
                当前完成率 {{ completionRate }}%，本月累计营业额 {{ formatCurrency(dashboard.monthRevenue) }}。
              </span>
            </div>
            <el-table :data="statusRows" size="small" stripe>
              <el-table-column prop="label" label="状态" min-width="140" />
              <el-table-column prop="count" label="订单数" width="90" />
              <el-table-column prop="hint" label="运营建议" min-width="220" show-overflow-tooltip />
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppChart from '../../components/charts/AppChart.vue'
import { fetchAdminDashboard } from '../../api'
import { buildCompactDonutOption } from '../../utils/dashboard'
import { formatCurrency } from '../../utils/format'
import { getOrderStatusLabel } from '../../utils/order'
import { getPaymentMethodLabel } from '../../utils/payment'

const router = useRouter()
const loading = ref(false)
const dashboard = ref({
  totalOrders: 0,
  completedOrders: 0,
  activeWorkers: 0,
  averageRating: 0,
  paidOrders: 0,
  totalRevenue: 0,
  todayRevenue: 0,
  monthRevenue: 0,
  serviceSales: {},
  statusDistribution: {},
  revenueTrend: [],
  recentPayments: []
})

const completionRate = computed(() => {
  if (!dashboard.value.totalOrders) {
    return 0
  }
  return Number(((dashboard.value.completedOrders / dashboard.value.totalOrders) * 100).toFixed(1))
})

const topServiceLabel = computed(() => {
  const [name, value] = Object.entries(dashboard.value.serviceSales || {})[0] || []
  if (!name) {
    return '暂无成交数据'
  }
  return `${name}（${value} 单）`
})

const statusRows = computed(() =>
  Object.entries(dashboard.value.statusDistribution || {}).map(([key, value]) => ({
    label: getOrderStatusLabel(key),
    count: value,
    hint: getStatusHint(key)
  }))
)

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
      colors: ['#0071e3', '#5e9eff', '#8cc0ff', '#b6d8ff', '#d5e9ff', '#ebf3ff']
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

const salesChartOption = computed(() => ({
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
        color: '#5ac8fa'
      }
    }
  ]
}))

function getStatusHint(status) {
  if (status === 'PENDING') return '关注待接单订单，避免用户等待过长。'
  if (status === 'ACCEPTED') return '提醒用户尽快确认预约安排。'
  if (status === 'CONFIRMED') return '适合提前安排服务排期和人员调度。'
  if (status === 'IN_SERVICE') return '关注现场履约质量和过程记录。'
  if (status === 'WAITING_USER_CONFIRMATION') return '可提醒用户尽快确认完工。'
  if (status === 'COMPLETED') return '可继续观察评价与复购情况。'
  return '持续关注平台订单流转。'
}

onMounted(async () => {
  loading.value = true
  try {
    dashboard.value = await fetchAdminDashboard()
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
  background: rgba(255, 255, 255, 0.62);
  backdrop-filter: blur(22px);
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
