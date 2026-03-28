<template>
  <el-container class="user-shell">
    <el-aside width="232px" class="user-aside">
      <div class="user-brand">
        <strong>用户中心</strong>
      </div>
      <el-menu :default-active="route.path" class="user-menu" router>
        <el-menu-item v-if="showMenu('USER_DASHBOARD_VIEW')" index="/user/dashboard">控制台</el-menu-item>
        <el-menu-item v-if="showMenu('USER_PROFILE_MANAGE')" index="/user/profile">个人资料</el-menu-item>
        <el-menu-item v-if="showMenu('USER_FAVORITE_MANAGE')" index="/user/favorites">收藏</el-menu-item>
        <el-menu-item v-if="showMenu('USER_MESSAGE_USE')" index="/user/messages">
          <span>消息</span>
          <el-badge v-if="unreadCount" :value="unreadCount" :max="99" class="layout-menu-badge" />
        </el-menu-item>
        <el-menu-item v-if="showMenu('USER_ORDER_USE')" index="/user/orders">订单</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="user-header">
        <div class="console-title">用户中心</div>
        <div class="console-header-actions">
          <el-button @click="router.push('/workers')">找服务人员</el-button>
          <el-badge v-if="showMenu('USER_MESSAGE_USE')" :value="unreadCount" :hidden="!unreadCount" :max="99">
            <el-button plain @click="router.push('/user/messages')">消息</el-button>
          </el-badge>
          <span class="console-user-label">{{ authStore.state.user?.realName }}</span>
          <el-button type="danger" plain @click="logout">退出</el-button>
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
