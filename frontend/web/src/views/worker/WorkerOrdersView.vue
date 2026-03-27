<template>
  <div class="page-stack">
    <el-card shadow="never">
      <template #header>
        <div class="section-title">
          <div>
            <h1>服务订单工作台</h1>
            <p>集中处理分配给当前服务人员的订单，完成接单、开工和完工流转。</p>
          </div>
          <el-button @click="loadOrders">刷新</el-button>
        </div>
      </template>

      <el-table :data="orders" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单号" width="100" />
        <el-table-column prop="serviceName" label="服务项目" min-width="140" />
        <el-table-column prop="customerName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="bookingDate" label="预约日期" width="120" />
        <el-table-column prop="bookingSlot" label="时间段" width="130" />
        <el-table-column prop="serviceAddress" label="服务地址" min-width="220" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progressNote" label="最新进度" min-width="220" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-space v-if="normalizeStatus(row.status) === 'PENDING'">
              <el-button size="small" type="primary" @click="handleAction(row.id, 'accept')">接单</el-button>
              <el-button size="small" @click="handleAction(row.id, 'start')">直接开工</el-button>
            </el-space>
            <el-button
              v-else-if="normalizeStatus(row.status) === 'ACCEPTED'"
              size="small"
              type="warning"
              @click="handleAction(row.id, 'start')"
            >
              开始服务
            </el-button>
            <el-button
              v-else-if="normalizeStatus(row.status) === 'IN_SERVICE'"
              size="small"
              type="success"
              @click="handleAction(row.id, 'complete')"
            >
              完成服务
            </el-button>
            <span v-else class="muted-line">已完成</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  acceptWorkerOrder,
  completeWorkerOrder,
  fetchWorkerOrders,
  startWorkerOrder
} from '../../api'

const orders = ref([])
const loading = ref(false)

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

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await fetchWorkerOrders()
  } catch (error) {
    ElMessage.error(error.message || '获取服务订单失败')
  } finally {
    loading.value = false
  }
}

async function handleAction(id, action) {
  try {
    if (action === 'accept') {
      await acceptWorkerOrder(id)
    } else if (action === 'start') {
      await startWorkerOrder(id)
    } else {
      await completeWorkerOrder(id)
    }
    ElMessage.success('订单状态已更新')
    await loadOrders()
  } catch (error) {
    ElMessage.error(error.message || '订单流转失败')
  }
}

onMounted(loadOrders)
</script>
