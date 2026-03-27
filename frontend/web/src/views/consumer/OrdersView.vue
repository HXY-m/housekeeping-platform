<template>
  <div class="page-stack">
    <el-card shadow="never">
      <div class="section-title">
        <div>
          <h1>我的订单</h1>
          <p>查看预约进度、提交评价，并在出现问题时发起售后反馈。</p>
        </div>
        <el-button @click="loadData">刷新</el-button>
      </div>

      <el-table :data="orderRows" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单号" width="100" />
        <el-table-column prop="serviceName" label="服务项目" min-width="140" />
        <el-table-column prop="workerName" label="服务人员" width="120" />
        <el-table-column prop="bookingDate" label="预约日期" width="120" />
        <el-table-column prop="bookingSlot" label="时间段" width="130" />
        <el-table-column prop="serviceAddress" label="服务地址" min-width="220" />
        <el-table-column label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusTagType(row.status)">{{ getOrderStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progressNote" label="最新进度" min-width="220" />
        <el-table-column label="评价" min-width="180">
          <template #default="{ row }">
            <template v-if="row.reviewed">
              <div class="review-summary">
                <el-rate :model-value="row.reviewRating" disabled />
                <span>{{ row.reviewContent }}</span>
              </div>
            </template>
            <span v-else class="muted-line">暂未评价</span>
          </template>
        </el-table-column>
        <el-table-column label="售后状态" min-width="180">
          <template #default="{ row }">
            <template v-if="row.afterSale">
              <div class="stack-cell">
                <el-tag :type="afterSaleTagType(row.afterSale.status)">{{ afterSaleStatusLabel(row.afterSale.status) }}</el-tag>
                <span class="muted-line">{{ row.afterSale.issueType }}</span>
              </div>
            </template>
            <span v-else class="muted-line">未发起售后</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-space wrap>
              <el-button
                v-if="canReview(row)"
                size="small"
                type="primary"
                @click="openReview(row)"
              >
                去评价
              </el-button>
              <el-button
                v-if="canCreateAfterSale(row)"
                size="small"
                plain
                @click="openAfterSale(row)"
              >
                申请售后
              </el-button>
              <span v-if="!canReview(row) && !canCreateAfterSale(row)" class="muted-line">无可用操作</span>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="reviewDialogVisible" title="提交订单评价" width="520px">
      <el-form label-position="top">
        <el-form-item label="服务评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="说说本次服务体验，例如是否准时、是否细致、沟通是否顺畅。"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="afterSaleDialogVisible" title="提交售后反馈" width="560px">
      <el-form label-position="top">
        <el-form-item label="问题类型">
          <el-select v-model="afterSaleForm.issueType" style="width: 100%">
            <el-option label="服务质量不达标" value="服务质量不达标" />
            <el-option label="服务人员迟到或爽约" value="服务人员迟到或爽约" />
            <el-option label="收费或沟通争议" value="收费或沟通争议" />
            <el-option label="物品损坏或安全问题" value="物品损坏或安全问题" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="afterSaleForm.contactPhone" placeholder="便于管理员回访沟通" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input
            v-model="afterSaleForm.content"
            type="textarea"
            :rows="5"
            maxlength="300"
            show-word-limit
            placeholder="请尽量描述清楚问题经过、影响范围和你的诉求。"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="afterSaleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAfterSale">提交售后</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createAfterSale,
  fetchMyAfterSales,
  fetchOrders,
  submitOrderReview
} from '../../api'
import {
  getOrderStatusLabel,
  getOrderStatusTagType,
  normalizeOrderStatus
} from '../../utils/order'

const orders = ref([])
const afterSales = ref([])
const loading = ref(false)

const reviewDialogVisible = ref(false)
const reviewOrderId = ref(null)
const reviewForm = reactive({
  rating: 5,
  content: ''
})

const afterSaleDialogVisible = ref(false)
const afterSaleOrderId = ref(null)
const afterSaleForm = reactive({
  issueType: '服务质量不达标',
  content: '',
  contactPhone: ''
})

const afterSaleMap = computed(() =>
  Object.fromEntries((afterSales.value || []).map((item) => [item.orderId, item]))
)

const orderRows = computed(() =>
  (orders.value || []).map((item) => ({
    ...item,
    afterSale: afterSaleMap.value[item.id] || null
  }))
)

function afterSaleStatusLabel(status) {
  if (status === 'PENDING') return '待处理'
  if (status === 'PROCESSING') return '处理中'
  if (status === 'RESOLVED') return '已解决'
  if (status === 'REJECTED') return '已驳回'
  return status
}

function afterSaleTagType(status) {
  if (status === 'PENDING') return 'warning'
  if (status === 'PROCESSING') return 'primary'
  if (status === 'RESOLVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

function canReview(order) {
  return normalizeOrderStatus(order.status) === 'COMPLETED' && !order.reviewed
}

function canCreateAfterSale(order) {
  if (order.afterSale) return false
  return normalizeOrderStatus(order.status) !== 'PENDING'
}

function openReview(order) {
  reviewOrderId.value = order.id
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewDialogVisible.value = true
}

function openAfterSale(order) {
  afterSaleOrderId.value = order.id
  afterSaleForm.issueType = '服务质量不达标'
  afterSaleForm.content = ''
  afterSaleForm.contactPhone = order.contactPhone || ''
  afterSaleDialogVisible.value = true
}

async function loadData() {
  loading.value = true
  try {
    const [orderResult, afterSaleResult] = await Promise.all([
      fetchOrders(),
      fetchMyAfterSales()
    ])
    orders.value = orderResult
    afterSales.value = afterSaleResult
  } catch (error) {
    ElMessage.error(error.message || '获取订单数据失败')
  } finally {
    loading.value = false
  }
}

async function submitReview() {
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }

  try {
    await submitOrderReview(reviewOrderId.value, {
      rating: reviewForm.rating,
      content: reviewForm.content.trim()
    })
    ElMessage.success('评价提交成功')
    reviewDialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '提交评价失败')
  }
}

async function submitAfterSale() {
  if (!afterSaleForm.content.trim()) {
    ElMessage.warning('请填写问题描述')
    return
  }

  try {
    await createAfterSale({
      orderId: afterSaleOrderId.value,
      issueType: afterSaleForm.issueType,
      content: afterSaleForm.content.trim(),
      contactPhone: afterSaleForm.contactPhone.trim()
    })
    ElMessage.success('售后反馈已提交')
    afterSaleDialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '提交售后失败')
  }
}

onMounted(loadData)
</script>
