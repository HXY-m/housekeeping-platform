<template>
  <div class="page-stack booking-page">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">预约下单</h1>
          <p class="page-panel__desc">按步骤填写服务时间、联系信息和需求说明，系统会实时校验可预约时段。</p>
        </div>
      </div>

      <el-steps :active="activeStep" finish-status="success" simple class="booking-steps">
        <el-step title="选择时间" />
        <el-step title="填写地址" />
        <el-step title="提交需求" />
      </el-steps>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="booking-side-card">
          <template #header>
            <div class="card-header-between">
              <strong>服务人员信息</strong>
              <el-tag type="success" round>{{ worker?.roleLabel || '服务人员' }}</el-tag>
            </div>
          </template>

          <div v-if="worker" class="booking-worker-preview booking-worker-preview--trust">
            <el-avatar :src="getWorkerImage(worker)" :size="88" />
            <div class="booking-worker-preview__head">
              <h2>{{ worker.name }}</h2>
              <p class="muted-line">{{ worker.intro }}</p>
            </div>
            <div class="booking-worker-preview__badges">
              <span>评分 {{ worker.rating }}</span>
              <span>完成 {{ worker.completedOrders }} 单</span>
              <span>{{ formatCurrency(worker.hourlyPrice) }}/小时</span>
            </div>
            <div class="tag-wrap">
              <el-tag v-for="tag in worker.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
            </div>
          </div>

          <el-skeleton v-else :rows="6" animated />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-form :model="form" label-position="top" class="booking-form" @submit.prevent="submitOrder">
          <el-card shadow="never" class="booking-step-card" :class="{ 'booking-step-card--active': activeStep === 1 }">
            <template #header>
              <div class="booking-step-card__header">
                <strong>步骤 1：选择时间</strong>
                <span class="section-caption">先确定服务项目、日期和可预约时段</span>
              </div>
            </template>

            <el-row :gutter="16">
              <el-col :xs="24" :md="12">
                <el-form-item label="服务项目">
                  <el-select v-model="form.serviceName" placeholder="请选择服务项目" style="width: 100%">
                    <el-option v-for="item in serviceOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :xs="24" :md="12">
                <el-form-item label="服务人员">
                  <el-input :model-value="worker?.name || '加载中'" disabled />
                </el-form-item>
              </el-col>

              <el-col :xs="24" :md="12">
                <el-form-item label="预约日期">
                  <el-date-picker
                    v-model="form.bookingDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    placeholder="请选择预约日期"
                    style="width: 100%"
                    :disabled-date="disablePastDate"
                  />
                </el-form-item>
              </el-col>

              <el-col :xs="24" :md="12">
                <el-form-item label="可预约时段">
                  <div class="slot-picker" v-loading="slotLoading">
                    <el-radio-group v-model="form.bookingSlot" class="slot-grid">
                      <el-radio-button
                        v-for="slot in slotOptions"
                        :key="slot.value"
                        :label="slot.value"
                        :disabled="slot.disabled"
                      >
                        <span class="slot-title">{{ slot.label }}</span>
                        <span class="slot-meta">{{ slot.desc }}</span>
                      </el-radio-button>
                    </el-radio-group>
                    <p class="muted-line">{{ availabilityHint }}</p>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>

          <el-card shadow="never" class="booking-step-card" :class="{ 'booking-step-card--active': activeStep === 2 }">
            <template #header>
              <div class="booking-step-card__header">
                <strong>步骤 2：填写地址</strong>
                <span class="section-caption">优先选择已保存地址，减少重复填写</span>
              </div>
            </template>

            <el-alert
              v-if="addresses.length"
              title="已为你加载常用地址，选择后可快速带入联系人和上门地址。"
              type="success"
              show-icon
              :closable="false"
              style="margin-bottom: 16px"
            />

            <el-row :gutter="16">
              <el-col :xs="24">
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
                  <el-input v-model="form.customerName" placeholder="请输入联系人姓名" />
                </el-form-item>
              </el-col>

              <el-col :xs="24" :md="12">
                <el-form-item label="联系电话">
                  <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
                </el-form-item>
              </el-col>

              <el-col :xs="24">
                <el-form-item label="服务地址">
                  <el-input v-model="form.serviceAddress" placeholder="街道、小区、门牌号等详细地址" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>

          <el-card shadow="never" class="booking-step-card" :class="{ 'booking-step-card--active': activeStep === 3 }">
            <template #header>
              <div class="booking-step-card__header">
                <strong>步骤 3：提交需求</strong>
                <span class="section-caption">补充特殊要求后即可提交预约</span>
              </div>
            </template>

            <el-form-item label="需求说明">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                placeholder="例如：厨房重油污、卫生间除霉、空调拆洗 2 台等"
              />
            </el-form-item>

            <el-space wrap>
              <el-button type="primary" @click="submitOrder">提交预约</el-button>
              <el-button @click="router.push('/user/orders')">查看订单</el-button>
            </el-space>

            <el-alert
              v-if="successMessage"
              :title="successMessage"
              type="success"
              show-icon
              :closable="false"
              style="margin-top: 16px"
            />
          </el-card>
        </el-form>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  createOrder,
  fetchBookingAvailability,
  fetchUserAddresses,
  fetchUserProfile,
  fetchWorker
} from '../../api'
import { formatCurrency } from '../../utils/format'
import { getWorkerImage } from '../../utils/displayAssets'

const route = useRoute()
const router = useRouter()

