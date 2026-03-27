<template>
  <el-container class="worker-shell">
    <el-aside width="240px" class="worker-aside">
      <div class="worker-brand">
        <strong>Worker Desk</strong>
        <span>服务人员工作台</span>
      </div>
      <el-menu :default-active="route.path" class="worker-menu" router>
        <el-menu-item index="/worker/dashboard">履约看板</el-menu-item>
        <el-menu-item index="/worker/orders">我的服务订单</el-menu-item>
        <el-menu-item index="/worker/messages">
          <span>消息通知</span>
          <el-badge
            v-if="unreadCount"
            :value="unreadCount"
            :max="99"
            class="layout-menu-badge"
          />
        </el-menu-item>
        <el-menu-item index="/worker/qualification">资质资料</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="worker-header">
        <div>
          <div class="console-title">服务人员控制台</div>
          <div class="console-subtitle">接单、沟通、排期、过程记录与资质资料维护</div>
        </div>
        <div class="console-header-actions">
          <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99">
            <el-button plain @click="router.push('/worker/messages')">消息通知</el-button>
          </el-badge>
          <el-button plain @click="router.push('/worker/qualification')">资质资料</el-button>
          <span class="console-user-label">{{ authStore.state.user?.realName }}</span>
          <el-button type="danger" plain @click="logout">退出登录</el-button>
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

function logout() {
  authStore.logout()
  notificationStore.reset()
  router.push('/login')
}
</script>
