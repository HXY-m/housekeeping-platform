<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">订单沟通</h1>
          <p class="page-panel__desc">按订单集中查看与用户之间的沟通记录。</p>
        </div>
        <div class="filter-actions">
          <el-button :loading="loading" @click="loadOrders">刷新订单</el-button>
        </div>
      </div>

      <div class="metric-strip">
        <div class="metric-chip">
          <span class="metric-chip__label">可沟通订单</span>
          <strong>{{ orders.length }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">待接单</span>
          <strong>{{ pendingCount }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">待用户确认</span>
          <strong>{{ waitingUserCount }}</strong>
        </div>
      </div>
    </el-card>

    <OrderConversationBoard
      portal="worker"
      :orders="orders"
      :initial-order-id="route.query.orderId"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import OrderConversationBoard from '../../components/messages/OrderConversationBoard.vue'
import { fetchWorkerOrders } from '../../api'
import { normalizeOrderStatus } from '../../utils/order'

const route = useRoute()
const orders = ref([])
const loading = ref(false)

const pendingCount = computed(() =>
  orders.value.filter((item) => normalizeOrderStatus(item.status) === 'PENDING').length
)
const waitingUserCount = computed(() =>
  orders.value.filter((item) => normalizeOrderStatus(item.status) === 'WAITING_USER_CONFIRMATION').length
)

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await fetchWorkerOrders()
  } catch (error) {
    ElMessage.error(error.message || '获取订单沟通数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadOrders)
</script>
