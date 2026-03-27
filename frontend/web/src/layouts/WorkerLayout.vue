<template>
  <el-container class="worker-shell">
    <el-aside width="240px" class="worker-aside">
      <div class="worker-brand">
        <strong>Worker Desk</strong>
        <span>服务人员工作台</span>
      </div>
      <el-menu :default-active="route.path" class="worker-menu" router>
        <el-menu-item index="/worker/orders">我的服务订单</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="worker-header">
        <div>
          <div class="admin-title">履约工作台</div>
          <div class="admin-subtitle">接单、开工、完工与订单跟进</div>
        </div>
        <div class="admin-header-actions">
          <el-button @click="router.push('/')">返回首页</el-button>
          <span class="worker-user">{{ authStore.state.user?.realName }}</span>
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
import { useRoute, useRouter } from 'vue-router'
import { authStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>
