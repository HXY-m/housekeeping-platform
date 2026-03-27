<template>
  <section class="login-shell">
    <article class="card login-card">
      <div class="section-head login-head">
        <div>
          <p class="eyebrow">登录系统</p>
          <h1>进入家政服务预约平台</h1>
        </div>
      </div>

      <form class="booking-form" @submit.prevent="handleLogin">
        <label>
          <span>手机号</span>
          <input v-model="form.phone" placeholder="请输入手机号" required />
        </label>
        <label>
          <span>密码</span>
          <input v-model="form.password" type="password" placeholder="请输入密码" required />
        </label>
        <label>
          <span>角色</span>
          <select v-model="form.roleCode" required>
            <option value="USER">普通用户</option>
            <option value="WORKER">服务人员</option>
            <option value="ADMIN">管理员</option>
          </select>
        </label>
        <button class="button" type="submit">登录</button>
      </form>

      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

      <article class="demo-accounts">
        <h3>演示账号</h3>
        <div class="grid">
          <button
            v-for="account in demoAccounts"
            :key="`${account.roleCode}-${account.phone}`"
            type="button"
            class="demo-account"
            @click="fillDemoAccount(account)"
          >
            <strong>{{ account.description }}</strong>
            <span>{{ account.roleCode }} / {{ account.phone }}</span>
          </button>
        </div>
      </article>
    </article>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchDemoAccounts, login } from '../api'
import { authStore } from '../stores/auth'

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

async function handleLogin() {
  try {
    errorMessage.value = ''
    const result = await login(form)
    authStore.loginSuccess(result)
    const redirect = route.query.redirect || (result.user.roleCode === 'ADMIN' ? '/admin' : '/')
    router.push(redirect)
  } catch (error) {
    errorMessage.value = error.message || '登录失败'
  }
}

onMounted(async () => {
  try {
    demoAccounts.value = await fetchDemoAccounts()
  } catch {
    demoAccounts.value = []
  }

  if (route.query.error === 'no_permission') {
    errorMessage.value = '当前账号没有目标页面访问权限，请切换正确角色后登录'
  }
})
</script>
