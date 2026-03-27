<template>
  <div class="page-stack">
    <el-card shadow="never">
      <div class="section-title">
        <div>
          <h1>订单进度跟踪</h1>
          <p>查看预约状态、服务地址、联系人和最新进度。</p>
        </div>
      </div>
      <el-table :data="orders" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单号" width="100" />
        <el-table-column prop="serviceName" label="服务项目" min-width="140" />
        <el-table-column prop="workerName" label="服务人员" width="120" />
        <el-table-column prop="bookingDate" label="预约日期" width="120" />
        <el-table-column prop="bookingSlot" label="时段" width="120" />
        <el-table-column prop="serviceAddress" label="服务地址" min-width="220" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === '服务中' ? 'warning' : 'success'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progressNote" label="最新进度" min-width="220" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchOrders } from '../../api'

const orders = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    orders.value = await fetchOrders()
  } finally {
    loading.value = false
  }
})
</script>
