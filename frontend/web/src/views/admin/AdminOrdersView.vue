<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">订单监管</h1>
          <p class="page-panel__desc">
            统一查看平台订单状态、预约时间、履约进度和评价结果。筛选条件集中收口，避免重复的信息卡片。
          </p>
        </div>
        <el-button type="primary" :loading="loading" @click="loadOrders">刷新数据</el-button>
      </div>

      <div class="metric-strip">
        <div class="metric-chip">
          <span class="metric-chip__label">平台订单</span>
          <strong>{{ summary.total }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">待接单</span>
          <strong>{{ summary.pending }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">服务中</span>
          <strong>{{ summary.inService }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">已完成</span>
          <strong>{{ summary.completed }}</strong>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <div class="table-toolbar">
        <div class="table-toolbar__filters">
          <el-input
            v-model.trim="filters.keyword"
            clearable
            placeholder="搜索服务项目、服务人员、用户姓名"
            style="width: 280px"
          />
          <el-select v-model="filters.status" clearable placeholder="订单状态" style="width: 160px">
            <el-option label="待接单" value="PENDING" />
            <el-option label="已接单" value="ACCEPTED" />
            <el-option label="服务中" value="IN_SERVICE" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            start-placeholder="预约开始日期"
            end-placeholder="预约结束日期"
            value-format="YYYY-MM-DD"
            unlink-panels
          />
        </div>
        <span class="section-caption">按预约日期和状态快速定位问题订单</span>
      </div>

      <el-table :data="filteredOrders" v-loading="loading" stripe style="margin-top: 16px">
        <el-table-column label="订单信息" min-width="200">
          <template #default="{ row }">
            <div class="table-cell-primary">
              <strong>{{ row.serviceName }}</strong>
              <span class="table-cell-secondary">订单号 #{{ row.id }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户 / 服务人员" min-width="180">
          <template #default="{ row }">
            <div class="table-cell-primary">
              <strong>{{ row.customerName }}</strong>
              <span class="table-cell-secondary">{{ row.workerName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="预约时间" min-width="180">
          <template #default="{ row }">
            <div class="table-cell-primary">
              <strong>{{ row.bookingDate }}</strong>
              <span class="table-cell-secondary">{{ row.bookingSlot }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="serviceAddress" label="服务地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusTagType(row.status)">
              {{ getOrderStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前推进" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="table-cell-primary">
              <strong>{{ getUserOrderFlowMeta(row).title }}</strong>
              <span class="table-cell-secondary">{{ row.progressNote || '暂无进度说明' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评价结果" min-width="180">
          <template #default="{ row }">
            <div class="table-cell-primary">
              <strong>{{ row.reviewed ? `${row.reviewRating} 星` : '未评价' }}</strong>
              <span class="table-cell-secondary">{{ row.reviewContent || '暂无评价内容' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="订单备注" min-width="180" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminOrders } from '../../api'
import { getUserOrderFlowMeta } from '../../utils/orderFlow'
import { getOrderStatusLabel, getOrderStatusTagType, normalizeOrderStatus } from '../../utils/order'

const loading = ref(false)
const orders = ref([])
const filters = reactive({
  keyword: '',
  status: '',
  dateRange: []
})

const summary = computed(() => ({
  total: orders.value.length,
  pending: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'PENDING').length,
  inService: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'IN_SERVICE').length,
  completed: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'COMPLETED').length
}))

const filteredOrders = computed(() =>
  orders.value.filter((item) => {
    if (filters.status && normalizeOrderStatus(item.status) !== filters.status) {
      return false
    }

    if (filters.dateRange?.length === 2) {
      const [start, end] = filters.dateRange
      if (item.bookingDate < start || item.bookingDate > end) {
        return false
      }
    }

    if (!filters.keyword) {
      return true
    }

    const keyword = filters.keyword.toLowerCase()
    return [item.serviceName, item.workerName, item.customerName]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(keyword))
  })
)

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await fetchAdminOrders()
  } catch (error) {
    ElMessage.error(error.message || '获取平台订单失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadOrders)
</script>
