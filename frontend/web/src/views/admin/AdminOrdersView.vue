<template>
  <div class="page-stack">
    <div class="console-overview console-overview--admin">
      <div>
        <el-tag type="danger" round>订单监管</el-tag>
        <h1>平台订单与履约进度</h1>
        <p>集中查看服务预约、履约推进、用户评价和售后风险，便于快速定位异常订单。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" :loading="loading" @click="loadOrders">刷新数据</el-button>
      </div>
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
        <span class="metric-chip__label">已支付</span>
        <strong>{{ summary.paid }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">已完成</span>
        <strong>{{ summary.completed }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">支付金额</span>
        <strong>{{ formatCurrency(summary.paidAmount || 0) }}</strong>
      </div>
    </div>

    <el-card shadow="never" class="page-panel">
      <div class="card-header-between">
        <div>
          <strong>筛选与列表</strong>
          <p class="section-caption">按服务、用户、服务人员、状态和预约日期筛选。</p>
        </div>
      </div>

      <div class="table-toolbar table-toolbar--dense">
        <div class="table-toolbar__filters">
          <el-input
            v-model.trim="filters.keyword"
            clearable
            placeholder="搜索服务、用户或服务人员"
            style="width: 280px"
          />
          <el-select v-model="filters.status" clearable placeholder="订单状态" style="width: 180px">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
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
      </div>

      <el-table :data="orders" v-loading="loading" stripe style="margin-top: 16px">
        <el-table-column label="订单信息" min-width="200">
          <template #default="{ row }">
            <div class="table-cell-primary">
              <strong>{{ row.serviceName }}</strong>
              <span class="table-cell-secondary">订单号 #{{ row.id }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户 / 服务人员" min-width="190">
          <template #default="{ row }">
            <div class="table-cell-primary">
              <strong>{{ row.customerName }}</strong>
              <span class="table-cell-secondary">{{ row.workerName || '待分配服务人员' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="预约时间" min-width="190">
          <template #default="{ row }">
            <div class="table-cell-primary">
              <strong>{{ row.bookingDate }}</strong>
              <span class="table-cell-secondary">{{ row.bookingSlot }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="serviceAddress" label="服务地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="订单状态" width="130">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusTagType(row.status)">
              {{ getOrderStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付信息" min-width="170">
          <template #default="{ row }">
            <div class="table-cell-primary">
              <strong>{{ formatCurrency(row.payableAmount || 0) }}</strong>
              <span class="table-cell-secondary">
                {{ getPaymentStatusLabel(row.paymentStatus) }} · {{ getPaymentMethodLabel(row.paymentMethod) }}
              </span>
            </div>
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

      <ListPagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizes"
        :total="total"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminOrders, fetchAdminOrderSummary } from '../../api'
import ListPagination from '../../components/common/ListPagination.vue'
import { useServerPagination } from '../../composables/useServerPagination'
import { formatCurrency } from '../../utils/format'
import { getUserOrderFlowMeta } from '../../utils/orderFlow'
import { getOrderStatusLabel, getOrderStatusTagType } from '../../utils/order'
import { getPaymentMethodLabel, getPaymentStatusLabel } from '../../utils/payment'

const statusOptions = [
  { label: '待接单', value: 'PENDING' },
  { label: '待用户确认', value: 'ACCEPTED' },
  { label: '待上门', value: 'CONFIRMED' },
  { label: '服务中', value: 'IN_SERVICE' },
  { label: '待确认完工', value: 'WAITING_USER_CONFIRMATION' },
  { label: '已完成', value: 'COMPLETED' }
]

const loading = ref(false)
const orders = ref([])
const summary = ref({
  total: 0,
  pending: 0,
  inService: 0,
  completed: 0,
  paid: 0,
  paidAmount: 0
})
const filters = reactive({
  keyword: '',
  status: '',
  dateRange: []
})

const { currentPage, pageSize, pageSizes, total, buildParams, applyPageResult, resetPage } = useServerPagination(8)

function buildFilters() {
  return {
    keyword: filters.keyword.trim(),
    status: filters.status,
    dateFrom: filters.dateRange?.[0] || '',
    dateTo: filters.dateRange?.[1] || ''
  }
}

async function loadOrders() {
  loading.value = true
  try {
    const params = buildFilters()
    const [result, summaryResult] = await Promise.all([
      fetchAdminOrders(buildParams(params)),
      fetchAdminOrderSummary(params)
    ])
    orders.value = applyPageResult(result)
    summary.value = {
      total: Number(summaryResult?.total || 0),
      pending: Number(summaryResult?.pending || 0),
      inService: Number(summaryResult?.inService || 0),
      completed: Number(summaryResult?.completed || 0),
      paid: Number(summaryResult?.paid || 0),
      paidAmount: Number(summaryResult?.paidAmount || 0)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取平台订单失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => [filters.keyword, filters.status, filters.dateRange?.join('|')],
  () => {
    if (currentPage.value !== 1) {
      resetPage()
      return
    }
    loadOrders()
  }
)

watch([currentPage, pageSize], () => {
  loadOrders()
})

onMounted(loadOrders)
</script>
