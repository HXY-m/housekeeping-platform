<template>
  <div class="page-stack">
    <el-card shadow="never">
      <div class="section-title">
        <div>
          <h1>在线预约与下单</h1>
          <p>填写服务时间、地址和需求说明，生成预约订单。</p>
        </div>
      </div>

      <el-form :model="form" label-position="top" class="booking-form" @submit.prevent="submitOrder">
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="服务人员 ID">
              <el-input v-model="form.workerId" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="服务项目">
              <el-select v-model="form.serviceName" placeholder="请选择服务项目">
                <el-option label="日常保洁" value="日常保洁" />
                <el-option label="深度清洁" value="深度清洁" />
                <el-option label="母婴护理" value="母婴护理" />
                <el-option label="老人陪护" value="老人陪护" />
                <el-option label="家电清洗" value="家电清洗" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="联系人">
              <el-input v-model="form.customerName" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="预约日期">
              <el-input v-model="form.bookingDate" placeholder="例如 2026-03-29" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="时间段">
              <el-input v-model="form.bookingSlot" placeholder="例如 09:00-12:00" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="服务地址">
          <el-input v-model="form.serviceAddress" />
        </el-form-item>
        <el-form-item label="需求说明">
          <el-input v-model="form.remark" type="textarea" :rows="4" />
        </el-form-item>
        <el-space wrap>
          <el-button type="primary" @click="submitOrder">提交预约</el-button>
          <el-button @click="router.push('/orders')">查看订单</el-button>
        </el-space>
        <el-alert v-if="successMessage" :title="successMessage" type="success" show-icon :closable="false" />
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOrder } from '../../api'

const route = useRoute()
const router = useRouter()
const successMessage = ref('')
const form = reactive({
  workerId: String(route.params.workerId || ''),
  serviceName: '日常保洁',
  customerName: '',
  contactPhone: '',
  serviceAddress: '',
  bookingDate: '',
  bookingSlot: '',
  remark: ''
})

async function submitOrder() {
  try {
    const order = await createOrder({
      ...form,
      workerId: Number(form.workerId)
    })
    successMessage.value = `预约成功，订单号 #${order.id}，当前状态：${order.status}`
    ElMessage.success('预约成功')
  } catch (error) {
    ElMessage.error(error.message || '预约失败')
  }
}
</script>
