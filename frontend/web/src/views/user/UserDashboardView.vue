<template>
  <div class="page-stack">
    <div class="console-overview">
      <div>
        <el-tag type="info" round>用户中心</el-tag>
        <h1>我的家政服务</h1>
        <p>快速查看订单进度、售后状态和常用服务人员，所有状态都以中文统一展示。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="router.push('/workers')">继续找服务</el-button>
        <el-button plain @click="router.push('/user/profile')">资料与地址</el-button>
        <el-button plain @click="router.push('/user/orders')">全部订单</el-button>
      </div>
    </div>

    <div class="summary-grid summary-grid--five">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="累计订单" :value="summary.totalOrders" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="处理中订单" :value="summary.activeOrders" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="已完成订单" :value="summary.completedOrders" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="售后工单" :value="summary.afterSaleCount" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="收藏服务人员" :value="summary.favoriteCount" />
      </el-card>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :xl="14">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <div class="card-header-between">
              <strong>订单状态分布</strong>
              <span class="muted-line">帮助你快速判断当前履约阶段</span>
            </div>
          </template>
          <AppChart :option="statusChartOption" height="340px" />
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="10">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <div class="card-header-between">
              <strong>服务偏好</strong>
              <span class="muted-line">最近订单中的服务类型统计</span>
            </div>
          </template>
          <AppChart :option="serviceChartOption" height="340px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :xl="12">
        <el-card shadow="never" class="dashboard-card">
          <template #header><strong>售后处理状态</strong></template>
          <AppChart :option="afterSaleChartOption" height="300px" />
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="12">
        <el-card shadow="never" class="dashboard-card">
          <template #header><strong>近期订单提醒</strong></template>
          <el-timeline v-if="recentOrders.length">
            <el-timeline-item
              v-for="order in recentOrders"
              :key="order.id"
              :timestamp="`${order.bookingDate} ${order.bookingSlot}`"
            >
              <div class="timeline-title">{{ order.serviceName }}</div>
              <p>{{ order.workerName }} / {{ order.serviceAddress }}</p>
              <p class="muted-line">{{ order.progressNote }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="还没有预约记录" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppChart from '../../components/charts/AppChart.vue'
import { fetchFavoriteWorkers, fetchMyAfterSales, fetchOrders } from '../../api'
import {
  buildAfterSaleStatusMap,
  buildOrderStatusSeriesData,
  buildServiceMap,
  mapToSeriesData,
  mapToSortedRows
} from '../../utils/dashboard'
import { getAfterSaleStatusLabel } from '../../utils/afterSale'
import { normalizeOrderStatus } from '../../utils/order'

const router = useRouter()
const loading = ref(false)
const orders = ref([])
const afterSales = ref([])
const favorites = ref([])

const summary = computed(() => ({
  totalOrders: orders.value.length,
  activeOrders: orders.value.filter((item) => {
    const status = normalizeOrderStatus(item.status)
    return status !== 'COMPLETED'
  }).length,
  completedOrders: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'COMPLETED').length,
  afterSaleCount: afterSales.value.length,
  favoriteCount: favorites.value.length
}))

const recentOrders = computed(() => orders.value.slice(0, 5))

const statusChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { color: '#6e6e73' } },
  color: ['#0071e3', '#2997ff', '#69b6ff', '#9ed0ff', '#cfe7ff', '#e8f3ff'],
  series: [
    {
      type: 'pie',
      radius: ['44%', '70%'],
      itemStyle: { borderRadius: 14, borderColor: '#fff', borderWidth: 4 },
      data: buildOrderStatusSeriesData(orders.value),
      label: { formatter: '{b}\n{c} 单', color: '#1d1d1f' }
    }
  ]
}))

const serviceChartOption = computed(() => {
  const rows = mapToSortedRows(buildServiceMap(orders.value)).slice(0, 6)
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 20, bottom: 20 },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(110, 110, 115, 0.12)' } }
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
        barWidth: 18,
        itemStyle: {
          borderRadius: [0, 10, 10, 0],
          color: '#0071e3'
        }
      }
    ]
  }
})

const afterSaleChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { color: '#6e6e73' } },
  color: ['#0071e3', '#5ac8fa', '#a1a1a6', '#ff9f0a'],
  series: [
    {
      type: 'pie',
      radius: '68%',
      data: mapToSeriesData(buildAfterSaleStatusMap(afterSales.value), {
        PENDING: getAfterSaleStatusLabel('PENDING'),
        PROCESSING: getAfterSaleStatusLabel('PROCESSING'),
        RESOLVED: getAfterSaleStatusLabel('RESOLVED'),
        REJECTED: getAfterSaleStatusLabel('REJECTED')
      }),
      label: { formatter: '{b}\n{c} 条', color: '#1d1d1f' }
    }
  ]
}))

onMounted(async () => {
  loading.value = true
  try {
    const [orderResult, afterSaleResult, favoriteResult] = await Promise.all([
      fetchOrders(),
      fetchMyAfterSales(),
      fetchFavoriteWorkers()
    ])
    orders.value = orderResult
    afterSales.value = afterSaleResult
    favorites.value = favoriteResult
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.summary-grid--five {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.dashboard-card {
  background: rgba(255, 255, 255, 0.76);
  backdrop-filter: blur(18px);
}

@media (max-width: 1280px) {
  .summary-grid--five {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .summary-grid--five {
    grid-template-columns: 1fr;
  }
}
</style>
