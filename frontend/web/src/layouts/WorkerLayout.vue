<template>
  <el-container class="worker-shell">
    <el-aside width="240px" class="worker-aside">
      <div class="worker-brand">
        <strong>Worker Desk</strong>
        <span>Worker Workspace</span>
      </div>
      <el-menu :default-active="route.path" class="worker-menu" router>
        <el-menu-item v-if="showMenu('WORKER_DASHBOARD_VIEW')" index="/worker/dashboard">Dashboard</el-menu-item>
        <el-menu-item v-if="showMenu('WORKER_ORDER_HANDLE')" index="/worker/orders">Orders</el-menu-item>
        <el-menu-item v-if="showMenu('WORKER_MESSAGE_USE')" index="/worker/messages">
          <span>Messages</span>
          <el-badge
            v-if="unreadCount"
            :value="unreadCount"
            :max="99"
            class="layout-menu-badge"
          />
        </el-menu-item>
        <el-menu-item v-if="showMenu('WORKER_QUALIFICATION_SUBMIT')" index="/worker/qualification">Qualification</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="worker-header">
        <div>
          <div class="console-title">Worker Console</div>
          <div class="console-subtitle">Handle orders, message clients, manage service records and maintain qualification files.</div>
        </div>
        <div class="console-header-actions">
          <el-badge v-if="showMenu('WORKER_MESSAGE_USE')" :value="unreadCount" :hidden="!unreadCount" :max="99">
            <el-button plain @click="router.push('/worker/messages')">Messages</el-button>
          </el-badge>
          <el-button v-if="showMenu('WORKER_QUALIFICATION_SUBMIT')" plain @click="router.push('/worker/qualification')">Qualification</el-button>
          <span class="console-user-label">{{ authStore.state.user?.realName }}</span>
          <el-button type="danger" plain @click="logout">Sign out</el-button>
        </div>
      </el-header>
      <el-main class="worker-main">
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
const unreadCount = computed(() => notificationStore.getUnreadCount('worker'))

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
