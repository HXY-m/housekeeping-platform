<template>
  <div class="message-center-layout">
    <el-card shadow="never" class="conversation-surface conversation-surface--sidebar">
      <template #header>
        <div class="card-header-between">
          <div>
            <strong>会话列表</strong>
            <div class="section-caption">按订单查看用户与服务人员之间的沟通记录</div>
          </div>
          <div class="filter-actions">
            <el-tag effect="plain">{{ filteredThreads.length }} 条</el-tag>
            <el-button :loading="threadLoading" @click="loadConversations">刷新</el-button>
          </div>
        </div>
      </template>

      <div class="table-toolbar__filters conversation-filter-stack">
        <el-input
          v-model.trim="keyword"
          clearable
          placeholder="搜索服务项目或对方姓名"
          style="width: 100%"
        />
        <el-select v-model="statusFilter" clearable placeholder="状态筛选" style="width: 100%">
          <el-option
            v-for="option in ORDER_STATUS_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>

      <el-scrollbar max-height="640px" class="message-thread-scroll">
        <div v-if="threadLoading && !filteredThreads.length" class="message-thread-skeleton-list">
          <el-skeleton v-for="index in 4" :key="index" animated class="message-thread-skeleton">
            <template #template>
              <el-skeleton-item variant="rect" style="width: 100%; height: 122px; border-radius: 20px;" />
            </template>
          </el-skeleton>
        </div>
        <div v-else-if="filteredThreads.length" class="message-thread-list">
          <button
            v-for="thread in filteredThreads"
            :key="thread.orderId"
            type="button"
            class="message-thread-item"
            :class="{ 'is-active': String(selectedOrderId) === String(thread.orderId) }"
            @click="selectedOrderId = thread.orderId"
          >
            <div class="message-thread-item__top">
              <strong>{{ thread.serviceName }}</strong>
              <el-tag size="small" :type="getOrderStatusTagType(thread.status)">
                {{ getOrderStatusLabel(thread.status) }}
              </el-tag>
            </div>
            <div class="message-thread-item__sub">
              {{ portal === 'worker' ? '用户' : '服务人员' }}：{{ thread.counterpartName }}
            </div>
            <div class="message-thread-item__meta">
              <span>{{ formatBookingDateTime(thread.bookingDate, thread.bookingSlot) }}</span>
              <span class="muted-line">{{ thread.progressNote || '等待更新进度' }}</span>
            </div>
            <div class="message-thread-item__preview">
              {{ thread.latestMessage || '暂时还没有留言，点击右侧即可发起沟通。' }}
            </div>
          </button>
        </div>
        <el-empty v-else :description="emptyDescription" class="empty-surface" />
      </el-scrollbar>
    </el-card>

    <el-card shadow="never" class="conversation-surface conversation-surface--main">
      <template #header>
        <div class="card-header-between">
          <div>
            <strong>{{ selectedThread?.serviceName || '订单沟通' }}</strong>
            <div class="section-caption">
              {{ selectedThread ? `${portal === 'worker' ? '用户' : '服务人员'}：${selectedThread.counterpartName}` : '选择左侧订单后查看沟通详情' }}
            </div>
          </div>
          <el-tag v-if="selectedThread" :type="getOrderStatusTagType(selectedThread.status)">
            {{ getOrderStatusLabel(selectedThread.status) }}
          </el-tag>
        </div>
      </template>

      <el-empty v-if="!selectedThread" :description="emptyDescription" class="empty-surface" />
      <div v-else class="message-thread-panel">
        <div class="message-summary-panel">
          <div class="message-summary-panel__meta">
            <div class="order-meta-item">
              <span class="order-meta-item__label">预约时间</span>
              <strong>{{ formatBookingDateTime(selectedThread.bookingDate, selectedThread.bookingSlot) }}</strong>
            </div>
            <div class="order-meta-item">
              <span class="order-meta-item__label">服务地址</span>
              <strong>{{ selectedOrder?.serviceAddress || '--' }}</strong>
            </div>
          </div>
          <div class="message-flow-banner">
            <div>
              <strong>{{ selectedFlowMeta.title }}</strong>
              <p>{{ selectedFlowMeta.description }}</p>
            </div>
            <el-tag :type="selectedFlowMeta.ownerType || 'info'" effect="plain">
              当前由{{ selectedFlowMeta.owner || '系统' }}推进
            </el-tag>
          </div>
        </div>

        <el-scrollbar max-height="440px" class="message-bubble-scroll" v-loading="messageLoading">
          <div v-if="messageLoading && !messages.length" class="message-bubble-skeleton-list">
            <el-skeleton v-for="index in 3" :key="index" animated>
              <template #template>
                <el-skeleton-item variant="rect" style="width: 100%; height: 88px; border-radius: 18px;" />
              </template>
            </el-skeleton>
          </div>
          <div v-else-if="messages.length" class="message-bubble-list">
            <div
              v-for="item in messages"
              :key="item.id"
              class="message-bubble"
              :class="item.senderUserId === authStore.state.user?.id ? 'message-bubble--self' : 'message-bubble--other'"
            >
              <div class="message-bubble__meta">
                <strong>{{ item.senderName }}</strong>
                <span>{{ formatMessageTime(item.createdAt) }}</span>
              </div>
              <div class="message-bubble__content">{{ item.content }}</div>
            </div>
          </div>
          <el-empty v-else description="当前订单还没有留言记录" class="empty-surface" />
        </el-scrollbar>

        <div class="message-compose">
          <div class="tag-list">
            <el-tag
              v-for="item in quickPhrases"
              :key="item"
              effect="plain"
              class="message-quick-tag"
              @click="appendQuickPhrase(item)"
            >
              {{ item }}
            </el-tag>
          </div>
          <el-input
            v-model.trim="draft"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="可记录上门时间、门禁说明、服务确认等沟通内容。"
          />
          <div class="order-card__actions">
            <span class="muted-line">{{ selectedFlowMeta.hint }}</span>
            <el-button type="primary" :loading="sending" @click="submitMessage">发送留言</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchOrderConversations, fetchOrderMessages, sendOrderMessage } from '../../api'
