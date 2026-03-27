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
        <el-menu-item v-if="authStore.hasRole('USER')" index="/orders">我的订单</el-menu-item>
        <el-menu-item v-if="authStore.hasRole('USER')" index="/worker/apply">服务者入驻</el-menu-item>
        <el-menu-item v-if="authStore.hasRole('WORKER')" index="/worker/orders">服务工作台</el-menu-item>
      </el-menu>

      <div class="public-header__actions">
        <template v-if="authStore.isLoggedIn()">
          <el-tag>{{ authStore.state.user?.realName }} / {{ authStore.state.user?.roleCode }}</el-tag>
          <el-button v-if="authStore.hasRole('WORKER')" @click="router.push('/worker/orders')">进入工作台</el-button>
          <el-button v-if="authStore.hasRole('ADMIN')" @click="router.push('/admin/dashboard')">进入后台</el-button>
          <el-button type="primary" plain @click="logout">退出登录</el-button>
        </template>
        <el-button v-else type="primary" @click="router.push('/login')">登录</el-button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authStore } from '../../stores/auth'

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

function handleSelect(index) {
  router.push(index)
}

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>
