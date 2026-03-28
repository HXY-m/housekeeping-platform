<template>
  <el-container class="admin-shell">
    <el-aside width="248px" class="admin-aside">
      <div class="admin-brand">
        <strong>Housekeeping Admin</strong>
        <span>Operations Console</span>
      </div>

      <el-menu :default-active="route.path" class="admin-menu" router>
        <el-menu-item v-if="showMenu('ADMIN_DASHBOARD_VIEW')" index="/admin/dashboard">Dashboard</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_MESSAGE_VIEW')" index="/admin/messages">
          <span>Messages</span>
          <el-badge
            v-if="unreadCount"
            :value="unreadCount"
            :max="99"
            class="layout-menu-badge"
          />
        </el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_ORDER_MANAGE')" index="/admin/orders">Orders</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_USER_MANAGE')" index="/admin/users">Users</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_SERVICE_MANAGE')" index="/admin/services">Services</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_APPLICATION_REVIEW')" index="/admin/applications">Qualifications</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_AFTER_SALE_MANAGE')" index="/admin/after-sales">After-sales</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_OPERATION_LOG_VIEW')" index="/admin/operation-logs">Operation Logs</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_REPORT_EXPORT')" index="/admin/reports">Reports</el-menu-item>
        <el-menu-item v-if="showMenu('ADMIN_PERMISSION_MANAGE')" index="/admin/permissions">Permissions</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div>
          <div class="console-title">Housekeeping Platform Admin</div>
          <div class="console-subtitle">Operate orders, services, audits, notifications and governance in one place.</div>
        </div>

        <div class="console-header-actions">
          <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99">
            <el-button plain @click="router.push('/admin/messages')">Messages</el-button>
          </el-badge>
          <span class="console-user-label">{{ authStore.state.user?.realName || 'Admin' }}</span>
          <el-button type="danger" plain @click="logout">Sign out</el-button>
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