import { authStore } from '../../stores/auth'
import { formatBookingDateTime } from '../../utils/bookingSlots'
import {
  ORDER_STATUS_OPTIONS,
  getOrderStatusLabel,
  getOrderStatusTagType,
  normalizeOrderStatus
} from '../../utils/order'
import { getUserOrderFlowMeta, getWorkerOrderFlowMeta } from '../../utils/orderFlow'

const props = defineProps({
  portal: {
    type: String,
    required: true
  },
  orders: {
    type: Array,
    default: () => []
  },
  initialOrderId: {
    type: [String, Number],
    default: ''
  }
})

const keyword = ref('')
const statusFilter = ref('')
const threadLoading = ref(false)
const messageLoading = ref(false)
const sending = ref(false)
const selectedOrderId = ref('')
const conversations = ref([])
const messages = ref([])
const draft = ref('')

const quickPhrases = computed(() => {
  if (props.portal === 'worker') {
    return ['我已接单，稍后电话确认', '预计会准时到达，如有变化我会提前联系', '完工资料已上传，请你确认']
  }
  return ['门禁请提前电话联系', '如果有临时调整我会在这里说明', '服务完成后请提醒我确认']
})

const orderMap = computed(() =>
  Object.fromEntries((props.orders || []).map((item) => [String(item.id), item]))
)

const filteredThreads = computed(() => {
  const normalizedKeyword = keyword.value.toLowerCase()
  const items = [...conversations.value]

  return items.filter((item) => {
    if (statusFilter.value && normalizeOrderStatus(item.status) !== statusFilter.value) {
      return false
    }
    if (!normalizedKeyword) {
      return true
    }
    return [item.serviceName, item.counterpartName, item.latestMessage]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(normalizedKeyword))
  })
})

const selectedThread = computed(
  () => filteredThreads.value.find((item) => String(item.orderId) === String(selectedOrderId.value)) || filteredThreads.value[0] || null
)

const selectedOrder = computed(() =>
  selectedThread.value ? orderMap.value[String(selectedThread.value.orderId)] || null : null
)

const selectedFlowMeta = computed(() => {
  if (!selectedOrder.value) {
    return {
      owner: '系统',
      ownerType: 'info',
      title: '暂无流程提示',
      description: '选择左侧订单后查看当前阶段说明。',
      hint: '你可以围绕当前订单与对方沟通关键安排。'
    }
  }
  return props.portal === 'worker'
    ? getWorkerOrderFlowMeta(selectedOrder.value)
    : getUserOrderFlowMeta(selectedOrder.value)
})

const emptyDescription = computed(() =>
  threadLoading.value ? '会话加载中' : '当前还没有可展示的订单沟通'
)

