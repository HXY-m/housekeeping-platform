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

      <el-menu mode="horizontal" :ellipsis="false" router class="public-menu">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/workers">服务人员</el-menu-item>
        <el-menu-item index="/orders">订单进度</el-menu-item>
        <el-menu-item index="/worker-application">申请入驻</el-menu-item>
      </el-menu>

      <div class="public-header__actions">
        <template v-if="authStore.isLoggedIn()">
          <el-tag type="success">{{ authStore.state.user?.realName }} / {{ authStore.state.user?.roleCode }}</el-tag>
          <el-button v-if="authStore.hasRole('ADMIN')" type="primary" plain @click="router.push('/admin')">进入后台</el-button>
          <el-button @click="handleLogout">退出</el-button>
        </template>
        <template v-else>
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
        </template>
      </div>
    </div>
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
