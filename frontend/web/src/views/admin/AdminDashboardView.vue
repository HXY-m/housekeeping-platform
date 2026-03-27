<template>
  <div class="page-stack">
    <el-row :gutter="16">
      <el-col :xs="24" :md="6">
        <el-card><el-statistic title="总订单量" :value="dashboard.totalOrders" /></el-card>
      </el-col>
      <el-col :xs="24" :md="6">
        <el-card><el-statistic title="已完成订单" :value="dashboard.completedOrders" /></el-card>
      </el-col>
      <el-col :xs="24" :md="6">
        <el-card><el-statistic title="活跃服务人员" :value="dashboard.activeWorkers" /></el-card>
      </el-col>
      <el-col :xs="24" :md="6">
        <el-card><el-statistic title="平均满意度" :value="dashboard.averageRating" /></el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>服务类型销量</template>
      <el-table :data="salesRows">
        <el-table-column prop="name" label="服务类型" />
        <el-table-column prop="count" label="销量" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchAdminDashboard } from '../../api'

const dashboard = ref({
  totalOrders: 0,
  completedOrders: 0,
  activeWorkers: 0,
  averageRating: 0,
  serviceSales: {}
})

const salesRows = computed(() =>
  Object.entries(dashboard.value.serviceSales || {}).map(([name, count]) => ({ name, count }))
)

onMounted(async () => {
  dashboard.value = await fetchAdminDashboard()
})
</script>
