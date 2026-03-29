<template>
  <el-card class="auth-form-card login-card" shadow="never">
    <div class="login-panel">
      <section class="login-panel__main page-stack">
        <div class="auth-form-head">
          <div class="login-title">登录</div>
        </div>

        <el-form :model="form" label-position="top" @submit.prevent="handleLogin">
          <el-form-item label="手机号">
            <el-input v-model="form.phone" size="large" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" size="large" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item label="身份">
            <el-select v-model="form.roleCode" size="large" style="width: 100%">
              <el-option label="普通用户" value="USER" />
              <el-option label="服务人员" value="WORKER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </el-form-item>
          <el-button class="full-width" type="primary" size="large" @click="handleLogin">登录</el-button>
        </el-form>

        <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />
      </section>

      <aside class="login-panel__aside">
        <div class="login-panel__aside-head">
          <strong>体验账号</strong>
          <span>点击即可自动填充</span>
        </div>

        <div class="demo-account-list">
          <button
            v-for="account in demoAccounts"
            :key="`${account.roleCode}-${account.phone}`"
            type="button"
            class="demo-account-card demo-account-card--button"
            @click="fillDemoAccount(account)"
          >
            <strong>{{ getDemoDisplayName(account) }}</strong>
            <div class="muted-line">{{ formatRoleLabel(account.roleCode) }} / {{ account.phone }}</div>
          </button>
        </div>
      </aside>
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchDemoAccounts, login } from '../../api'
import { authStore } from '../../stores/auth'
import { formatRoleLabel } from '../../utils/auth'

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

function getDemoDisplayName(account) {
  if (account.roleCode === 'ADMIN') {
    return '管理员体验账号'
  }
  if (account.roleCode === 'WORKER') {
    return '服务人员体验账号'
  }
  return '普通用户体验账号'
}

async function handleLogin() {
  try {
    errorMessage.value = ''
    const result = await login(form)
    authStore.setUserSession(result)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || authStore.resolveHomePath(result.user.roleCode)
    router.push(redirect)
  } catch (error) {
    errorMessage.value = error.message || '登录失败'
  }
}

onMounted(async () => {
  demoAccounts.value = await fetchDemoAccounts().catch(() => [])
  if (route.query.phone) {
    form.phone = String(route.query.phone)
  }
  if (route.query.error === 'no_permission') {
    errorMessage.value = '当前账号没有目标页面的访问权限，请切换身份后重新登录。'
  }
  if (route.query.registered === '1') {
    ElMessage.success('注册成功，请登录')
  }
})
</script>

<style scoped>
.login-card {
  max-width: none;
}

.login-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(280px, 0.9fr);
  gap: 24px;
  align-items: start;
}

.login-panel__main {
  min-width: 0;
}

.login-panel__aside {
  display: grid;
  gap: 16px;
  align-content: start;
  padding: 22px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.54);
  backdrop-filter: blur(22px);
}

.auth-form-head {
  display: grid;
  gap: 0;
}

.login-panel__aside-head {
  display: grid;
  gap: 6px;
}

.login-panel__aside-head span {
  color: var(--muted);
  line-height: 1.7;
}

.demo-account-card--button {
  width: 100%;
  padding: 16px 18px;
  border: 1px solid rgba(29, 29, 31, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.demo-account-card--button:hover {
  border-color: rgba(0, 113, 227, 0.28);
  box-shadow: 0 18px 36px rgba(17, 24, 39, 0.08);
  transform: translateY(-1px);
}

@media (max-width: 960px) {
  .login-panel {
    grid-template-columns: 1fr;
  }

  .login-panel__aside {
    padding: 18px;
  }
}
</style>
