<template>
  <el-container class="user-shell">
    <el-aside width="248px" class="user-aside">
      <div class="user-brand">
        <strong>Client Center</strong>
        <span>User Workspace</span>
      </div>
      <el-menu :default-active="route.path" class="user-menu" router>
        <el-menu-item v-if="showMenu('USER_DASHBOARD_VIEW')" index="/user/dashboard">Dashboard</el-menu-item>
        <el-menu-item v-if="showMenu('USER_PROFILE_MANAGE')" index="/user/profile">Profile</el-menu-item>
        <el-menu-item v-if="showMenu('USER_FAVORITE_MANAGE')" index="/user/favorites">Favorites</el-menu-item>
        <el-menu-item v-if="showMenu('USER_MESSAGE_USE')" index="/user/messages">
          <span>Messages</span>
          <el-badge
            v-if="unreadCount"
            :value="unreadCount"
            :max="99"
            class="layout-menu-badge"
          />
        </el-menu-item>
        <el-menu-item v-if="showMenu('USER_ORDER_USE')" index="/user/orders">Orders</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="user-header">
        <div>
          <div class="console-title">User Center</div>
          <div class="console-subtitle">Manage profile, addresses, favorites, messages, orders and after-sales requests.</div>
        </div>
        <div class="console-header-actions">
          <el-button @click="router.push('/workers')">Browse Services</el-button>
          <el-button v-if="showMenu('USER_FAVORITE_MANAGE')" plain @click="router.push('/user/favorites')">Favorites</el-button>
          <el-badge v-if="showMenu('USER_MESSAGE_USE')" :value="unreadCount" :hidden="!unreadCount" :max="99">
            <el-button plain @click="router.push('/user/messages')">Messages</el-button>
          </el-badge>
          <span class="console-user-label">{{ authStore.state.user?.realName }}</span>
          <el-button type="danger" plain @click="logout">Sign out</el-button>
        </div>
      </el-header>
      <el-main class="user-main">
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
const unreadCount = computed(() => notificationStore.getUnreadCount('user'))

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
