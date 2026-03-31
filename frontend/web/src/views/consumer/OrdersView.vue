<template>
  <div class="page-stack">
    <div class="console-overview">
      <div>
        <el-tag type="success" round>我的订单</el-tag>
        <h1>订单概览</h1>
        <p>这里仅显示订单摘要，详细履约、支付、评价和售后信息可在当前页面展开查看。</p>
      </div>
      <div class="hero-actions">
        <el-button plain @click="router.push('/workers')">继续预约服务</el-button>
        <el-button type="primary" :loading="loading" @click="loadData">刷新订单</el-button>
      </div>
    </div>

    <div class="metric-strip">
      <div class="metric-chip">
        <span class="metric-chip__label">订单总数</span>
        <strong>{{ orderSummary.total }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">处理中</span>
        <strong>{{ orderSummary.active }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待我确认</span>
        <strong>{{ orderSummary.needUserAction }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">待支付</span>
        <strong>{{ orderSummary.unpaid }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">售后工单</span>
        <strong>{{ orderSummary.afterSales }}</strong>
      </div>
    </div>

    <el-card shadow="never" class="page-panel">
      <div class="card-header-between">
        <div>
          <strong>订单筛选</strong>
          <p class="section-caption">按状态查看摘要，点击详情可在当前页面展开查看完整信息。</p>
        </div>
      </div>

      <div class="order-filter-pills">
        <el-radio-group v-model="statusFilter" size="small" class="order-filter-pills__group">
          <el-radio-button label="ALL">全部</el-radio-button>
          <el-radio-button label="PENDING">待接单</el-radio-button>
          <el-radio-button label="ACCEPTED">待我确认</el-radio-button>
          <el-radio-button label="CONFIRMED">待上门</el-radio-button>
          <el-radio-button label="IN_SERVICE">服务中</el-radio-button>
          <el-radio-button label="WAITING_USER_CONFIRMATION">待确认完工</el-radio-button>
          <el-radio-button label="COMPLETED">已完成</el-radio-button>
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

    <el-empty v-else-if="!orders.length" description="当前没有可展示的订单" class="empty-surface" />

    <div v-else class="order-card-grid">
      <el-card v-for="order in orders" :key="order.id" shadow="never" class="order-card order-card--summary">
        <div class="order-card__header">
          <div class="order-card__identity">
            <el-avatar :size="48" class="order-card__avatar">
              {{ order.workerName?.slice(0, 1) || '家' }}
            </el-avatar>
            <div>
              <div class="order-card__title">{{ order.serviceName }}</div>
              <div class="order-card__subtitle">订单号 #{{ order.id }} · 服务人员 {{ order.workerName || '待分配' }}</div>
            </div>
          </div>
          <div class="order-card__header-tags">
            <el-tag :type="getOrderStatusTagType(order.status)">{{ getOrderStatusLabel(order.status) }}</el-tag>
            <el-tag :type="getPaymentStatusTagType(order.paymentStatus)" effect="plain">
              {{ getPaymentStatusLabel(order.paymentStatus) }}
            </el-tag>
          </div>
        </div>

        <div class="order-card__meta-grid">
          <div class="order-meta-item">
            <span class="order-meta-item__label">预约时间</span>
            <strong>{{ formatBookingDateTime(order.bookingDate, order.bookingSlot) }}</strong>
          </div>
          <div class="order-meta-item">
            <span class="order-meta-item__label">联系人</span>
            <strong>{{ order.customerName }}</strong>
          </div>
          <div class="order-meta-item order-meta-item--wide">
            <span class="order-meta-item__label">服务地址</span>
            <strong>{{ order.serviceAddress || '--' }}</strong>
          </div>
          <div class="order-meta-item">
            <span class="order-meta-item__label">应付金额</span>
            <strong>{{ formatCurrency(order.payableAmount || 0) }}</strong>
          </div>
        </div>

        <div class="order-stage-callout order-stage-callout--glass">
          <div>
            <div class="order-stage-callout__title">{{ getUserOrderFlowMeta(order).title }}</div>
            <div class="order-stage-callout__desc">{{ order.progressNote || getUserOrderFlowMeta(order).description }}</div>
          </div>
          <el-tag :type="getUserOrderFlowMeta(order).ownerType" effect="plain">
            当前由{{ getUserOrderFlowMeta(order).owner }}推进
          </el-tag>
        </div>

        <div class="order-card__actions">
          <el-button plain @click="openConversation(order.id)">订单沟通</el-button>
          <el-button type="primary" @click="openDetail(order.id)">查看详情</el-button>
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
      <UserOrderDetailView v-if="selectedOrderId" embedded :order-id="selectedOrderId" />
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ListPagination from '../../components/common/ListPagination.vue'
import UserOrderDetailView from '../user/UserOrderDetailView.vue'
import { useServerPagination } from '../../composables/useServerPagination'
import { fetchOrders, fetchOrderSummary } from '../../api'
import { getUserOrderFlowMeta } from '../../utils/orderFlow'
import { formatBookingDateTime } from '../../utils/bookingSlots'
import { getOrderStatusLabel, getOrderStatusTagType } from '../../utils/order'
import { formatCurrency } from '../../utils/format'
import { getPaymentStatusLabel, getPaymentStatusTagType } from '../../utils/payment'
import { buildConversationPath } from '../../utils/orderNavigation'

const router = useRouter()
const loading = ref(false)
const orders = ref([])
const statusFilter = ref('ALL')
const detailDrawerVisible = ref(false)
const selectedOrderId = ref('')
const orderSummary = reactive({
  total: 0,
  active: 0,
  needUserAction: 0,
  unpaid: 0,
  afterSales: 0
})

const { currentPage, pageSize, pageSizes, total, buildParams, applyPageResult, resetPage } = useServerPagination(8)

async function loadData() {
  loading.value = true
  try {
    const status = statusFilter.value === 'ALL' ? '' : statusFilter.value
    const [orderResult, summaryResult] = await Promise.all([
      fetchOrders(buildParams({ status })),
      fetchOrderSummary({ status })
    ])
    orders.value = applyPageResult(orderResult)
    orderSummary.total = Number(summaryResult?.total || 0)
    orderSummary.active = Number(summaryResult?.active || 0)
    orderSummary.needUserAction = Number(summaryResult?.needUserAction || 0)
    orderSummary.unpaid = Number(summaryResult?.unpaid || 0)
    orderSummary.afterSales = Number(summaryResult?.afterSales || 0)
  } catch (error) {
    ElMessage.error(error.message || '获取订单数据失败')
  } finally {
    loading.value = false
  }
}

function openDetail(orderId) {
  selectedOrderId.value = String(orderId)
  detailDrawerVisible.value = true
}

function openConversation(orderId) {
  router.push(buildConversationPath('user', orderId))
}

watch(statusFilter, () => {
  if (currentPage.value !== 1) {
    resetPage()
    return
  }
  loadData()
})

watch([currentPage, pageSize], () => {
  loadData()
})

onMounted(loadData)
</script>
