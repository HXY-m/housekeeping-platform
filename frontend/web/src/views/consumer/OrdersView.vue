<template>
  <div class="page-stack">
    <el-card shadow="never">
      <div class="section-title">
        <div>
          <h1>我的订单</h1>
          <p>查看预约进度，并在服务完成后提交评价。</p>
        </div>
        <el-button @click="loadOrders">刷新</el-button>
      </div>

      <el-table :data="orders" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单号" width="100" />
        <el-table-column prop="serviceName" label="服务项目" min-width="140" />
        <el-table-column prop="workerName" label="服务人员" width="120" />
        <el-table-column prop="bookingDate" label="预约日期" width="120" />
        <el-table-column prop="bookingSlot" label="时间段" width="130" />
        <el-table-column prop="serviceAddress" label="服务地址" min-width="220" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
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
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="canReview(row)"
              size="small"
              type="primary"
              @click="openReview(row)"
            >
              去评价
            </el-button>
            <span v-else class="muted-line">无可用操作</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="提交订单评价" width="520px">
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchOrders, submitOrderReview } from '../../api'

const orders = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const selectedOrderId = ref(null)
const reviewForm = reactive({
  rating: 5,
  content: ''
})

const STATUS_LABEL_MAP = {
  PENDING: '待接单',
  ACCEPTED: '已接单',
  IN_SERVICE: '服务中',
  COMPLETED: '已完成'
}

function normalizeStatus(status) {
  if (!status) return 'PENDING'
  if (STATUS_LABEL_MAP[status]) return status
  if (status === '待服务' || status === '待接单') return 'PENDING'
  if (status === '已接单') return 'ACCEPTED'
  if (status === '服务中') return 'IN_SERVICE'
  if (status === '已完成') return 'COMPLETED'
  return status
}

function statusLabel(status) {
  const code = normalizeStatus(status)
  return STATUS_LABEL_MAP[code] || status
}

function statusTagType(status) {
  const code = normalizeStatus(status)
  if (code === 'PENDING') return 'info'
  if (code === 'ACCEPTED') return 'warning'
  if (code === 'IN_SERVICE') return 'primary'
  if (code === 'COMPLETED') return 'success'
  return 'info'
}

function canReview(order) {
  return normalizeStatus(order.status) === 'COMPLETED' && !order.reviewed
}

function openReview(order) {
  selectedOrderId.value = order.id
  reviewForm.rating = 5
  reviewForm.content = ''
  dialogVisible.value = true
}

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await fetchOrders()
  } catch (error) {
    ElMessage.error(error.message || '获取订单失败')
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
    await submitOrderReview(selectedOrderId.value, {
      rating: reviewForm.rating,
      content: reviewForm.content.trim()
    })
    ElMessage.success('评价提交成功')
    dialogVisible.value = false
    await loadOrders()
  } catch (error) {
    ElMessage.error(error.message || '提交评价失败')
  }
}

onMounted(loadOrders)
</script>
