<template>
  <div class="page-stack">
    <div class="console-overview console-overview--worker qualification-hero">
      <div>
        <el-tag type="warning" round>资质资料</el-tag>
        <h1>维护服务人员资质与审核材料</h1>
        <p>服务人员在这里统一提交擅长服务、证书、服务区域和可服务时段，供管理员审核。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="submitApplication">提交资质资料</el-button>
        <el-button plain @click="loadApplications">刷新审核记录</el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="累计提交次数" :value="applications.length" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="待审核" :value="pendingCount" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="已通过" :value="approvedCount" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="已驳回" :value="rejectedCount" />
      </el-card>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :xl="15">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-between">
              <div>
                <strong>资质资料表单</strong>
                <p class="muted-line">提交前请确认联系方式、服务范围和证书信息完整准确。</p>
              </div>
              <el-tag type="warning">服务人员端</el-tag>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-row :gutter="16">
              <el-col :xs="24" :md="12">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model.trim="form.realName" placeholder="请输入真实姓名" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="联系电话" prop="phone">
                  <el-input v-model.trim="form.phone" placeholder="请输入手机号" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="16">
              <el-col :xs="24" :md="12">
                <el-form-item label="服务类型">
                  <el-select
                    v-model="form.serviceTypes"
                    multiple
                    collapse-tags
                    collapse-tags-tooltip
                    placeholder="请选择擅长服务"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="item in serviceTypeOptions"
                      :key="item"
                      :label="item"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="从业年限">
                  <el-input-number v-model="form.yearsOfExperience" :min="0" :max="40" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="16">
              <el-col :xs="24" :md="12">
                <el-form-item label="证书 / 培训经历">
                  <el-select
                    v-model="form.certificates"
                    multiple
                    filterable
                    allow-create
                    default-first-option
                    collapse-tags
                    collapse-tags-tooltip
                    placeholder="请输入证书名称"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="item in certificateOptions"
                      :key="item"
                      :label="item"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="服务区域">
                  <el-select
                    v-model="form.serviceAreas"
                    multiple
                    filterable
                    allow-create
                    default-first-option
                    collapse-tags
                    collapse-tags-tooltip
                    placeholder="请选择或输入服务区域"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="item in areaOptions"
                      :key="item"
                      :label="item"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="可服务时段">
              <el-checkbox-group v-model="form.availableSchedule" class="slot-group">
                <el-checkbox-button
                  v-for="slot in scheduleOptions"
                  :key="slot"
                  :label="slot"
                >
                  {{ slot }}
                </el-checkbox-button>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item label="个人介绍">
              <el-input
                v-model.trim="form.intro"
                type="textarea"
                :rows="5"
                maxlength="300"
                show-word-limit
                placeholder="请简要说明擅长领域、服务经验和服务风格"
              />
            </el-form-item>

            <el-alert
              v-if="latestApplication?.status === 'REJECTED' && latestApplication?.adminRemark"
              :title="`最近一次驳回说明：${latestApplication.adminRemark}`"
              type="warning"
              show-icon
              :closable="false"
            />

            <div class="form-actions">
              <el-button type="primary" @click="submitApplication">提交资质资料</el-button>
              <el-button @click="resetFormFromLatest">恢复最近一次提交内容</el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <el-card shadow="never">
          <template #header><strong>当前审核状态</strong></template>
          <el-empty v-if="!latestApplication" description="还没有资质提交记录" />
          <div v-else class="status-panel">
            <el-tag size="large" :type="statusType(latestApplication.status)">
              {{ statusLabel(latestApplication.status) }}
            </el-tag>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="最近提交时间">
                {{ latestApplication.createdAt || '--' }}
              </el-descriptions-item>
              <el-descriptions-item label="服务类型">
                {{ latestApplication.serviceTypes || '--' }}
              </el-descriptions-item>
              <el-descriptions-item label="服务区域">
                {{ latestApplication.serviceAreas || '--' }}
              </el-descriptions-item>
              <el-descriptions-item label="可服务时段">
                {{ latestApplication.availableSchedule || '--' }}
              </el-descriptions-item>
              <el-descriptions-item label="审核备注">
                {{ latestApplication.adminRemark || '暂无' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>

        <el-card shadow="never">
          <template #header><strong>审核记录</strong></template>
          <el-timeline v-if="applications.length">
            <el-timeline-item
              v-for="item in applications"
              :key="item.id"
              :type="statusType(item.status)"
              :timestamp="item.createdAt"
            >
              <div class="timeline-title">
                {{ statusLabel(item.status) }}
              </div>
              <p>服务类型：{{ item.serviceTypes || '--' }}</p>
              <p>服务区域：{{ item.serviceAreas || '--' }}</p>
              <p v-if="item.adminRemark">审核备注：{{ item.adminRemark }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无审核时间线" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMyWorkerApplications, submitWorkerApplication } from '../../api'
import { authStore } from '../../stores/auth'

const serviceTypeOptions = ['日常保洁', '深度清洁', '家电清洗', '收纳整理', '做饭阿姨', '老人陪护', '母婴护理']
const certificateOptions = ['健康证', '育婴师证', '母婴护理证', '养老护理员证', '家政培训结业证']
const areaOptions = ['浦东新区', '闵行区', '徐汇区', '静安区', '黄浦区', '杨浦区', '宝山区']
const scheduleOptions = ['工作日白天', '工作日晚间', '周末白天', '周末晚间', '节假日可接单']

const formRef = ref(null)
const applications = ref([])
const form = reactive({
  realName: authStore.state.user?.realName || '',
  phone: authStore.state.user?.phone || '',
  serviceTypes: [],
  yearsOfExperience: 0,
  certificates: [],
  serviceAreas: [],
  availableSchedule: [],
  intro: ''
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入有效的 11 位手机号', trigger: 'blur' }
  ]
}

const latestApplication = computed(() => applications.value[0] || null)
const pendingCount = computed(() => applications.value.filter((item) => item.status === 'PENDING').length)
const approvedCount = computed(() => applications.value.filter((item) => item.status === 'APPROVED').length)
const rejectedCount = computed(() => applications.value.filter((item) => item.status === 'REJECTED').length)

function splitValues(value) {
  return String(value || '')
    .split(/[、,，;；\n]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function statusType(status) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

function statusLabel(status) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  return '待审核'
}

function syncForm(application) {
  if (!application) {
    return
  }
  form.realName = application.realName || authStore.state.user?.realName || ''
  form.phone = application.phone || authStore.state.user?.phone || ''
  form.serviceTypes = splitValues(application.serviceTypes)
  form.yearsOfExperience = Number(application.yearsOfExperience || 0)
  form.certificates = splitValues(application.certificates)
  form.serviceAreas = splitValues(application.serviceAreas)
  form.availableSchedule = splitValues(application.availableSchedule)
  form.intro = application.intro || ''
}

function resetFormFromLatest() {
  if (latestApplication.value) {
    syncForm(latestApplication.value)
    return
  }
  form.realName = authStore.state.user?.realName || ''
  form.phone = authStore.state.user?.phone || ''
  form.serviceTypes = []
  form.yearsOfExperience = 0
  form.certificates = []
  form.serviceAreas = []
  form.availableSchedule = []
  form.intro = ''
}

function validateWorkerFields() {
  if (!form.serviceTypes.length) {
    ElMessage.error('请选择至少一个服务类型')
    return false
  }
  if (!form.serviceAreas.length) {
    ElMessage.error('请填写至少一个服务区域')
    return false
  }
  if (!form.availableSchedule.length) {
    ElMessage.error('请选择至少一个可服务时段')
    return false
  }
  if (!form.intro.trim()) {
    ElMessage.error('请填写个人介绍')
    return false
  }
  return true
}

async function loadApplications() {
  applications.value = await fetchMyWorkerApplications().catch(() => [])
  if (applications.value.length) {
    syncForm(applications.value[0])
  }
}

async function submitApplication() {
  try {
    await formRef.value?.validate()
    if (!validateWorkerFields()) {
      return
    }

    await submitWorkerApplication({
      realName: form.realName,
      phone: form.phone,
      serviceTypes: form.serviceTypes.join('、'),
      yearsOfExperience: form.yearsOfExperience,
      certificates: form.certificates.join('、'),
      serviceAreas: form.serviceAreas.join('、'),
      availableSchedule: form.availableSchedule.join('；'),
      intro: form.intro
    })
    ElMessage.success('资质资料已提交')
    await loadApplications()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}

onMounted(loadApplications)
</script>

<style scoped>
.qualification-hero {
  align-items: center;
}

.slot-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.form-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.status-panel {
  display: grid;
  gap: 16px;
}

.timeline-title {
  font-weight: 600;
  margin-bottom: 6px;
}
</style>
