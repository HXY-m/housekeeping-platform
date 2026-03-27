<template>
  <div class="page-stack">
    <div class="console-overview console-overview--worker">
      <div>
        <el-tag type="warning" round>服务人员工作台</el-tag>
        <h1>履约节奏一屏掌握</h1>
        <p>优先处理待接单和待上门服务，保障每天的履约效率与客户体验。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="router.push('/worker/orders')">进入订单列表</el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="分配订单" :value="summary.total" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="待接单" :value="summary.pending" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="服务中" :value="summary.inService" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="已完成" :value="summary.completed" />
      </el-card>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :xl="10">
        <el-card shadow="never">
          <template #header><strong>订单状态结构</strong></template>
          <AppChart :option="statusChartOption" height="330px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="14">
        <el-card shadow="never">
          <template #header><strong>未来服务排期</strong></template>
          <AppChart :option="bookingChartOption" height="330px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :xl="12">
        <el-card shadow="never">
          <template #header><strong>服务类型接单量</strong></template>
          <AppChart :option="serviceChartOption" height="300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="12">
        <el-card shadow="never">
          <template #header><strong>待优先处理订单</strong></template>
          <el-table :data="priorityOrders" size="small">
            <el-table-column prop="serviceName" label="服务项目" min-width="120" />
            <el-table-column prop="customerName" label="联系人" width="100" />
            <el-table-column prop="bookingDate" label="日期" width="120" />
            <el-table-column prop="bookingSlot" label="时间段" width="120" />
            <el-table-column prop="progressNote" label="当前进度" min-width="180" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppChart from '../../components/charts/AppChart.vue'
import { fetchWorkerOrders } from '../../api'
import {
  buildDateMap,
  buildOrderStatusMap,
  buildServiceMap,
  mapToSeriesData,
  mapToSortedRows
} from '../../utils/dashboard'
import { getOrderStatusLabel, normalizeOrderStatus } from '../../utils/order'

const router = useRouter()
const loading = ref(false)
const orders = ref([])

const summary = computed(() => ({
  total: orders.value.length,
  pending: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'PENDING').length,
  inService: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'IN_SERVICE').length,
  completed: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'COMPLETED').length
}))

const priorityOrders = computed(() =>
  orders.value.filter((item) => {
    const status = normalizeOrderStatus(item.status)
    return status === 'PENDING' || status === 'ACCEPTED' || status === 'IN_SERVICE'
  }).slice(0, 6)
)

const statusChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      type: 'pie',
      radius: ['42%', '70%'],
      itemStyle: { borderRadius: 12, borderColor: '#fff', borderWidth: 4 },
      data: mapToSeriesData(buildOrderStatusMap(orders.value), {
        PENDING: getOrderStatusLabel('PENDING'),
        ACCEPTED: getOrderStatusLabel('ACCEPTED'),
        IN_SERVICE: getOrderStatusLabel('IN_SERVICE'),
        COMPLETED: getOrderStatusLabel('COMPLETED')
      }),
      label: { formatter: '{b}\n{c} 单' }
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
      axisLabel: { interval: 0, rotate: 20 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.18)' } }
    },
    series: [
      {
        type: 'line',
        smooth: true,
        symbolSize: 10,
        data: rows.map((item) => item.value),
        lineStyle: { width: 4, color: '#f59e0b' },
        itemStyle: { color: '#f59e0b' },
        areaStyle: { color: 'rgba(245, 158, 11, 0.18)' }
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
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.18)' } }
    },
    yAxis: {
      type: 'category',
      data: rows.map((item) => item.name)
    },
    series: [
      {
        type: 'bar',
        data: rows.map((item) => item.value),
        barWidth: 16,
        itemStyle: {
          borderRadius: [0, 10, 10, 0],
          color: '#0f766e'
        }
      }
    ]
  }
})

onMounted(async () => {
  loading.value = true
  try {
    orders.value = await fetchWorkerOrders()
  } finally {
    loading.value = false
  }
})
</script>
