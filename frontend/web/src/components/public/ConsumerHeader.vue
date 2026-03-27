<template>
  <header class="public-header">
    <div class="public-header__inner">
      <RouterLink to="/" class="brand-link">
        <div class="brand-mark">H</div>
        <div class="brand-copy">
          <strong>Housekeeping Hub</strong>
          <span>家政服务预约平台</span>
        </div>
      </RouterLink>

      <el-menu
        mode="horizontal"
        :default-active="activeIndex"
        class="public-menu"
        @select="handleSelect"
      >
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/workers">找服务人员</el-menu-item>
        <el-menu-item v-if="authStore.hasRole(ROLE_USER)" index="/orders">我的订单</el-menu-item>
        <el-menu-item v-if="authStore.hasRole(ROLE_USER)" index="/worker/apply">服务者入驻</el-menu-item>
        <el-menu-item v-if="authStore.hasRole(ROLE_WORKER)" index="/worker/orders">服务工作台</el-menu-item>
      </el-menu>

      <div class="public-header__actions">
        <template v-if="authStore.isLoggedIn()">
          <el-tag>{{ authStore.state.user?.realName }} / {{ authStore.state.user?.roleCode }}</el-tag>
          <el-button v-if="consoleEntry" @click="router.push(consoleEntry.path)">{{ consoleEntry.label }}</el-button>
          <el-button type="primary" plain @click="logout">退出登录</el-button>
        </template>
        <template v-else>
          <el-button @click="router.push('/register')">注册</el-button>
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authStore } from '../../stores/auth'
import { ROLE_USER, ROLE_WORKER, resolveConsoleEntry } from '../../utils/auth'

const route = useRoute()
const router = useRouter()

const activeIndex = computed(() => {
  if (route.path.startsWith('/workers')) return '/workers'
  if (route.path.startsWith('/orders')) return '/orders'
  if (route.path.startsWith('/booking')) return '/workers'
  if (route.path.startsWith('/worker/apply')) return '/worker/apply'
  if (route.path.startsWith('/worker/orders')) return '/worker/orders'
  return '/'
})

const consoleEntry = computed(() => resolveConsoleEntry(authStore.state.user?.roleCode))

function handleSelect(index) {
  router.push(index)
}

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>
