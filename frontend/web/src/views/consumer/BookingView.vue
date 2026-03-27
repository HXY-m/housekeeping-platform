<template>
  <div class="page-stack booking-page">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="booking-side-card">
          <template #header>
            <div class="card-header-between">
              <strong>服务人员信息</strong>
              <el-tag type="success" round>{{ worker?.roleLabel || '服务人员' }}</el-tag>
            </div>
          </template>

          <div v-if="worker" class="booking-worker-preview">
            <h2>{{ worker.name }}</h2>
            <p class="muted-line">{{ worker.intro }}</p>
            <div class="tag-wrap">
              <el-tag v-for="tag in worker.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
            </div>
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="服务评分">{{ worker.rating }}</el-descriptions-item>
              <el-descriptions-item label="完成订单">{{ worker.completedOrders }}</el-descriptions-item>
              <el-descriptions-item label="参考单价">{{ formatCurrency(worker.hourlyPrice) }}/小时</el-descriptions-item>
              <el-descriptions-item label="最近可约">{{ worker.nextAvailable }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <el-skeleton v-else :rows="6" animated />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <div class="section-title">
            <div>
              <h1>在线预约下单</h1>
              <p>先选择日期与可预约时段，再补充联系信息和需求说明，系统会自动校验该时段是否还能预约。</p>
            </div>
          </div>

          <el-form :model="form" label-position="top" class="booking-form" @submit.prevent="submitOrder">
            <el-alert
              v-if="addresses.length"
              title="已为你加载常用地址，选择后可快速带入联系人和上门地址。"
              type="success"
              show-icon
              :closable="false"
            />

            <el-row :gutter="16">
              <el-col :xs="24" :md="12">
                <el-form-item label="服务项目">
                  <el-select v-model="form.serviceName" placeholder="请选择服务项目" style="width: 100%">
                    <el-option
                      v-for="item in serviceOptions"
                      :key="item"
                      :label="item"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :xs="24" :md="12">
                <el-form-item label="服务人员">
                  <el-input :model-value="worker?.name || '加载中'" disabled />
                </el-form-item>
              </el-col>

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
                    <p class="muted-line">
                      {{ availabilityHint }}
                    </p>
                  </div>
                </el-form-item>
              </el-col>

              <el-col :xs="24">
                <el-form-item label="服务地址">
                  <el-input v-model="form.serviceAddress" placeholder="街道、小区、门牌号等详细地址" />
                </el-form-item>
              </el-col>

              <el-col :xs="24">
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
              </el-col>
            </el-row>

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
            />
          </el-form>
        </el-card>
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
    successMessage.value = `预约成功，订单号 #${order.id}，当前状态：${order.status}`
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
