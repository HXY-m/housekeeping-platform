<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">订单沟通与提醒</h1>
          <p class="page-panel__desc">
            面向服务人员集中查看用户留言、预约推进提醒和资质审核通知。
          </p>
        </div>
        <div class="filter-actions">
          <el-button :loading="notificationLoading" @click="refreshNotifications">刷新通知</el-button>
          <el-button type="primary" :loading="loading" @click="loadData">刷新订单</el-button>
        </div>
      </div>

      <div class="metric-strip">
        <div class="metric-chip">
          <span class="metric-chip__label">我的服务单</span>
          <strong>{{ summary.total }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">待接单</span>
          <strong>{{ summary.pending }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">待用户确认</span>
          <strong>{{ summary.waitingUser }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">未读通知</span>
          <strong>{{ unreadCount }}</strong>
        </div>
      </div>
    </el-card>

    <MessageCenterWorkspace
      portal="worker"
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
import { fetchWorkerOrders } from '../../api'
import { notificationStore } from '../../stores/notification'
import { normalizeOrderStatus } from '../../utils/order'

const route = useRoute()
const orders = ref([])
const loading = ref(false)

const notificationLoading = computed(() => notificationStore.isLoading('worker'))
const unreadCount = computed(() => notificationStore.getUnreadCount('worker'))
const summary = computed(() => ({
  total: orders.value.length,
  pending: orders.value.filter((item) => normalizeOrderStatus(item.status) === 'PENDING').length,
  waitingUser: orders.value.filter(
    (item) => normalizeOrderStatus(item.status) === 'WAITING_USER_CONFIRMATION'
  ).length
}))

async function loadData() {
  loading.value = true
  try {
    orders.value = await fetchWorkerOrders()
  } catch (error) {
    ElMessage.error(error.message || '获取订单沟通数据失败')
  } finally {
    loading.value = false
  }
}

async function refreshNotifications() {
  try {
    await notificationStore.refresh('worker')
  } catch (error) {
    ElMessage.error(error.message || '获取通知失败')
  }
}

onMounted(async () => {
  await Promise.all([loadData(), refreshNotifications()])
})
</script>
