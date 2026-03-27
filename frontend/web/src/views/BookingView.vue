<template>
  <section class="stack">
    <div class="section-head">
      <div>
        <p class="eyebrow">在线预约与下单</p>
        <h1>填写预约信息，生成服务订单</h1>
      </div>
    </div>

    <form class="booking-form card" @submit.prevent="submitOrder">
      <div class="grid grid-2">
        <label>
          <span>服务人员 ID</span>
          <input v-model="form.workerId" type="number" required />
        </label>
        <label>
          <span>服务项目</span>
          <select v-model="form.serviceName" required>
            <option>日常保洁</option>
            <option>深度清洁</option>
            <option>母婴护理</option>
            <option>老人陪护</option>
            <option>家电清洗</option>
          </select>
        </label>
        <label>
          <span>联系人</span>
          <input v-model="form.customerName" required />
        </label>
        <label>
          <span>联系电话</span>
          <input v-model="form.contactPhone" required />
        </label>
        <label>
          <span>预约日期</span>
          <input v-model="form.bookingDate" type="date" required />
        </label>
        <label>
          <span>时间段</span>
          <input v-model="form.bookingSlot" placeholder="如 09:00-12:00" required />
        </label>
      </div>
      <label>
        <span>服务地址</span>
        <input v-model="form.serviceAddress" required />
      </label>
      <label>
        <span>需求说明</span>
        <textarea v-model="form.remark" rows="4" required />
      </label>
      <div class="form-actions">
        <button class="button" type="submit">提交预约</button>
        <RouterLink class="button button-ghost" to="/orders">查看订单</RouterLink>
      </div>
      <p v-if="successMessage" class="success-text">{{ successMessage }}</p>
    </form>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { createOrder } from '../api'

const route = useRoute()
const successMessage = ref('')
const form = reactive({
  workerId: Number(route.params.workerId),
  serviceName: '日常保洁',
  customerName: '',
  contactPhone: '',
  serviceAddress: '',
  bookingDate: '',
  bookingSlot: '',
  remark: ''
})

async function submitOrder() {
  const order = await createOrder({
    ...form,
    workerId: Number(form.workerId)
  })
  successMessage.value = `预约成功，订单号 #${order.id}，当前状态：${order.status}`
}
</script>
