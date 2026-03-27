<template>
  <el-container class="user-shell">
    <el-aside width="248px" class="user-aside">
      <div class="user-brand">
        <strong>Client Center</strong>
        <span>普通用户中心</span>
      </div>
      <el-menu :default-active="route.path" class="user-menu" router>
        <el-menu-item index="/user/dashboard">个人看板</el-menu-item>
        <el-menu-item index="/user/profile">个人资料与地址簿</el-menu-item>
        <el-menu-item index="/user/orders">我的订单</el-menu-item>
        <el-menu-item index="/user/worker-application">服务者入驻</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="user-header">
        <div>
          <div class="console-title">用户中心</div>
          <div class="console-subtitle">管理资料、地址簿、订单、售后反馈与服务者入驻申请</div>
        </div>
        <div class="console-header-actions">
          <el-button @click="router.push('/workers')">继续找服务</el-button>
          <span class="console-user-label">{{ authStore.state.user?.realName }}</span>
          <el-button type="danger" plain @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="user-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { authStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>
