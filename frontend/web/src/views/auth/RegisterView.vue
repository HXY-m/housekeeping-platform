<template>
  <el-card class="login-card" shadow="hover">
    <div class="page-stack">
      <div>
        <el-tag type="warning" round>创建账号</el-tag>
        <div class="login-title">注册普通用户账号</div>
        <div class="login-subtitle">注册后将自动进入用户端，可继续预约服务或申请成为服务人员。</div>
      </div>

      <el-form :model="form" label-position="top" @submit.prevent="handleRegister">
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入 11 位手机号" />
        </el-form-item>
        <el-form-item label="登录密码">
          <el-input v-model="form.password" type="password" placeholder="请设置密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-button class="full-width" type="primary" size="large" @click="handleRegister">注册并进入平台</el-button>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />
    </div>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api'
import { authStore } from '../../stores/auth'

const router = useRouter()
const errorMessage = ref('')

const form = reactive({
  realName: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

async function handleRegister() {
  if (form.password !== form.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }

  try {
    errorMessage.value = ''
    const result = await register({
      realName: form.realName,
      phone: form.phone,
      password: form.password
    })
    authStore.setUserSession(result)
    ElMessage.success('注册成功')
    router.push(authStore.resolveHomePath(result.user.roleCode))
  } catch (error) {
    errorMessage.value = error.message || '注册失败'
  }
}
</script>
