<template>
  <el-tabs v-model="activeTab" class="message-center-tabs">
    <el-tab-pane v-if="showConversation" label="订单沟通" name="conversation">
      <OrderConversationBoard
        :portal="portal"
        :orders="orders"
        :initial-order-id="initialOrderId"
      />
    </el-tab-pane>

    <el-tab-pane :label="notificationTabLabel" name="notifications">
      <el-card shadow="never">
        <template #header>
          <div class="card-header-between">
            <div>
              <strong>通知中心</strong>
              <div class="section-caption">聚合订单状态、售后处理与资质审核等站内通知</div>
            </div>
            <div class="filter-actions">
              <el-button text :disabled="!unreadCount" @click="handleMarkAllRead">全部已读</el-button>
              <el-button :loading="notificationLoading" @click="$emit('refresh-notifications')">刷新通知</el-button>
            </div>
          </div>
        </template>

        <div class="table-toolbar">
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

        <div v-loading="notificationLoading" class="notification-list">
          <el-empty
            v-if="!filteredNotifications.length && !notificationLoading"
            description="暂无通知"
            class="empty-surface"
          />
          <button
            v-for="item in filteredNotifications"
            :key="item.id"
            type="button"
            class="notification-item"
            :class="{ 'is-read': item.read }"
            @click="openNotification(item)"
          >
            <div class="notification-item__header">
              <div class="notification-item__title">
                <el-tag size="small" :type="tagTypeMap[item.type] || 'info'">{{ typeLabelMap[item.type] || '系统通知' }}</el-tag>
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
      </el-card>
    </el-tab-pane>
  </el-tabs>
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
.message-center-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.notification-list {
  margin-top: 16px;
  display: grid;
  gap: 12px;
}

.notification-item {
  width: 100%;
  text-align: left;
  border: 1px solid var(--border);
  border-radius: 18px;
  background: #fff;
  padding: 16px 18px;
  display: grid;
  gap: 10px;
  cursor: pointer;
}

.notification-item.is-read {
  opacity: 0.78;
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
  line-height: 1.65;
}
</style>
