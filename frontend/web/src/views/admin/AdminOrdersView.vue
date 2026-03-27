<template>
  <div class="page-stack">
    <div class="console-overview console-overview--admin">
      <div>
        <el-tag type="danger" round>订单监管</el-tag>
        <h1>平台订单与履约总览</h1>
        <p>统一查看支付状态、履约进度和服务过程留痕，便于运营排查异常订单。</p>
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
        <el-statistic title="待支付订单" :value="summary.unpaid" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="服务中订单" :value="summary.inService" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="已完工订单" :value="summary.completed" />
      </el-card>
    </div>

    <el-card shadow="never">
      <div class="filter-row">
        <el-select v-model="filters.status" clearable placeholder="筛选订单状态" style="width: 180px">
          <el-option label="待支付/待接单" value="PENDING" />
          <el-option label="已接单" value="ACCEPTED" />
          <el-option label="服务中" value="IN_SERVICE" />
          <el-option label="已完成" value="COMPLETED" />
        </el-select>
        <el-select
          v-model="filters.paymentStatus"
          clearable
          placeholder="筛选支付状态"
          style="width: 180px"
        >
          <el-option label="待支付" value="UNPAID" />
          <el-option label="已支付" value="PAID" />
        </el-select>
      </div>

      <el-table :data="filteredOrders" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单号" width="90" />
        <el-table-column prop="serviceName" label="服务项目" min-width="140" />
        <el-table-column prop="workerName" label="服务人员" width="120" />
        <el-table-column prop="customerName" label="用户联系人" width="120" />
        <el-table-column label="预约信息" min-width="180">
          <template #default="{ row }">
            <div class="stack-cell">
              <span>{{ row.bookingDate }}</span>
              <span class="muted-line">{{ row.bookingSlot }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="费用与支付" min-width="220">
          <template #default="{ row }">
            <div class="stack-cell">
              <strong>{{ formatCurrency(row.finalAmount ?? row.estimatedAmount) }}</strong>
              <div class="tag-group">
                <el-tag :type="getPaymentStatusTagType(row.paymentStatus)">
                  {{ getPaymentStatusLabel(row.paymentStatus) }}
                </el-tag>
                <el-tag v-if="row.paymentMethod" type="info">
                  {{ getPaymentMethodLabel(row.paymentMethod) }}
                </el-tag>
              </div>
              <span v-if="row.paidAt" class="muted-line">
                {{ formatDateTime(row.paidAt) }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusTagType(row.status)">
              {{ getOrderStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progressNote" label="最新进度" min-width="220" show-overflow-tooltip />
        <el-table-column label="履约记录" min-width="180">
          <template #default="{ row }">
            <div class="stack-cell">
              <span class="muted-line">共 {{ row.serviceRecords?.length || 0 }} 条</span>
              <el-button link type="primary" @click="openTimeline(row)">
                查看时间线
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="timelineDialogVisible" width="680px" destroy-on-close>
      <template #header>
        <div>
          <strong>履约时间线</strong>
          <div v-if="timelineOrder" class="muted-line">
            订单 #{{ timelineOrder.id }} · {{ timelineOrder.serviceName }}
          </div>
        </div>
      </template>
      <OrderServiceRecordTimeline
        :records="timelineOrder?.serviceRecords || []"
        empty-text="当前还没有服务过程记录"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import OrderServiceRecordTimeline from '../../components/common/OrderServiceRecordTimeline.vue'
import { fetchAdminOrders } from '../../api'
import { formatCurrency, formatDateTime } from '../../utils/format'
import { getOrderStatusLabel, getOrderStatusTagType } from '../../utils/order'
import {
  getPaymentMethodLabel,
  getPaymentStatusLabel,
  getPaymentStatusTagType
} from '../../utils/payment'

const orders = ref([])
const loading = ref(false)
const timelineDialogVisible = ref(false)
const timelineOrder = ref(null)
const filters = reactive({
  status: '',
  paymentStatus: ''
})

const summary = computed(() => ({
  total: orders.value.length,
  unpaid: orders.value.filter((item) => item.paymentStatus !== 'PAID').length,
  inService: orders.value.filter((item) => item.status === 'IN_SERVICE').length,
  completed: orders.value.filter((item) => item.status === 'COMPLETED').length
}))

const filteredOrders = computed(() =>
  orders.value.filter((item) => {
    if (filters.status && item.status !== filters.status) {
      return false
    }
    if (filters.paymentStatus && item.paymentStatus !== filters.paymentStatus) {
      return false
    }
    return true
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

function openTimeline(order) {
  timelineOrder.value = order
  timelineDialogVisible.value = true
}

onMounted(loadOrders)
</script>
