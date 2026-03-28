<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">服务人员资质申请</h1>
          <p class="page-panel__desc">
            请同时提交服务介绍、服务范围、可接单时段与资质文件。审核通过后，资料才会出现在前台并开放接单。
          </p>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="submitApplication">提交资质申请</el-button>
          <el-button @click="loadApplications">刷新记录</el-button>
        </div>
      </div>

      <div class="metric-strip">
        <div class="metric-chip">
          <span class="metric-chip__label">累计提交</span>
          <strong>{{ applications.length }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">待审核</span>
          <strong>{{ pendingCount }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">已通过</span>
          <strong>{{ approvedCount }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">已驳回</span>
          <strong>{{ rejectedCount }}</strong>
        </div>
      </div>
    </el-card>

    <div class="page-grid--sidebar">
      <el-card shadow="never">
        <template #header>
          <div class="card-header-between">
            <div>
              <strong>申请表单</strong>
              <p class="muted-line">展示头像将用于前台推荐服务人员卡片，可与资质文件一并提交。</p>
            </div>
            <el-tag type="warning">服务人员端</el-tag>
          </div>
        </template>

        <div class="profile-avatar-panel worker-avatar-panel">
          <el-avatar :src="avatarPreview" :size="88" />
          <div class="worker-avatar-panel__meta">
            <strong>展示头像</strong>
            <span class="muted-line">建议上传清晰的半身照或职业照，大小不超过 5MB。</span>
            <div class="hero-actions">
              <el-button plain :loading="uploadingAvatar" @click="openAvatarPicker">上传头像</el-button>
              <el-button text @click="clearAvatar">恢复默认</el-button>
            </div>
          </div>
        </div>

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
                  <el-option v-for="item in serviceTypeOptions" :key="item" :label="item" :value="item" />
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
              <el-form-item label="资质标签">
                <el-select
                  v-model="form.certificateLabels"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  collapse-tags
                  collapse-tags-tooltip
                  placeholder="例如：健康证、母婴护理证"
                  style="width: 100%"
                >
                  <el-option v-for="item in certificateOptions" :key="item" :label="item" :value="item" />
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
                  <el-option v-for="item in areaOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="可接单时段">
            <el-checkbox-group v-model="form.availableSchedule" class="slot-group">
              <el-checkbox-button v-for="slot in scheduleOptions" :key="slot" :label="slot">
                {{ slot }}
              </el-checkbox-button>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="资质附件">
            <div class="page-stack full-width">
              <el-upload
                v-model:file-list="proofFileList"
                :auto-upload="false"
                :limit="maxProofCount"
                accept=".jpg,.jpeg,.png,.webp,.pdf,.doc,.docx,.txt"
                :before-upload="beforeProofSelect"
                :on-change="handleProofChange"
                :on-remove="handleProofRemove"
                :on-exceed="handleProofExceed"
              >
                <el-button type="primary" plain>选择资质文件</el-button>
              </el-upload>
              <span class="proof-upload-tip">
                至少上传 1 份证明文件，支持图片、PDF、Word 或文本文件，单个文件不超过 10MB。
              </span>
              <FileAttachmentList :files="normalizedProofFiles" empty-text="尚未选择资质附件" />
            </div>
          </el-form-item>

          <el-form-item label="个人介绍">
            <el-input
              v-model.trim="form.intro"
              type="textarea"
              :rows="5"
              maxlength="300"
              show-word-limit
              placeholder="简要说明擅长领域、服务经验和服务风格"
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
            <el-button type="primary" @click="submitApplication">提交资质申请</el-button>
            <el-button @click="resetFormFromLatest">恢复最近一次资料</el-button>
          </div>
        </el-form>

        <input
          ref="avatarInputRef"
          type="file"
          accept="image/*"
          class="visually-hidden-input"
          @change="handleAvatarChange"
        />
      </el-card>

      <div class="page-stack">
        <el-card shadow="never">
          <template #header><strong>当前审核状态</strong></template>
          <el-empty v-if="!latestApplication" description="还没有资质申请记录" />
          <div v-else class="status-panel">
            <el-tag size="large" :type="statusType(latestApplication.status)">
              {{ statusLabel(latestApplication.status) }}
            </el-tag>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="最近提交时间">{{ latestApplication.createdAt || '--' }}</el-descriptions-item>
              <el-descriptions-item label="服务类型">{{ latestApplication.serviceTypes || '--' }}</el-descriptions-item>
              <el-descriptions-item label="服务区域">{{ latestApplication.serviceAreas || '--' }}</el-descriptions-item>
              <el-descriptions-item label="资质标签">
                {{ latestCertificateLabels.length ? latestCertificateLabels.join('、') : '未填写' }}
              </el-descriptions-item>
              <el-descriptions-item label="资质附件">
                <FileAttachmentList :files="latestAttachments" empty-text="未上传资质附件" />
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
              <div class="timeline-title">{{ statusLabel(item.status) }}</div>
              <p>服务类型：{{ item.serviceTypes || '--' }}</p>
              <p>服务区域：{{ item.serviceAreas || '--' }}</p>
              <p>资质概览：{{ formatWorkerCertificateSummary(item) }}</p>
              <p v-if="item.adminRemark">审核备注：{{ item.adminRemark }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无审核记录" />
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import FileAttachmentList from '../../components/common/FileAttachmentList.vue'
import {
  fetchCurrentWorkerProfile,
  fetchMyWorkerApplications,
  submitWorkerApplication,
  uploadAttachment,
  uploadImage
} from '../../api'
import { authStore } from '../../stores/auth'
import { getWorkerImage } from '../../utils/displayAssets'
import {
  formatWorkerCertificateSummary,
  getWorkerCertificateLabels,
  getWorkerQualificationAttachments
} from '../../utils/workerApplication'

const maxProofCount = 5
const maxProofSize = 10 * 1024 * 1024
const allowedExtensions = ['jpg', 'jpeg', 'png', 'webp', 'pdf', 'doc', 'docx', 'txt']

const serviceTypeOptions = ['日常保洁', '深度清洁', '家电清洗', '收纳整理', '做饭阿姨', '老人陪护', '母婴护理']
const certificateOptions = ['健康证', '育婴师证', '母婴护理证', '养老护理员证', '家政培训结业证']
const areaOptions = ['浦东新区', '闵行区', '徐汇区', '静安区', '黄浦区', '杨浦区', '宝山区']
const scheduleOptions = ['工作日白天', '工作日晚间', '周末白天', '周末晚间', '节假日可接单']

const formRef = ref(null)
const avatarInputRef = ref(null)
const applications = ref([])
const proofFileList = ref([])
const uploadingAvatar = ref(false)

const form = reactive({
  realName: authStore.state.user?.realName || '',
  phone: authStore.state.user?.phone || '',
  serviceTypes: [],
  yearsOfExperience: 0,
  certificateLabels: [],
  serviceAreas: [],
  availableSchedule: [],
  intro: '',
  avatarUrl: ''
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入有效的 11 位手机号', trigger: 'blur' }
  ]
}

const latestApplication = computed(() => applications.value[0] || null)
const latestCertificateLabels = computed(() => getWorkerCertificateLabels(latestApplication.value?.certificates))
const latestAttachments = computed(() => getWorkerQualificationAttachments(latestApplication.value))
const pendingCount = computed(() => applications.value.filter((item) => item.status === 'PENDING').length)
const approvedCount = computed(() => applications.value.filter((item) => item.status === 'APPROVED').length)
const rejectedCount = computed(() => applications.value.filter((item) => item.status === 'REJECTED').length)
const normalizedProofFiles = computed(() =>
  proofFileList.value
    .map((item, index) => ({
      uid: item.uid || `proof-${index}`,
      name: item.name,
      url: item.remoteUrl || item.url || '',
      size: item.size || 0
    }))
    .filter((item) => item.url)
)
const avatarPreview = computed(() =>
  getWorkerImage({
    id: authStore.state.user?.id,
    name: form.realName || authStore.state.user?.realName || '服务人员',
    city: '服务人员',
    avatarUrl: form.avatarUrl
  })
)

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

function splitValues(value) {
  return String(value || '')
    .split(/[、,，;\n]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function buildRemoteProofFiles(attachments = []) {
  return attachments.map((item, index) => ({
    uid: item.uid || item.id || `remote-${index}`,
    name: item.name,
    size: item.size || 0,
    status: 'success',
    url: item.url,
    remoteUrl: item.url
  }))
}

function syncForm(application) {
  if (!application) {
    return
  }
  form.realName = application.realName || authStore.state.user?.realName || ''
  form.phone = application.phone || authStore.state.user?.phone || ''
  form.serviceTypes = splitValues(application.serviceTypes)
  form.yearsOfExperience = Number(application.yearsOfExperience || 0)
  form.certificateLabels = getWorkerCertificateLabels(application.certificates)
  form.serviceAreas = splitValues(application.serviceAreas)
  form.availableSchedule = splitValues(application.availableSchedule)
  form.intro = application.intro || ''
  proofFileList.value = buildRemoteProofFiles(getWorkerQualificationAttachments(application))
}

async function loadWorkerProfile() {
  try {
    const profile = await fetchCurrentWorkerProfile()
    if (profile?.avatarUrl && !form.avatarUrl) {
      form.avatarUrl = profile.avatarUrl
    }
    if (profile?.name && !form.realName) {
      form.realName = profile.name
    }
  } catch {
    // ignore
  }
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
  form.certificateLabels = []
  form.serviceAreas = []
  form.availableSchedule = []
  form.intro = ''
  proofFileList.value = []
}

function validateWorkerFields() {
  if (!form.serviceTypes.length) {
    ElMessage.error('请至少选择一个服务类型')
    return false
  }
  if (!form.certificateLabels.length) {
    ElMessage.error('请至少填写一个资质标签')
    return false
  }
  if (!form.serviceAreas.length) {
    ElMessage.error('请至少填写一个服务区域')
    return false
  }
  if (!form.availableSchedule.length) {
    ElMessage.error('请至少选择一个可接单时段')
    return false
  }
  if (!form.intro.trim()) {
    ElMessage.error('请填写个人介绍')
    return false
  }
  if (!proofFileList.value.length) {
    ElMessage.error('请至少上传一份资质附件')
    return false
  }
  return true
}

function normalizeUploadFile(item, index) {
  return {
    ...item,
    uid: item.uid || `proof-${index}`,
    remoteUrl: item.remoteUrl || item.response?.url || item.url || '',
    size: item.size || item.raw?.size || 0
  }
}

function fileExtension(name) {
  const parts = String(name || '').split('.')
  return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : ''
}

function beforeProofSelect(file) {
  const extension = fileExtension(file.name)
  if (!allowedExtensions.includes(extension)) {
    ElMessage.warning('当前仅支持上传图片、PDF、Word 或文本文件')
    return false
  }
  if (file.size > maxProofSize) {
    ElMessage.warning(`文件 ${file.name} 超过 10MB 限制`)
    return false
  }
  return false
}

function handleProofChange(_uploadFile, uploadFiles) {
  proofFileList.value = uploadFiles.slice(0, maxProofCount).map(normalizeUploadFile)
}

function handleProofRemove(_uploadFile, uploadFiles) {
  proofFileList.value = uploadFiles.map(normalizeUploadFile)
}

function handleProofExceed() {
  ElMessage.warning(`最多上传 ${maxProofCount} 份资质文件`)
}

function openAvatarPicker() {
  avatarInputRef.value?.click()
}

function clearAvatar() {
  form.avatarUrl = ''
  if (avatarInputRef.value) {
    avatarInputRef.value.value = ''
  }
}

async function handleAvatarChange(event) {
  const file = event.target.files?.[0]
  if (!file) {
    return
  }
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片格式的头像文件')
    event.target.value = ''
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('头像图片不能超过 5MB')
    event.target.value = ''
    return
  }

  uploadingAvatar.value = true
  try {
    const uploaded = await uploadImage(file)
    form.avatarUrl = uploaded.url
    ElMessage.success('展示头像上传成功')
  } catch (error) {
    ElMessage.error(error.message || '上传展示头像失败')
  } finally {
    uploadingAvatar.value = false
    event.target.value = ''
  }
}

async function uploadProofAttachments() {
  const attachments = []
  for (const file of proofFileList.value) {
    if (file.remoteUrl && !file.raw) {
      attachments.push({
        name: file.name,
        url: file.remoteUrl,
        size: file.size || 0
      })
      continue
    }

    if (!file.raw) {
      continue
    }

    const uploaded = await uploadAttachment(file.raw)
    file.remoteUrl = uploaded.url
    file.url = uploaded.url
    attachments.push({
      name: uploaded.name || file.name,
      url: uploaded.url,
      size: uploaded.size || file.size || 0
    })
  }
  return attachments
}

async function loadApplications() {
  await loadWorkerProfile()
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

    const uploadedAttachments = await uploadProofAttachments()
    await submitWorkerApplication({
      realName: form.realName,
      phone: form.phone,
      serviceTypes: form.serviceTypes.join('、'),
      yearsOfExperience: form.yearsOfExperience,
      certificates: form.certificateLabels.join('、'),
      serviceAreas: form.serviceAreas.join('、'),
      availableSchedule: form.availableSchedule.join('、'),
      intro: form.intro,
      avatarUrl: form.avatarUrl,
      attachments: uploadedAttachments
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
.worker-avatar-panel {
  padding: 16px 18px;
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid #e4e7ec;
}

.worker-avatar-panel__meta {
  display: grid;
  gap: 8px;
}
</style>
