<template>
  <header class="consumer-header">
    <div class="layout-container header-inner">
      <RouterLink to="/" class="consumer-brand">
        <div class="brand-mark">H</div>
        <div>
          <strong>Housekeeping Hub</strong>
          <p>家政服务预约平台</p>
        </div>
      </RouterLink>

      <el-menu
        mode="horizontal"
        :default-active="activeIndex"
        class="consumer-menu"
        @select="handleSelect"
      >
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/workers">找服务人员</el-menu-item>
        <el-menu-item index="/orders">我的订单</el-menu-item>
        <el-menu-item index="/worker/apply">服务者入驻</el-menu-item>
      </el-menu>

      <div class="consumer-actions">
        <template v-if="authStore.isLoggedIn()">
          <el-tag>{{ authStore.state.user?.realName }} / {{ authStore.state.user?.roleCode }}</el-tag>
          <el-button v-if="authStore.hasRole('ADMIN')" @click="router.push('/admin/dashboard')">进入后台</el-button>
          <el-button type="primary" plain @click="logout">退出</el-button>
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
  if (route.path.startsWith('/worker/apply')) return '/worker/apply'
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
