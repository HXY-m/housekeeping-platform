<template>
  <header class="app-header">
    <div class="brand">
      <div class="brand-mark">H</div>
      <div>
        <strong>Housekeeping Hub</strong>
        <p>以 Taskrabbit 思路落地的家政服务预约平台</p>
      </div>
    </div>
    <nav class="nav-links">
      <RouterLink to="/">首页</RouterLink>
      <RouterLink to="/workers">找服务人员</RouterLink>
      <RouterLink to="/orders">我的订单</RouterLink>
      <RouterLink to="/admin">运营看板</RouterLink>
      <RouterLink v-if="!authStore.isLoggedIn()" to="/login">登录</RouterLink>
      <button v-else class="nav-user" type="button" @click="handleLogout">
        {{ authStore.state.user?.realName }} / 退出
      </button>
    </nav>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { authStore } from '../stores/auth'

const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>
