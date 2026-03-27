<template>
  <el-container class="admin-shell">
    <el-aside width="248px" class="admin-aside">
      <div class="admin-brand">
        <strong>Housekeeping Admin</strong>
        <span>运营管理中台</span>
      </div>

      <el-menu :default-active="route.path" class="admin-menu" router>
        <el-menu-item index="/admin/dashboard">运营看板</el-menu-item>
        <el-menu-item index="/admin/orders">订单监管</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/services">服务项目</el-menu-item>
        <el-menu-item index="/admin/applications">入驻审核</el-menu-item>
        <el-menu-item index="/admin/after-sales">售后处理</el-menu-item>
        <el-menu-item index="/admin/operation-logs">操作日志</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div>
          <div class="console-title">家政服务预约平台后台</div>
          <div class="console-subtitle">统一处理运营、审核、服务配置与平台治理动作</div>
        </div>

        <div class="console-header-actions">
          <span class="console-user-label">{{ authStore.state.user?.realName || '管理员' }}</span>
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
