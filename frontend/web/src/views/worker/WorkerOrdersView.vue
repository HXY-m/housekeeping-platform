<template>
  <div class="page-stack">
    <div class="console-overview console-overview--worker">
      <div>
        <el-tag type="warning" round>服务工作台</el-tag>
        <h1>订单概览</h1>
        <p>列表页仅保留摘要信息，详细履约、打卡和完工操作可在当前页面展开处理。</p>
      </div>
      <div class="hero-actions">
        <el-button plain :disabled="!workerQualified" @click="router.push('/worker/conversations')">订单沟通</el-button>
        <el-button type="primary" :loading="loading" @click="loadOrders">刷新订单</el-button>
      </div>
    </div>

    <el-alert
      v-if="showApprovedEntryAlert"
      title="资质审核已通过，可以开始接单"
      description="你的服务信息已经对外展示，建议优先查看待接单和待开工订单。"
      type="success"
      show-icon
      @close="clearSourceQuery"
    />

    <div class="metric-strip">
      <div class="metric-chip">
        <span class="metric-chip__label">分配订单</span>
        <strong>{{ summary.total }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待接单</span>
        <strong>{{ summary.pending }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待用户确认</span>
        <strong>{{ summary.accepted }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待开工</span>
        <strong>{{ summary.confirmed }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待确认完工</span>
        <strong>{{ summary.waitingCompletion }}</strong>
      </div>
    </div>

    <el-alert
      v-if="!workerQualified"
      :title="qualificationBanner.title"
      :description="qualificationBanner.description"
      :type="qualificationBanner.type"
      show-icon
      :closable="false"
    >
      <template #default>
        <div class="hero-actions">
          <el-button type="primary" @click="router.push('/worker/qualification')">
            {{ qualificationBanner.buttonText }}
          </el-button>
        </div>
      </template>
    </el-alert>

    <el-card shadow="never" class="page-panel">
      <div class="card-header-between">
        <div>
          <strong>订单筛选</strong>
          <p class="section-caption">优先查看待接单与待开工订单，具体处理可在当前页面展开。</p>
        </div>
      </div>

      <div class="order-filter-pills">
        <el-radio-group v-model="statusFilter" size="small" class="order-filter-pills__group">
          <el-radio-button
            v-for="option in workerStatusFilterOptions"
            :key="option.value"
            :label="option.value"
          >
            {{ option.label }}
          </el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <div v-if="loading && !orders.length" class="order-skeleton-list">
      <el-skeleton v-for="index in 3" :key="index" animated class="order-skeleton-card">
        <template #template>
          <el-skeleton-item variant="rect" style="width: 100%; height: 200px; border-radius: 24px;" />
        </template>
      </el-skeleton>
    </div>

    <el-empty v-else-if="!orders.length" description="当前没有可处理的服务订单" class="empty-surface" />

    <div v-else class="order-card-grid">
      <el-card v-for="order in orders" :key="order.id" shadow="never" class="order-card order-card--summary">
        <div class="order-card__header">
          <div class="order-card__identity">
            <el-avatar :size="48" class="order-card__avatar">
              {{ order.customerName?.slice(0, 1) || '客' }}
            </el-avatar>
            <div>
              <div class="order-card__title">{{ order.serviceName }}</div>
              <div class="order-card__subtitle">订单号 #{{ order.id }} · 用户 {{ order.customerName }}</div>
            </div>
          </div>
          <div class="order-card__header-tags">
            <el-tag :type="getOrderStatusTagType(order.status)">{{ getOrderStatusLabel(order.status) }}</el-tag>
          </div>
        </div>

        <div class="order-card__meta-grid">
          <div class="order-meta-item">
            <span class="order-meta-item__label">预约时间</span>
            <strong>{{ formatBookingDateTime(order.bookingDate, order.bookingSlot) }}</strong>
          </div>
          <div class="order-meta-item">
            <span class="order-meta-item__label">联系方式</span>
            <strong>{{ order.contactPhone }}</strong>
          </div>
          <div class="order-meta-item order-meta-item--wide">
            <span class="order-meta-item__label">服务地址</span>
            <strong>{{ order.serviceAddress || '--' }}</strong>
          </div>
        </div>

        <div class="order-stage-callout order-stage-callout--glass">
          <div>
            <div class="order-stage-callout__title">{{ getWorkerOrderFlowMeta(order).title }}</div>
            <div class="order-stage-callout__desc">{{ order.progressNote || getWorkerOrderFlowMeta(order).description }}</div>
          </div>
          <el-tag :type="getWorkerOrderFlowMeta(order).ownerType" effect="plain">
            当前由{{ getWorkerOrderFlowMeta(order).owner }}推进
          </el-tag>
        </div>

        <div class="order-card__actions">
          <el-button plain :disabled="!workerQualified" @click="openConversation(order.id)">订单沟通</el-button>
          <el-button type="primary" :disabled="!workerQualified" @click="openDetail(order.id)">查看详情</el-button>
        </div>
      </el-card>
    </div>

    <ListPagination
      v-if="orders.length"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="pageSizes"
      :total="total"
    />

    <el-drawer
      v-model="detailDrawerVisible"
      title="订单详情"
      size="720px"
      append-to-body
      destroy-on-close
    >
      <WorkerOrderDetailView v-if="selectedOrderId" embedded :order-id="selectedOrderId" />
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ListPagination from '../../components/common/ListPagination.vue'
import WorkerOrderDetailView from './WorkerOrderDetailView.vue'
import { useServerPagination } from '../../composables/useServerPagination'
import { fetchCurrentWorkerProfile, fetchWorkerOrderSummary, fetchWorkerOrders } from '../../api'
import { formatBookingDateTime } from '../../utils/bookingSlots'
import { getWorkerOrderFlowMeta } from '../../utils/orderFlow'
import {
  WORKER_ORDER_STATUS_OPTIONS,
  getOrderStatusLabel,
  getOrderStatusTagType
} from '../../utils/order'
import { buildConversationPath } from '../../utils/orderNavigation'
import { getWorkerQualificationNotice, isWorkerQualificationApproved } from '../../utils/workerApplication'

const route = useRoute()
const router = useRouter()
const statusFilter = ref('ALL')
const loading = ref(false)
const orders = ref([])
const workerProfile = ref(null)
const detailDrawerVisible = ref(false)
const selectedOrderId = ref('')
const summary = reactive({
  total: 0,
  pending: 0,
  accepted: 0,
  confirmed: 0,
  waitingCompletion: 0
})

const workerStatusFilterOptions = [{ label: '全部', value: 'ALL' }, ...WORKER_ORDER_STATUS_OPTIONS]
const { currentPage, pageSize, pageSizes, total, buildParams, applyPageResult, resetPage } = useServerPagination(8)

const workerQualified = computed(() => isWorkerQualificationApproved(workerProfile.value?.qualificationStatus))
const qualificationBanner = computed(() => getWorkerQualificationNotice(workerProfile.value?.qualificationStatus))
const showApprovedEntryAlert = computed(() => route.query.source === 'qualification_approved')

async function loadOrders() {
  loading.value = true
  try {
    const status = statusFilter.value === 'ALL' ? '' : statusFilter.value
    const [profileResult, result, summaryResult] = await Promise.all([
      fetchCurrentWorkerProfile().catch(() => null),
      fetchWorkerOrders(buildParams({ status })),
      fetchWorkerOrderSummary({ status })
    ])
    workerProfile.value = profileResult
    orders.value = applyPageResult(result)
    summary.total = Number(summaryResult?.total || 0)
    summary.pending = Number(summaryResult?.pending || 0)
    summary.accepted = Number(summaryResult?.accepted || 0)
    summary.confirmed = Number(summaryResult?.confirmed || 0)
    summary.waitingCompletion = Number(summaryResult?.waitingUserConfirmation || 0)
  } catch (error) {
    ElMessage.error(error.message || '获取服务订单失败')
  } finally {
    loading.value = false
  }
}

function openDetail(orderId) {
  if (!workerQualified.value) {
    ElMessage.warning('资质审核通过后才能进入订单详情页处理订单')
    router.push('/worker/qualification')
    return
  }
  selectedOrderId.value = String(orderId)
  detailDrawerVisible.value = true
}

function openConversation(orderId) {
  if (!workerQualified.value) {
    ElMessage.warning('资质审核通过后才能进入订单沟通')
    router.push('/worker/qualification')
    return
  }
  router.push(buildConversationPath('worker', orderId))
}

function clearSourceQuery() {
  const nextQuery = { ...route.query }
  delete nextQuery.source
  router.replace({
    path: route.path,
    query: nextQuery
  })
}

watch(statusFilter, () => {
  if (currentPage.value !== 1) {
    resetPage()
    return
  }
  loadOrders()
})

watch([currentPage, pageSize], () => {
  loadOrders()
})

onMounted(() => {
  if (route.query.source === 'qualification_approved' && statusFilter.value === 'ALL') {
    statusFilter.value = 'PENDING'
  }
  loadOrders()
})
</script>
