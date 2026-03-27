<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">订单沟通与通知</h1>
          <p class="page-panel__desc">
            在这里统一查看和服务人员的留言往来，以及订单状态、售后进展等站内通知。
          </p>
        </div>
        <div class="filter-actions">
          <el-button :loading="notificationLoading" @click="refreshNotifications">刷新通知</el-button>
          <el-button type="primary" :loading="loading" @click="loadData">刷新订单</el-button>
        </div>
      </div>

      <div class="metric-strip">
        <div class="metric-chip">
          <span class="metric-chip__label">我的订单</span>
          <strong>{{ summary.total }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">待我确认</span>
          <strong>{{ summary.needUserAction }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">服务中</span>
          <strong>{{ summary.inService }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">未读通知</span>
          <strong>{{ unreadCount }}</strong>
        </div>
      </div>
    </el-card>

    <MessageCenterWorkspace
      portal="user"
      :orders="orders"
      :conversation-loading="loading"
      :initial-order-id="route.query.orderId"
      :initial-tab="route.query.tab === 'notifications' ? 'notifications' : 'conversation'"
      @refresh-notifications="refreshNotifications"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import MessageCenterWorkspace from '../../components/messages/MessageCenterWorkspace.vue'
import { fetchOrders } from '../../api'
import { notificationStore } from '../../stores/notification'
import { normalizeOrderStatus } from '../../utils/order'

const route = useRoute()
const orders = ref([])
const loading = ref(false)

const notificationLoading = computed(() => notificationStore.isLoading('user'))
const unreadCount = computed(() => notificationStore.getUnreadCount('user'))
const summary = computed(() => ({
  total: orders.value.length,
  needUserAction: orders.value.filter((item) => {
    const status = normalizeOrderStatus(item.status)
    return status === 'ACCEPTED' || status === 'WAITING_USER_CONFIRMATION'
  }).length,
  inService: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'IN_SERVICE').length
}))

async function loadData() {
  loading.value = true
  try {
    orders.value = await fetchOrders()
  } catch (error) {
    ElMessage.error(error.message || '获取消息中心数据失败')
  } finally {
    loading.value = false
  }
}

async function refreshNotifications() {
  try {
    await notificationStore.refresh('user')
  } catch (error) {
    ElMessage.error(error.message || '获取通知失败')
  }
}

onMounted(async () => {
  await Promise.all([loadData(), refreshNotifications()])
})
</script>
