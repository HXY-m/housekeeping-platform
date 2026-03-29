<template>
  <div class="page-stack">
    <div class="metric-strip">
      <div v-if="showConversation" class="metric-chip">
        <span class="metric-chip__label">订单会话</span>
        <strong>{{ orders.length }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">未读通知</span>
        <strong>{{ unreadCount }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">订单提醒</span>
        <strong>{{ notificationTypeCount.ORDER_STATUS }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">售后 / 审核</span>
        <strong>{{ notificationTypeCount.AFTER_SALE + notificationTypeCount.WORKER_APPLICATION }}</strong>
      </div>
    </div>

    <el-card shadow="never" class="page-panel message-center-panel">
      <div class="card-header-between">
        <div>
          <strong>{{ showConversation ? '沟通与通知工作台' : '通知工作台' }}</strong>
          <p class="section-caption">统一处理订单留言、状态提醒、售后反馈和资质审核通知。</p>
        </div>
        <div class="hero-actions">
          <el-button text :disabled="!unreadCount" @click="handleMarkAllRead">全部已读</el-button>
          <el-button :loading="notificationLoading" @click="$emit('refresh-notifications')">刷新通知</el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="message-center-tabs">
        <el-tab-pane v-if="showConversation" label="订单沟通" name="conversation">
          <OrderConversationBoard
            :portal="portal"
            :orders="orders"
            :initial-order-id="initialOrderId"
          />
        </el-tab-pane>

        <el-tab-pane :label="notificationTabLabel" name="notifications">
          <div class="page-stack">
            <div class="table-toolbar table-toolbar--dense">
              <div class="table-toolbar__filters">
                <el-radio-group v-model="readFilter" size="small">
                  <el-radio-button label="ALL">全部</el-radio-button>
                  <el-radio-button label="UNREAD">未读</el-radio-button>
                  <el-radio-button label="READ">已读</el-radio-button>
                </el-radio-group>
                <el-select v-model="categoryFilter" clearable placeholder="通知类别" style="width: 180px">
                  <el-option label="订单提醒" value="ORDER_STATUS" />
                  <el-option label="订单沟通" value="ORDER_MESSAGE" />
                  <el-option label="售后处理" value="AFTER_SALE" />
                  <el-option label="资质审核" value="WORKER_APPLICATION" />
                </el-select>
              </div>
              <span class="section-caption">未读 {{ unreadCount }} 条</span>
            </div>

            <div v-loading="notificationLoading" class="notification-card-list">
              <div v-if="notificationLoading && !filteredNotifications.length" class="notification-skeleton-list">
                <el-skeleton v-for="index in 4" :key="index" animated class="notification-skeleton">
                  <template #template>
                    <el-skeleton-item variant="rect" style="width: 100%; height: 132px; border-radius: 22px;" />
                  </template>
                </el-skeleton>
              </div>

              <el-empty
                v-else-if="!filteredNotifications.length"
                description="暂无通知"
                class="empty-surface"
              />

              <button
                v-for="item in filteredNotifications"
                v-else
                :key="item.id"
                type="button"
                class="notification-item"
                :class="{ 'is-read': item.read }"
                @click="openNotification(item)"
              >
                <div class="notification-item__header">
                  <div class="notification-item__title">
                    <el-tag size="small" :type="tagTypeMap[item.type] || 'info'">
                      {{ typeLabelMap[item.type] || '系统通知' }}
                    </el-tag>
                    <strong>{{ item.title }}</strong>
                  </div>
                  <span class="notification-item__time">{{ formatNotificationTime(item.createdAt) }}</span>
                </div>
                <div class="notification-item__content">{{ item.content }}</div>
                <div class="notification-item__footer">
                  <span class="muted-line">{{ item.read ? '已读' : '未读' }}</span>
                  <el-button
                    v-if="!item.read"
                    link
                    type="primary"
                    @click.stop="handleMarkRead(item.id)"
                  >
                    标记已读
                  </el-button>
                </div>
              </button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import OrderConversationBoard from './OrderConversationBoard.vue'
import { notificationStore } from '../../stores/notification'

const props = defineProps({
  portal: {
    type: String,
    required: true
  },
  orders: {
    type: Array,
    default: () => []
  },
  conversationLoading: {
    type: Boolean,
    default: false
  },
  initialOrderId: {
    type: [String, Number],
    default: ''
  },
  initialTab: {
    type: String,
    default: 'conversation'
  },
  showConversation: {
    type: Boolean,
    default: true
  }
})

defineEmits(['refresh-notifications'])

const router = useRouter()
const activeTab = ref(props.showConversation ? props.initialTab : 'notifications')
const readFilter = ref('ALL')
const categoryFilter = ref('')

const tagTypeMap = {
  ORDER_STATUS: 'primary',
  ORDER_MESSAGE: 'success',
  AFTER_SALE: 'danger',
  WORKER_APPLICATION: 'warning'
}

const typeLabelMap = {
  ORDER_STATUS: '订单提醒',
  ORDER_MESSAGE: '订单沟通',
  AFTER_SALE: '售后处理',
  WORKER_APPLICATION: '资质审核'
}

const notifications = computed(() => notificationStore.getItems(props.portal))
const unreadCount = computed(() => notificationStore.getUnreadCount(props.portal))
const notificationLoading = computed(() => notificationStore.isLoading(props.portal))
const notificationTabLabel = computed(() =>
  unreadCount.value ? `通知中心 (${unreadCount.value})` : '通知中心'
)

const notificationTypeCount = computed(() => {
  const count = {
    ORDER_STATUS: 0,
    ORDER_MESSAGE: 0,
    AFTER_SALE: 0,
    WORKER_APPLICATION: 0
  }
  notifications.value.forEach((item) => {
    if (count[item.type] !== undefined) {
      count[item.type] += 1
    }
  })
  return count
})

const filteredNotifications = computed(() =>
  notifications.value.filter((item) => {
    if (readFilter.value === 'UNREAD' && item.read) {
      return false
    }
    if (readFilter.value === 'READ' && !item.read) {
      return false
    }
    if (categoryFilter.value && item.type !== categoryFilter.value) {
      return false
    }
    return true
  })
)

watch(
  () => props.initialTab,
  (value) => {
    if (value === 'notifications' || (value === 'conversation' && props.showConversation)) {
      activeTab.value = value
    }
  },
  { immediate: true }
)

function formatNotificationTime(value) {
  if (!value) {
    return '--'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

async function handleMarkRead(id) {
  try {
    await notificationStore.markRead(props.portal, id)
  } catch (error) {
    ElMessage.error(error.message || '通知已读失败')
  }
}

async function handleMarkAllRead() {
  try {
    await notificationStore.markAllRead(props.portal)
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error(error.message || '批量已读失败')
  }
}

async function openNotification(item) {
  if (!item.read) {
    await handleMarkRead(item.id)
  }

  if (item.actionPath) {
    router.push(item.actionPath)
    if (props.showConversation && item.actionPath.includes('/messages') && item.actionPath.includes('orderId=')) {
      activeTab.value = 'conversation'
      return
    }
  }

  activeTab.value = 'notifications'
}
</script>

<style scoped>
.message-center-panel {
  gap: 0;
}

.message-center-tabs {
  margin-top: 18px;
}

.message-center-tabs :deep(.el-tabs__header) {
  margin-bottom: 18px;
}

.message-center-tabs :deep(.el-tabs__nav-wrap::after) {
  background: rgba(255, 255, 255, 0.32);
}

.notification-card-list,
.notification-skeleton-list {
  display: grid;
  gap: 12px;
}

.notification-item {
  width: 100%;
  text-align: left;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.52);
  backdrop-filter: blur(22px);
  padding: 18px 20px;
  display: grid;
  gap: 12px;
  cursor: pointer;
  transition:
    border-color 0.24s ease,
    box-shadow 0.24s ease,
    transform 0.24s ease;
}

.notification-item:hover {
  border-color: rgba(0, 113, 227, 0.24);
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.08);
  transform: translateY(-2px);
}

.notification-item.is-read {
  opacity: 0.82;
}

.notification-item__header,
.notification-item__footer,
.notification-item__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.notification-item__content,
.notification-item__time {
  color: var(--muted);
  line-height: 1.7;
}
</style>