const SLOT_META = {
  '08:00-10:00': { label: '上午', desc: '适合常规上门' },
  '10:00-12:00': { label: '午前', desc: '适合半日服务' },
  '13:00-15:00': { label: '下午', desc: '适合深度清洁' },
  '15:00-17:00': { label: '傍晚', desc: '适合下班前后预约' },
  '18:00-20:00': { label: '晚间', desc: '适合夜间上门' }
}

const DEFAULT_SLOT_ORDER = Object.keys(SLOT_META)
const defaultServiceOptions = ['日常保洁', '深度清洁', '母婴护理', '老人陪护', '家电清洗']

const successMessage = ref('')
const selectedAddressId = ref(null)
const addresses = ref([])
const worker = ref(null)
const slotLoading = ref(false)
const availableSlots = ref(DEFAULT_SLOT_ORDER)
const occupiedSlots = ref([])

const form = reactive({
  workerId: String(route.params.workerId || ''),
  serviceName: '',
  customerName: '',
  contactPhone: '',
  serviceAddress: '',
  bookingDate: '',
  bookingSlot: '',
  remark: ''
})

const serviceOptions = computed(() => (worker.value?.tags?.length ? worker.value.tags : defaultServiceOptions))

const activeStep = computed(() => {
  if (!form.bookingDate || !form.bookingSlot || !form.serviceName) {
    return 1
  }
  if (!form.customerName || !form.contactPhone || !form.serviceAddress) {
    return 2
  }
  return 3
})

const slotOptions = computed(() =>
  DEFAULT_SLOT_ORDER.map((value) => ({
    value,
    label: SLOT_META[value].label,
    desc: occupiedSlots.value.includes(value) ? '该时段已被预约' : SLOT_META[value].desc,
    disabled: !availableSlots.value.includes(value)
  }))
)

const availabilityHint = computed(() => {
  if (!form.bookingDate) {
    return '先选择预约日期，系统会自动查询该服务人员的可预约时段。'
  }
  if (slotLoading.value) {
    return '正在查询可预约时段...'
  }
  if (!availableSlots.value.length) {
    return '该日期暂无可预约时段，请更换其他日期。'
  }
  return `当前日期还有 ${availableSlots.value.length} 个可预约时段。`
})

function buildAddressLabel(address) {
  return `${address.addressTag || '常用地址'} / ${address.city} ${address.detailAddress}`
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

function disablePastDate(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}

function getTodayString() {
  return new Date().toISOString().slice(0, 10)
}

async function loadAvailability() {
  if (!form.workerId || !form.bookingDate) {
    availableSlots.value = DEFAULT_SLOT_ORDER
    occupiedSlots.value = []
    form.bookingSlot = ''
    return
  }

  slotLoading.value = true
  try {
    const result = await fetchBookingAvailability(form.workerId, form.bookingDate)
    availableSlots.value = result.availableSlots || []
    occupiedSlots.value = result.occupiedSlots || []
    if (!availableSlots.value.includes(form.bookingSlot)) {
      form.bookingSlot = availableSlots.value[0] || ''
    }
  } catch (error) {
    availableSlots.value = []
    occupiedSlots.value = DEFAULT_SLOT_ORDER
    form.bookingSlot = ''
    ElMessage.error(error.message || '查询可预约时段失败')
  } finally {
    slotLoading.value = false
  }
}

async function submitOrder() {
  if (!form.serviceName) {
    ElMessage.warning('请先选择服务项目')
    return
  }
  if (!form.bookingDate) {
    ElMessage.warning('请选择预约日期')
    return
  }
  if (!form.bookingSlot) {
    ElMessage.warning('请选择可预约时段')
    return
  }

  try {
    const order = await createOrder({
      ...form,
      workerId: Number(form.workerId)
    })
    successMessage.value = `预约成功，订单号 #${order.id}，应付金额 ${formatCurrency(order.payableAmount || 0)}，请前往订单中心完成支付。`
    ElMessage.success('预约成功')
  } catch (error) {
    ElMessage.error(error.message || '预约失败')
    await loadAvailability()
  }
}

watch(() => form.bookingDate, loadAvailability)

onMounted(async () => {
  try {
    const [workerDetail, profile, addressRows] = await Promise.all([
      fetchWorker(route.params.workerId),
      fetchUserProfile(),
      fetchUserAddresses()
    ])
    worker.value = workerDetail
    addresses.value = addressRows
    form.serviceName = workerDetail.tags?.[0] || defaultServiceOptions[0]
    form.bookingDate = getTodayString()

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

<style scoped>
.booking-steps {
  margin-top: 18px;
}

.booking-worker-preview--trust {
  display: grid;
  gap: 16px;
  align-items: start;
}

.booking-worker-preview--trust :deep(.el-avatar) {
  border: 2px solid #fff;
  box-shadow: 0 12px 24px rgba(16, 24, 40, 0.08);
}

.booking-worker-preview__head {
  display: grid;
  gap: 8px;
}

.booking-worker-preview__head h2 {
  margin: 0;
}

.booking-worker-preview__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.booking-worker-preview__badges span {
  padding: 8px 12px;
  border-radius: 999px;
  background: #f8fafc;
  color: #344054;
  font-size: 12px;
}

.booking-step-card {
  border: 1px solid #e4e7ec;
}

.booking-step-card--active {
  border-color: rgba(0, 113, 227, 0.24);
  box-shadow: 0 12px 24px rgba(16, 24, 40, 0.04);
}

.booking-step-card__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

@media (max-width: 768px) {
  .booking-step-card__header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
