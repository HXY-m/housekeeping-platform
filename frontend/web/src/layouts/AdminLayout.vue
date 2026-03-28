<template>
  <el-container class="admin-shell">
    <el-aside width="248px" class="admin-aside">
      <div class="admin-brand">
        <strong>管理后台</strong>
        <span>运营治理与平台配置中心</span>
      </div>

      <el-menu :default-active="route.path" class="admin-menu" router>
        <el-menu-item v-if="showMenu('ADMIN_DASHBOARD_VIEW')" index="/admin/dashboard">运营看板</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_MESSAGE_VIEW')" index="/admin/messages">
          <span>消息通知</span>
          <el-badge
            v-if="unreadCount"
            :value="unreadCount"
            :max="99"
            class="layout-menu-badge"
          />
        </el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_ORDER_MANAGE')" index="/admin/orders">订单监管</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_USER_MANAGE')" index="/admin/users">用户管理</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_SERVICE_MANAGE')" index="/admin/services">服务项目</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_APPLICATION_REVIEW')" index="/admin/applications">资质审核</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_AFTER_SALE_MANAGE')" index="/admin/after-sales">售后处理</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_OPERATION_LOG_VIEW')" index="/admin/operation-logs">操作日志</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_REPORT_EXPORT')" index="/admin/reports">报表导出</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_PERMISSION_MANAGE')" index="/admin/permissions">权限配置</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div>
          <div class="console-title">家政服务预约平台后台</div>
          <div class="console-subtitle">统一处理运营数据、消息通知、订单监管、权限配置与审核流程</div>
        </div>

        <div class="console-header-actions">
          <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99">
            <el-button plain @click="router.push('/admin/messages')">消息通知</el-button>
          </el-badge>
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authStore } from '../stores/auth'
import { notificationStore } from '../stores/notification'

const route = useRoute()
const router = useRouter()
const unreadCount = computed(() => notificationStore.getUnreadCount('admin'))

function showMenu(permissionCode) {
  if (!authStore.hasPermissionData()) {
    return true
  }
  return authStore.hasPermission(permissionCode)
}

function logout() {
  authStore.logout()
  notificationStore.reset()
  router.push('/login')
}
</script>
