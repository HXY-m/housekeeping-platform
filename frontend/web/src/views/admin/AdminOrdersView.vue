<template>
  <div class="page-stack">
    <div class="console-overview console-overview--admin">
      <div>
        <el-tag type="danger" round>订单监管</el-tag>
        <h1>平台订单总览</h1>
        <p>统一查看平台订单状态、预约信息、评价结果与最新履约进度。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="loadOrders">刷新数据</el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="平台订单" :value="summary.total" />
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

    <el-card shadow="never">
      <div class="filter-row">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索服务项目 / 服务人员 / 用户姓名"
          style="width: 320px"
        />
        <el-select v-model="filters.status" clearable placeholder="筛选订单状态" style="width: 180px">
          <el-option label="待接单" value="PENDING" />
          <el-option label="已接单" value="ACCEPTED" />
          <el-option label="服务中" value="IN_SERVICE" />
          <el-option label="已完成" value="COMPLETED" />
        </el-select>
      </div>

      <el-table :data="filteredOrders" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单号" width="90" />
        <el-table-column prop="serviceName" label="服务项目" min-width="140" />
        <el-table-column prop="workerName" label="服务人员" width="120" />
        <el-table-column prop="customerName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column label="预约时间" min-width="180">
          <template #default="{ row }">
            <div class="stack-cell">
              <span>{{ row.bookingDate }}</span>
              <span class="muted-line">{{ row.bookingSlot }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="serviceAddress" label="服务地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusTagType(row.status)">
              {{ getOrderStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progressNote" label="最新进度" min-width="220" show-overflow-tooltip />
        <el-table-column label="评价结果" min-width="180">
          <template #default="{ row }">
            <div class="stack-cell">
              <span>{{ row.reviewed ? `已评价 ${row.reviewRating} 分` : '未评价' }}</span>
              <span v-if="row.reviewContent" class="muted-line">{{ row.reviewContent }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="订单备注" min-width="220" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminOrders } from '../../api'
import { getOrderStatusLabel, getOrderStatusTagType, normalizeOrderStatus } from '../../utils/order'

const loading = ref(false)
const orders = ref([])
const filters = reactive({
  keyword: '',
  status: ''
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
    if (!filters.keyword) {
      return true
    }
    const keyword = filters.keyword.trim().toLowerCase()
    return [item.serviceName, item.workerName, item.customerName]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(keyword))
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
