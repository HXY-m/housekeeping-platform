<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">运营总览</h1>
          <p class="page-panel__desc">
            聚合订单规模、服务供给和满意度，帮助管理员快速判断当前平台负载和待处理重点。
          </p>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="router.push('/admin/applications')">去资质审核</el-button>
          <el-button plain @click="router.push('/admin/after-sales')">去售后处理</el-button>
        </div>
      </div>

      <div class="metric-strip">
        <div class="metric-chip">
          <span class="metric-chip__label">总订单量</span>
          <strong>{{ dashboard.totalOrders }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">已完成订单</span>
          <strong>{{ dashboard.completedOrders }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">活跃服务人员</span>
          <strong>{{ dashboard.activeWorkers }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">平均满意度</span>
          <strong>{{ dashboard.averageRating.toFixed(2) }}</strong>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :xl="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-between">
              <strong>服务销量分布</strong>
              <span class="section-caption">识别当前主要成交服务类型</span>
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
              <span class="section-caption">观察整体履约效率</span>
            </div>
          </template>
          <AppChart :option="completionChartOption" height="360px" />
        </el-card>
      </el-col>
    </el-row>

    <div class="page-grid--sidebar">
      <el-card shadow="never">
        <template #header>
          <div class="card-header-between">
            <strong>服务销量排行</strong>
            <span class="section-caption">用于运营排期和资源调度</span>
          </div>
        </template>
        <el-table :data="pagedSalesRows" stripe>
          <el-table-column type="index" label="#" width="60" />
          <el-table-column prop="name" label="服务类型" min-width="180" />
          <el-table-column prop="value" label="销量" width="120" />
        </el-table>
        <ListPagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="pageSizes"
          :total="total"
          small
        />
      </el-card>

      <el-card shadow="never">
        <template #header>
          <strong>运营提示</strong>
        </template>
        <div class="info-stack">
          <div class="info-panel">
            <span class="info-panel__label">当前重点</span>
            <strong>{{ topServiceLabel }}</strong>
            <span class="muted-line">
              {{ completionRate }}% 的订单已完成，{{ dashboard.activeWorkers }} 名服务人员处于活跃状态。
            </span>
          </div>
          <div class="info-panel">
            <span class="info-panel__label">建议跟进</span>
            <span class="muted-line">
              若待审核申请或售后工单偏多，建议优先进入对应后台页面处理，避免服务供给或用户体验积压。
            </span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppChart from '../../components/charts/AppChart.vue'
import ListPagination from '../../components/common/ListPagination.vue'
import { useClientPagination } from '../../composables/useClientPagination'
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
const { currentPage, pageSize, pageSizes, total, pagedItems: pagedSalesRows } = useClientPagination(salesRows, 5)

const completionRate = computed(() => {
  if (!dashboard.value.totalOrders) {
    return 0
  }
  return Number(((dashboard.value.completedOrders / dashboard.value.totalOrders) * 100).toFixed(1))
})

const topServiceLabel = computed(() => {
  if (!salesRows.value.length) {
    return '暂无成交数据'
  }
  return `${salesRows.value[0].name}（${salesRows.value[0].value} 单）`
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
