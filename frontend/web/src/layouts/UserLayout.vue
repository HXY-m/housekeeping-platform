<template>
  <div class="layout-shell layout-shell--ambient">
    <AmbientBackdrop variant="console" />
    <div class="layout-shell__content">
      <el-container class="user-shell">
        <el-aside width="232px" class="user-aside">
          <div class="user-brand">
            <strong>用户中心</strong>
          </div>
          <el-menu :default-active="route.path" class="user-menu" router>
            <el-menu-item v-if="showMenu('USER_DASHBOARD_VIEW')" index="/user/dashboard">控制台</el-menu-item>
            <el-menu-item v-if="showMenu('USER_PROFILE_MANAGE')" index="/user/profile">个人资料</el-menu-item>
            <el-menu-item v-if="showMenu('USER_FAVORITE_MANAGE')" index="/user/favorites">我的收藏</el-menu-item>
            <el-menu-item v-if="showMenu('USER_MESSAGE_USE')" index="/user/messages">
              <span>消息中心</span>
              <el-badge v-if="unreadCount" :value="unreadCount" :max="99" class="layout-menu-badge" />
            </el-menu-item>
            <el-menu-item v-if="showMenu('USER_MESSAGE_USE')" index="/user/conversations">订单沟通</el-menu-item>
            <el-menu-item v-if="showMenu('USER_ORDER_USE')" index="/user/orders">我的订单</el-menu-item>
          </el-menu>
        </el-aside>

        <el-container>
          <el-header class="user-header">
            <div class="console-title-block">
              <div class="console-title">用户中心</div>
              <div class="console-subtitle">查看预约、售后、通知与订单沟通</div>
            </div>
            <div class="console-header-actions">
              <el-button @click="router.push('/workers')">查找服务人员</el-button>
              <el-badge v-if="showMenu('USER_MESSAGE_USE')" :value="unreadCount" :hidden="!unreadCount" :max="99">
                <el-button plain @click="router.push('/user/messages')">消息</el-button>
              </el-badge>
              <div class="console-user-chip">
                <el-avatar :size="38" :src="authStore.state.user?.avatarUrl || undefined" class="console-user-avatar">
                  {{ userInitial }}
                </el-avatar>
                <div class="console-user-meta">
                  <strong>{{ authStore.state.user?.realName || '用户' }}</strong>
                  <span>{{ formatRoleLabel(authStore.state.user?.roleCode) }}</span>
                </div>
              </div>
              <el-button type="danger" plain @click="logout">退出登录</el-button>
            </div>
          </el-header>
          <el-main class="user-main">
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
const unreadCount = computed(() => notificationStore.getUnreadCount('user'))
const userInitial = computed(() => (authStore.state.user?.realName || '用户').slice(0, 1))

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
