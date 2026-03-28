<template>
  <el-card class="register-card" shadow="hover">
    <div class="page-stack">
      <div>
        <div class="login-title">注册</div>
        <div class="login-subtitle">创建普通用户或服务人员账号</div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleRegister">
        <el-form-item label="注册身份">
          <el-radio-group v-model="form.roleCode" class="identity-group">
            <el-radio-button label="USER">普通用户</el-radio-button>
            <el-radio-button label="WORKER">服务人员</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model.trim="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="form.phone" placeholder="请输入 11 位手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="登录密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                show-password
                placeholder="请设置至少 6 位密码"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入密码"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <div v-if="form.roleCode === 'WORKER'" class="worker-section">
          <div class="worker-section__header">
            <div>
              <h3>服务人员资料</h3>
              <p>注册后可继续在服务人员端补充资质文件与展示信息。</p>
            </div>
            <el-tag type="warning">服务人员</el-tag>
          </div>

          <el-row :gutter="16">
            <el-col :xs="24" :lg="12">
              <el-form-item label="服务类型">
                <el-select
                  v-model="form.serviceTypes"
                  multiple
                  collapse-tags
                  collapse-tags-tooltip
                  placeholder="请选择服务类型"
                  style="width: 100%"
                >
                  <el-option v-for="item in serviceTypeOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :lg="12">
              <el-form-item label="从业年限">
                <el-input-number v-model="form.yearsOfExperience" :min="0" :max="40" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :xs="24" :lg="12">
              <el-form-item label="证书 / 培训经历">
                <el-select
                  v-model="form.certificates"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  collapse-tags
                  collapse-tags-tooltip
                  placeholder="可输入健康证、育婴师证等"
                  style="width: 100%"
                >
                  <el-option v-for="item in certificateOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :lg="12">
              <el-form-item label="服务区域">
                <el-select
                  v-model="form.serviceAreas"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  collapse-tags
                  collapse-tags-tooltip
                  placeholder="请输入可服务区域"
                  style="width: 100%"
                >
                  <el-option v-for="item in areaOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="可服务时段">
            <el-checkbox-group v-model="form.availableSchedule" class="slot-group">
              <el-checkbox-button v-for="slot in scheduleOptions" :key="slot" :label="slot">
                {{ slot }}
              </el-checkbox-button>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="个人介绍">
            <el-input
              v-model.trim="form.intro"
              type="textarea"
              :rows="4"
              maxlength="300"
              show-word-limit
              placeholder="简要说明擅长领域和服务经验"
            />
          </el-form-item>
        </div>

        <el-button class="full-width" type="primary" size="large" @click="handleRegister">
          {{ form.roleCode === 'WORKER' ? '注册服务人员账号' : '注册普通用户账号' }}
        </el-button>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />
    </div>
  </el-card>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api'
import { authStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const errorMessage = ref('')

const serviceTypeOptions = ['日常保洁', '深度清洁', '家电清洗', '收纳整理', '做饭阿姨', '老人陪护', '母婴护理']
const certificateOptions = ['健康证', '育婴师证', '母婴护理证', '养老护理员证', '家政培训结业证']
const areaOptions = ['浦东新区', '闵行区', '徐汇区', '静安区', '黄浦区', '杨浦区', '宝山区']
const scheduleOptions = ['工作日白天', '工作日晚间', '周末白天', '周末晚间', '节假日可接单']

const form = reactive({
  roleCode: 'USER',
  realName: '',
  phone: '',
  password: '',
  confirmPassword: '',
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
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_, value, callback) => {
        if (!value) {
          callback(new Error('请再次输入密码'))
          return
        }
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

watch(
  () => route.query.roleCode,
  (roleCode) => {
    form.roleCode = roleCode === 'WORKER' ? 'WORKER' : 'USER'
  },
  { immediate: true }
)

function buildPayload() {
  const payload = {
    roleCode: form.roleCode,
    realName: form.realName,
    phone: form.phone,
    password: form.password
  }

  if (form.roleCode === 'WORKER') {
    payload.serviceTypes = form.serviceTypes.join('、')
    payload.yearsOfExperience = form.yearsOfExperience
    payload.certificates = form.certificates.join('、')
    payload.serviceAreas = form.serviceAreas.join('、')
    payload.availableSchedule = form.availableSchedule.join('、')
    payload.intro = form.intro
  }

  return payload
}

function validateWorkerFields() {
  if (form.roleCode !== 'WORKER') {
    return true
  }
  if (!form.serviceTypes.length) {
    errorMessage.value = '请选择至少一个服务类型'
    return false
  }
  if (!form.serviceAreas.length) {
    errorMessage.value = '请填写至少一个服务区域'
    return false
  }
  if (!form.availableSchedule.length) {
    errorMessage.value = '请选择至少一个可服务时段'
    return false
  }
  if (!form.intro.trim()) {
    errorMessage.value = '请填写个人介绍'
    return false
  }
  return true
}

async function handleRegister() {
  try {
    errorMessage.value = ''
    await formRef.value?.validate()
    if (!validateWorkerFields()) {
      return
    }

    const result = await register(buildPayload())
    authStore.setUserSession(result)
    ElMessage.success(form.roleCode === 'WORKER' ? '服务人员账号注册成功' : '注册成功')
    const targetPath =
      result.user?.roleCode === 'WORKER'
        ? '/worker/qualification'
        : authStore.resolveHomePath(result.user?.roleCode)
    router.push(targetPath)
  } catch (error) {
    errorMessage.value = error.message || '注册失败'
  }
}
</script>

<style scoped>
.register-card {
  border: none;
}

.identity-group {
  display: flex;
  flex-wrap: wrap;
}

.identity-group :deep(.el-radio-button__inner) {
  min-width: 132px;
}

.worker-section {
  margin: 8px 0 20px;
  padding: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 16px;
  background: #fafafa;
}

.worker-section__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.worker-section__header h3 {
  margin: 0 0 6px;
  font-size: 18px;
}

.worker-section__header p {
  margin: 0;
  color: #667085;
  line-height: 1.6;
}

.slot-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 768px) {
  .worker-section__header {
    flex-direction: column;
  }
}
</style>
