<template>
  <section class="stack">
    <div class="section-head">
      <div>
        <p class="eyebrow">数据统计与分析</p>
        <h1>平台运营看板</h1>
      </div>
    </div>

    <article v-if="errorMessage" class="card">
      <h3>访问受限</h3>
      <p>{{ errorMessage }}</p>
      <RouterLink class="button" to="/login">前往登录</RouterLink>
    </article>

    <template v-else-if="dashboard">
      <div class="grid grid-4">
        <article class="card stat-card">
          <p>总订单量</p>
          <h2>{{ dashboard.totalOrders }}</h2>
        </article>
        <article class="card stat-card">
          <p>已完成订单</p>
          <h2>{{ dashboard.completedOrders }}</h2>
        </article>
        <article class="card stat-card">
          <p>活跃服务人员</p>
          <h2>{{ dashboard.activeWorkers }}</h2>
        </article>
        <article class="card stat-card">
          <p>平均满意度</p>
          <h2>{{ dashboard.averageRating }}</h2>
        </article>
      </div>
      <article class="card">
        <h3>各服务类型销量</h3>
        <div class="sales-list">
          <div v-for="(value, key) in dashboard.serviceSales" :key="key" class="sales-item">
            <span>{{ key }}</span>
            <div class="sales-bar">
              <div class="sales-bar-fill" :style="{ width: `${value * 2}%` }"></div>
            </div>
            <strong>{{ value }}</strong>
          </div>
        </div>
      </article>
    </template>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchAdminDashboard } from '../api'

const dashboard = ref(null)
const errorMessage = ref('')

onMounted(async () => {
  try {
    dashboard.value = await fetchAdminDashboard()
  } catch (error) {
    errorMessage.value = error.message || '当前账号无法访问管理员统计看板'
  }
})
</script>
