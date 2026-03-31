<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">消息中心</h1>
          <p class="page-panel__desc">集中查看订单提醒、售后通知和资质审核结果。</p>
        </div>
        <div class="filter-actions">
          <el-button :loading="notificationLoading" @click="refreshNotifications">刷新通知</el-button>
        </div>
      </div>

      <div class="metric-strip">
        <div class="metric-chip">
          <span class="metric-chip__label">全部通知</span>
          <strong>{{ totalCount }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">未读通知</span>
          <strong>{{ unreadCount }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">订单提醒</span>
          <strong>{{ orderStatusCount }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">售后 / 资质</span>
          <strong>{{ secondaryCount }}</strong>
        </div>
      </div>
    </el-card>

    <MessageCenterWorkspace
      portal="worker"
      :show-conversation="false"
      initial-tab="notifications"
      @refresh-notifications="refreshNotifications"
    />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import MessageCenterWorkspace from '../../components/messages/MessageCenterWorkspace.vue'
import { notificationStore } from '../../stores/notification'

const notificationLoading = computed(() => notificationStore.isLoading('worker'))
const unreadCount = computed(() => notificationStore.getUnreadCount('worker'))
const items = computed(() => notificationStore.getItems('worker'))
const totalCount = computed(() => items.value.length)
const orderStatusCount = computed(() => items.value.filter((item) => item.type === 'ORDER_STATUS').length)
const secondaryCount = computed(() =>
  items.value.filter((item) => item.type === 'AFTER_SALE' || item.type === 'WORKER_APPLICATION').length
)

async function refreshNotifications() {
  try {
    await notificationStore.refresh('worker')
  } catch (error) {
    ElMessage.error(error.message || '获取通知失败')
  }
}

onMounted(refreshNotifications)
</script>
