<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">运营通知中心</h1>
          <p class="page-panel__desc">
            聚合售后工单、资质审核和关键订单提醒，方便管理员快速分发处理。
          </p>
        </div>
        <div class="filter-actions">
          <el-button :loading="notificationLoading" @click="refreshNotifications">刷新通知</el-button>
          <el-button type="primary" :loading="loading" @click="loadData">刷新订单概览</el-button>
        </div>
      </div>

      <div class="metric-strip">
        <div class="metric-chip">
          <span class="metric-chip__label">订单总览</span>
          <strong>{{ summary.total }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">待接单关注</span>
          <strong>{{ summary.pending }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">待完工确认</span>
          <strong>{{ summary.waitingUser }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">未读通知</span>
          <strong>{{ unreadCount }}</strong>
        </div>
      </div>
    </el-card>

    <MessageCenterWorkspace
      portal="admin"
      :orders="orders"
      :conversation-loading="loading"
      :initial-tab="'notifications'"
      :show-conversation="false"
      @refresh-notifications="refreshNotifications"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import MessageCenterWorkspace from '../../components/messages/MessageCenterWorkspace.vue'
import { fetchAdminOrders } from '../../api'
import { notificationStore } from '../../stores/notification'
import { normalizeOrderStatus } from '../../utils/order'

const orders = ref([])
const loading = ref(false)

const notificationLoading = computed(() => notificationStore.isLoading('admin'))
const unreadCount = computed(() => notificationStore.getUnreadCount('admin'))
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
    orders.value = await fetchAdminOrders()
  } catch (error) {
    ElMessage.error(error.message || '获取订单概览失败')
  } finally {
    loading.value = false
  }
}

async function refreshNotifications() {
  try {
    await notificationStore.refresh('admin')
  } catch (error) {
    ElMessage.error(error.message || '获取通知失败')
  }
}

onMounted(async () => {
  await Promise.all([loadData(), refreshNotifications()])
})
</script>
