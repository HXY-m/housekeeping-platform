<template>
  <el-card class="auth-form-card register-card" shadow="never">
    <div class="page-stack">
      <div class="auth-form-head">
        <div class="login-title">注册</div>
        <div class="login-subtitle">先完成账号创建，再进入对应工作台。</div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleRegister">
        <div class="register-step-card">
          <div class="register-step-card__head">
            <div>
              <strong>账号信息</strong>
              <p class="muted-line">填写基础身份信息和登录密码。</p>
            </div>
            <el-radio-group v-model="form.roleCode" class="identity-group">
              <el-radio-button label="USER">普通用户</el-radio-button>
              <el-radio-button label="WORKER">服务人员</el-radio-button>
            </el-radio-group>
          </div>

          <el-row :gutter="16">
            <el-col :xs="24" :md="12">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model.trim="form.realName" size="large" placeholder="请输入真实姓名" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model.trim="form.phone" size="large" placeholder="请输入 11 位手机号" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :xs="24" :md="12">
              <el-form-item label="登录密码" prop="password">
                <el-input
                  v-model="form.password"
                  size="large"
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
                  size="large"
                  type="password"
                  show-password
                  placeholder="请再次输入密码"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <el-alert
          v-if="form.roleCode === 'WORKER'"
          title="服务信息与资质文件将在注册成功后填写"
          description="服务类型、服务区域、接单时段和资质附件都请在“资质材料”页提交。资质审核通过前，服务人员无法接单。"
          type="info"
          show-icon
          :closable="false"
        />

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

const form = reactive({
  roleCode: 'USER',
  realName: '',
  phone: '',
  password: '',
  confirmPassword: ''
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

async function handleRegister() {
  try {
    errorMessage.value = ''
    await formRef.value?.validate()

    const result = await register({
      roleCode: form.roleCode,
      realName: form.realName,
      phone: form.phone,
      password: form.password
    })

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
  max-width: 640px;
}

.auth-form-head {
  display: grid;
  gap: 8px;
}

.register-step-card {
  padding: 20px;
  border: 1px solid rgba(29, 29, 31, 0.08);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.84);
}

.register-step-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.identity-group {
  display: flex;
  flex-wrap: wrap;
}

.identity-group :deep(.el-radio-button__inner) {
  min-width: 126px;
}

@media (max-width: 768px) {
  .register-card {
    max-width: 100%;
  }

  .register-step-card__head {
    flex-direction: column;
  }
}
</style>
