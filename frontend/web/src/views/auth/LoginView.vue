<template>
  <el-card class="login-card" shadow="hover">
    <div class="page-stack">
      <div>
        <el-tag type="primary" round>登录系统</el-tag>
        <div class="login-title">进入家政服务预约平台</div>
        <div class="login-subtitle">支持普通用户、服务人员、管理员三类角色登录。</div>
      </div>

      <el-form :model="form" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCode" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="服务人员" value="WORKER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-button class="full-width" type="primary" size="large" @click="handleLogin">登录</el-button>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <div class="page-stack">
        <el-divider>演示账号</el-divider>
        <div class="demo-account-list">
          <el-card
            v-for="account in demoAccounts"
            :key="`${account.roleCode}-${account.phone}`"
            class="demo-account-card"
            shadow="never"
            @click="fillDemoAccount(account)"
          >
            <strong>{{ account.description }}</strong>
            <div class="muted-line">{{ account.roleCode }} / {{ account.phone }}</div>
          </el-card>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchDemoAccounts, login } from '../../api'
import { authStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const errorMessage = ref('')
const demoAccounts = ref([])

const form = reactive({
  phone: '',
  password: '',
  roleCode: 'USER'
})

function fillDemoAccount(account) {
  form.phone = account.phone
  form.password = account.password
  form.roleCode = account.roleCode
}

function resolveDefaultRedirect(roleCode) {
  if (roleCode === 'ADMIN') return '/admin/dashboard'
  if (roleCode === 'WORKER') return '/worker/orders'
  return '/'
}

async function handleLogin() {
  try {
    errorMessage.value = ''
    const result = await login(form)
    authStore.loginSuccess(result)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || resolveDefaultRedirect(result.user.roleCode)
    router.push(redirect)
  } catch (error) {
    errorMessage.value = error.message || '登录失败'
  }
}

onMounted(async () => {
  demoAccounts.value = await fetchDemoAccounts().catch(() => [])
  if (route.query.error === 'no_permission') {
    errorMessage.value = '当前账号没有目标页面访问权限，请切换角色后重新登录。'
  }
})
</script>
