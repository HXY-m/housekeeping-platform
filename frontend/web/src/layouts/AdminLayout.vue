<template>
  <el-container class="admin-shell">
    <el-aside width="240px" class="admin-aside">
      <div class="admin-brand">
        <strong>Housekeeping Admin</strong>
        <span>管理后台</span>
      </div>
      <el-menu :default-active="route.path" class="admin-menu" router>
        <el-menu-item index="/admin/dashboard">运营看板</el-menu-item>
        <el-menu-item index="/admin/applications">入驻审核</el-menu-item>
        <el-menu-item index="/admin/after-sales">售后处理</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div>
          <div class="admin-title">后台工作台</div>
          <div class="admin-subtitle">审核入驻、处理售后并监控平台运行状态</div>
        </div>
        <div class="admin-header-actions">
          <el-button @click="router.push('/')">返回首页</el-button>
          <span class="admin-user">{{ authStore.state.user?.realName }}</span>
          <el-button type="danger" plain @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="admin-main">
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
