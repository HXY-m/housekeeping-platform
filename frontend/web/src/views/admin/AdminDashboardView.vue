<template>
  <div class="page-stack">
    <div class="console-overview console-overview--admin">
      <div>
        <el-tag type="danger" round>管理员中台</el-tag>
        <h1>平台运营态势总览</h1>
        <p>从订单规模、服务供给和满意度三个维度快速掌握平台运行情况。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="router.push('/admin/applications')">查看资质审核</el-button>
        <el-button plain @click="router.push('/admin/after-sales')">进入售后处理中台</el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="总订单量" :value="dashboard.totalOrders" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="已完成订单" :value="dashboard.completedOrders" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="活跃服务人员" :value="dashboard.activeWorkers" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="平均满意度" :value="dashboard.averageRating" :precision="2" />
      </el-card>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :xl="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-between">
              <strong>服务类型销量</strong>
              <span class="muted-line">识别平台主要成交品类</span>
            </div>
          </template>
          <AppChart :option="salesChartOption" height="360px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-between">
              <strong>订单完成率</strong>
              <span class="muted-line">观察履约效率</span>
            </div>
          </template>
          <AppChart :option="completionChartOption" height="360px" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="card-header-between">
          <strong>服务销量排行</strong>
          <span class="muted-line">用于运营排期与服务资源分配</span>
        </div>
      </template>
      <el-table :data="salesRows" stripe>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="name" label="服务类型" min-width="180" />
        <el-table-column prop="value" label="销量" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppChart from '../../components/charts/AppChart.vue'
import { fetchAdminDashboard } from '../../api'
import { mapToSortedRows } from '../../utils/dashboard'

const router = useRouter()
const dashboard = ref({
  totalOrders: 0,
  completedOrders: 0,
  activeWorkers: 0,
  averageRating: 0,
  serviceSales: {}
})

const salesRows = computed(() => mapToSortedRows(dashboard.value.serviceSales || {}))
const completionRate = computed(() => {
  if (!dashboard.value.totalOrders) {
    return 0
  }
  return Number(((dashboard.value.completedOrders / dashboard.value.totalOrders) * 100).toFixed(1))
})

const salesChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 44, right: 20, top: 20, bottom: 50 },
  xAxis: {
    type: 'category',
    data: salesRows.value.map((item) => item.name),
    axisLabel: { interval: 0, rotate: 16 }
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.18)' } }
  },
  series: [
    {
      type: 'bar',
      data: salesRows.value.map((item) => item.value),
      barWidth: 22,
      itemStyle: {
        borderRadius: [10, 10, 0, 0],
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#f97316' },
            { offset: 1, color: '#dc2626' }
          ]
        }
      }
    }
  ]
}))

const completionChartOption = computed(() => ({
  series: [
    {
      type: 'gauge',
      startAngle: 210,
      endAngle: -30,
      progress: { show: true, width: 18, itemStyle: { color: '#dc2626' } },
      axisLine: { lineStyle: { width: 18, color: [[1, '#e5e7eb']] } },
      pointer: { show: false },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      detail: {
        valueAnimation: true,
        formatter: '{value}%',
        fontSize: 36,
        color: '#111827',
        offsetCenter: [0, '10%']
      },
      title: {
        offsetCenter: [0, '52%'],
        fontSize: 14,
        color: '#6b7280'
      },
      data: [{ value: completionRate.value, name: '订单完成率' }]
    }
  ]
}))

onMounted(async () => {
  dashboard.value = await fetchAdminDashboard()
})
</script>