watch(
  () => props.initialOrderId,
  (orderId) => {
    if (orderId) {
      selectedOrderId.value = String(orderId)
    }
  },
  { immediate: true }
)

watch(
  filteredThreads,
  (list) => {
    if (!list.length) {
      selectedOrderId.value = ''
      return
    }
    if (!selectedOrderId.value || !list.some((item) => String(item.orderId) === String(selectedOrderId.value))) {
      selectedOrderId.value = String(props.initialOrderId || list[0].orderId)
    }
  },
  { immediate: true }
)

watch(
  () => selectedThread.value?.orderId,
  async (orderId) => {
    if (!orderId) {
      messages.value = []
      return
    }
    await loadMessages(orderId)
  },
  { immediate: true }
)

async function loadConversations() {
  threadLoading.value = true
  try {
    conversations.value = await fetchOrderConversations()
  } catch (error) {
    ElMessage.error(error.message || '获取订单沟通列表失败')
  } finally {
    threadLoading.value = false
  }
}

async function loadMessages(orderId) {
  messageLoading.value = true
  try {
    messages.value = await fetchOrderMessages(orderId)
  } catch (error) {
    ElMessage.error(error.message || '获取留言记录失败')
  } finally {
    messageLoading.value = false
  }
}

function appendQuickPhrase(text) {
  draft.value = draft.value ? `${draft.value}\n${text}` : text
}

function formatMessageTime(value) {
  if (!value) {
    return '刚刚'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

async function submitMessage() {
  if (!selectedThread.value) {
    ElMessage.warning('请先选择订单会话')
    return
  }
  if (!draft.value) {
    ElMessage.warning('请输入留言内容')
    return
  }

  sending.value = true
  try {
    await sendOrderMessage(selectedThread.value.orderId, {
      content: draft.value
    })
    draft.value = ''
    ElMessage.success('留言已发送')
    await Promise.all([loadMessages(selectedThread.value.orderId), loadConversations()])
  } catch (error) {
    ElMessage.error(error.message || '发送留言失败')
  } finally {
    sending.value = false
  }
}

onMounted(loadConversations)
</script>

<style scoped>
.message-center-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 16px;
}

.conversation-surface {
  background: rgba(255, 255, 255, 0.58);
  backdrop-filter: blur(22px);
}

.conversation-surface--sidebar {
  position: sticky;
  top: 16px;
  align-self: start;
}

.conversation-filter-stack {
  margin-top: 4px;
}

.message-thread-scroll,
.message-bubble-scroll {
  margin-top: 16px;
}

.message-thread-list,
.message-thread-skeleton-list,
.message-bubble-list,
.message-bubble-skeleton-list,
.message-thread-panel,
.message-summary-panel,
.message-compose {
  display: grid;
  gap: 12px;
}

.message-thread-item {
  width: 100%;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.48);
  backdrop-filter: blur(22px);
  padding: 16px 18px;
  text-align: left;
  cursor: pointer;
  display: grid;
  gap: 8px;
  transition:
    border-color 0.24s ease,
    box-shadow 0.24s ease,
    transform 0.24s ease;
}

.message-thread-item:hover,
.message-thread-item.is-active {
  border-color: rgba(0, 113, 227, 0.26);
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.08);
  transform: translateY(-2px);
}

.message-thread-item__top,
.message-thread-item__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.message-thread-item__sub,
.message-thread-item__preview {
  color: var(--muted);
  line-height: 1.6;
}

.message-summary-panel__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.message-flow-banner {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.46), rgba(255, 255, 255, 0.26));
  border: 1px solid rgba(255, 255, 255, 0.68);
}

.message-flow-banner p {
  margin: 8px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.message-bubble {
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(18px);
}

.message-bubble--self {
  background: rgba(0, 113, 227, 0.1);
  border-color: rgba(0, 113, 227, 0.18);
}

.message-bubble__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--muted);
  font-size: 13px;
  margin-bottom: 8px;
}

.message-bubble__content {
  line-height: 1.75;
  white-space: pre-wrap;
}

.message-quick-tag {
  cursor: pointer;
}

@media (max-width: 1080px) {
  .message-center-layout {
    grid-template-columns: 1fr;
  }

  .conversation-surface--sidebar {
    position: static;
  }

  .message-summary-panel__meta,
  .message-thread-item__top,
  .message-thread-item__meta,
  .message-bubble__meta,
  .message-flow-banner {
    grid-template-columns: 1fr;
    display: grid;
  }
}
</style>
