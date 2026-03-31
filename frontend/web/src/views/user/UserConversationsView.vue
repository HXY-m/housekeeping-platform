<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">订单沟通</h1>
          <p class="page-panel__desc">按订单查看与服务人员之间的沟通记录。</p>
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
          <span class="metric-chip__label">待我确认</span>
          <strong>{{ waitingCount }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">服务中</span>
          <strong>{{ inServiceCount }}</strong>
        </div>
      </div>
    </el-card>

    <OrderConversationBoard
      portal="user"
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
import { fetchOrders } from '../../api'
import { normalizeOrderStatus } from '../../utils/order'

const route = useRoute()
const orders = ref([])
const loading = ref(false)

const waitingCount = computed(() =>
  orders.value.filter((item) => {
    const status = normalizeOrderStatus(item.status)
    return status === 'ACCEPTED' || status === 'WAITING_USER_CONFIRMATION'
  }).length
)
const inServiceCount = computed(() =>
  orders.value.filter((item) => normalizeOrderStatus(item.status) === 'IN_SERVICE').length
)

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await fetchOrders()
  } catch (error) {
    ElMessage.error(error.message || '获取订单沟通数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadOrders)
</script>
