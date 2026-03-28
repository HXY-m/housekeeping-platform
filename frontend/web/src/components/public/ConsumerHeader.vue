<template>
  <header class="public-header">
    <div class="public-header__inner">
      <RouterLink to="/" class="brand-link">
        <div class="brand-mark">家</div>
        <div class="brand-copy">
          <strong>安心家政</strong>
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
      </el-menu>

      <div class="public-header__actions">
        <template v-if="authStore.isLoggedIn()">
          <el-tag>{{ authStore.state.user?.realName }} / {{ roleLabel }}</el-tag>
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
import { resolveConsoleEntry } from '../../utils/auth'

const route = useRoute()
const router = useRouter()

const activeIndex = computed(() => {
  if (route.path.startsWith('/workers')) return '/workers'
  if (route.path.startsWith('/booking')) return '/workers'
  return '/'
})

const consoleEntry = computed(() => resolveConsoleEntry(authStore.state.user?.roleCode))
const roleLabel = computed(() => {
  const roleCode = authStore.state.user?.roleCode
  if (roleCode === 'ADMIN') return '管理员'
  if (roleCode === 'WORKER') return '服务人员'
  return '普通用户'
})

function handleSelect(index) {
  router.push(index)
}

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>
