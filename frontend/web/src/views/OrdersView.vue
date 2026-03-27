<template>
  <section class="stack">
    <div class="section-head">
      <div>
        <p class="eyebrow">订单进度跟踪</p>
        <h1>查看预约状态与服务进度</h1>
      </div>
    </div>
    <div class="grid">
      <article v-for="order in orders" :key="order.id" class="card order-card">
        <div class="worker-top">
          <div>
            <p class="eyebrow">订单 #{{ order.id }}</p>
            <h3>{{ order.serviceName }} · {{ order.workerName }}</h3>
          </div>
          <strong class="status-pill">{{ order.status }}</strong>
        </div>
        <p>预约时间：{{ order.bookingDate }} {{ order.bookingSlot }}</p>
        <p>服务地址：{{ order.serviceAddress }}</p>
        <p>联系人：{{ order.customerName }} / {{ order.contactPhone }}</p>
        <p>进度说明：{{ order.progressNote }}</p>
        <p>需求备注：{{ order.remark }}</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchOrders } from '../api'

const orders = ref([])

onMounted(async () => {
  orders.value = await fetchOrders()
})
</script>
