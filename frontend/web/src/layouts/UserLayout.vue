<template>
  <el-container class="user-shell">
    <el-aside width="248px" class="user-aside">
      <div class="user-brand">
        <strong>用户中心</strong>
        <span>个人资料、订单与收藏工作台</span>
      </div>
      <el-menu :default-active="route.path" class="user-menu" router>
        <el-menu-item v-if="showMenu('USER_DASHBOARD_VIEW')" index="/user/dashboard">个人看板</el-menu-item>
        <el-menu-item v-if="showMenu('USER_PROFILE_MANAGE')" index="/user/profile">个人资料</el-menu-item>
        <el-menu-item v-if="showMenu('USER_FAVORITE_MANAGE')" index="/user/favorites">我的收藏</el-menu-item>
        <el-menu-item v-if="showMenu('USER_MESSAGE_USE')" index="/user/messages">
          <span>消息通知</span>
          <el-badge
            v-if="unreadCount"
            :value="unreadCount"
            :max="99"
            class="layout-menu-badge"
          />
        </el-menu-item>
        <el-menu-item v-if="showMenu('USER_ORDER_USE')" index="/user/orders">我的订单</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="user-header">
        <div>
          <div class="console-title">用户中心</div>
          <div class="console-subtitle">统一管理个人资料、地址簿、订单、收藏、通知与售后反馈</div>
        </div>
        <div class="console-header-actions">
          <el-button @click="router.push('/workers')">继续找服务</el-button>
          <el-button v-if="showMenu('USER_FAVORITE_MANAGE')" plain @click="router.push('/user/favorites')">我的收藏</el-button>
          <el-badge v-if="showMenu('USER_MESSAGE_USE')" :value="unreadCount" :hidden="!unreadCount" :max="99">
            <el-button plain @click="router.push('/user/messages')">消息通知</el-button>
          </el-badge>
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
