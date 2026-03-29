<template>
  <div class="layout-shell layout-shell--ambient">
    <AmbientBackdrop variant="console" />
    <div class="layout-shell__content">
      <el-container class="admin-shell">
        <el-aside width="240px" class="admin-aside">
          <div class="admin-brand">
            <strong>管理后台</strong>
          </div>

          <el-menu :default-active="route.path" class="admin-menu" router>
            <el-menu-item v-if="showMenu('ADMIN_DASHBOARD_VIEW')" index="/admin/dashboard">运营看板</el-menu-item>
            <el-menu-item v-if="showMenu('ADMIN_MESSAGE_VIEW')" index="/admin/messages">
              <span>消息通知</span>
              <el-badge v-if="unreadCount" :value="unreadCount" :max="99" class="layout-menu-badge" />
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
            <div class="console-title-block">
              <div class="console-title">管理后台</div>
              <div class="console-subtitle">统一查看运营、审核、售后和权限配置</div>
            </div>
            <div class="console-header-actions">
              <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99">
                <el-button plain @click="router.push('/admin/messages')">消息</el-button>
              </el-badge>
              <div class="console-user-chip">
                <el-avatar :size="38" :src="authStore.state.user?.avatarUrl || undefined" class="console-user-avatar">
                  {{ userInitial }}
                </el-avatar>
                <div class="console-user-meta">
                  <strong>{{ authStore.state.user?.realName || '管理员' }}</strong>
                  <span>{{ formatRoleLabel(authStore.state.user?.roleCode) }}</span>
                </div>
              </div>
              <el-button type="danger" plain @click="logout">退出登录</el-button>
            </div>
          </el-header>

          <el-main class="admin-main">
            <router-view />
          </el-main>
        </el-container>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AmbientBackdrop from '../components/common/AmbientBackdrop.vue'
import { authStore } from '../stores/auth'
import { notificationStore } from '../stores/notification'
import { formatRoleLabel } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const unreadCount = computed(() => notificationStore.getUnreadCount('admin'))
const userInitial = computed(() => (authStore.state.user?.realName || '管').slice(0, 1))

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
