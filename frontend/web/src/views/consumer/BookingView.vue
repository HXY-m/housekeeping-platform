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
        <el-alert
          v-if="addresses.length"
          title="已为你加载常用地址，可直接选择默认地址快速填充联系人与上门地址。"
          type="success"
          show-icon
          :closable="false"
        />

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
            <el-col :xs="24" :md="24">
              <el-form-item label="常用地址">
                <div class="booking-address-row">
                  <el-select
                    v-model="selectedAddressId"
                    placeholder="选择已保存的常用地址"
                    style="width: min(100%, 420px)"
                    @change="handleAddressSelect"
                  >
                    <el-option
                      v-for="item in addresses"
                      :key="item.id"
                      :label="buildAddressLabel(item)"
                      :value="item.id"
                    />
                  </el-select>
                  <el-button plain @click="router.push('/user/profile')">管理地址簿</el-button>
                </div>
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
          <el-button @click="router.push('/user/orders')">查看订单</el-button>
        </el-space>
        <el-alert v-if="successMessage" :title="successMessage" type="success" show-icon :closable="false" />
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOrder, fetchUserAddresses, fetchUserProfile } from '../../api'

const route = useRoute()
const router = useRouter()
const successMessage = ref('')
const selectedAddressId = ref(null)
const addresses = ref([])
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

function buildAddressLabel(address) {
  return `${address.addressTag || '常用地址'} · ${address.city} ${address.detailAddress}`
}

function applyAddress(address) {
  if (!address) {
    return
  }
  form.customerName = address.contactName
  form.contactPhone = address.contactPhone
  form.serviceAddress = `${address.city} ${address.detailAddress}`.trim()
}

function handleAddressSelect(addressId) {
  const target = addresses.value.find((item) => item.id === addressId)
  applyAddress(target)
}

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

onMounted(async () => {
  try {
    const [profile, addressRows] = await Promise.all([
      fetchUserProfile(),
      fetchUserAddresses()
    ])
    addresses.value = addressRows

    if (!form.customerName) {
      form.customerName = profile.realName || ''
    }
    if (!form.contactPhone) {
      form.contactPhone = profile.phone || ''
    }

    const defaultAddress = addressRows.find((item) => item.defaultAddress) || addressRows[0]
    if (defaultAddress) {
      selectedAddressId.value = defaultAddress.id
      applyAddress(defaultAddress)
    }
  } catch {
    addresses.value = []
  }
})
</script>
