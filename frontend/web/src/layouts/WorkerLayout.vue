<template>
  <div class="layout-shell layout-shell--ambient">
    <AmbientBackdrop variant="console" />
    <div class="layout-shell__content">
      <el-container class="worker-shell">
        <el-aside width="232px" class="worker-aside">
          <div class="worker-brand">
            <strong>服务人员工作台</strong>
            <span class="muted-line">先完善资质，再处理订单与消息</span>
          </div>
          <el-menu :default-active="route.path" class="worker-menu" router>
            <el-menu-item v-if="showMenu('WORKER_DASHBOARD_VIEW')" index="/worker/dashboard">工作台</el-menu-item>
            <el-menu-item
              v-if="showMenu('WORKER_ORDER_HANDLE') && workerQualified"
              index="/worker/orders"
            >
              <span>订单处理</span>
              <el-badge
                v-if="workerTodoCount"
                :value="workerTodoCount"
                :max="99"
                class="layout-menu-badge"
              />
            </el-menu-item>
            <el-menu-item v-if="showMenu('WORKER_MESSAGE_USE')" index="/worker/messages">
              <span>消息中心</span>
              <el-badge v-if="unreadCount" :value="unreadCount" :max="99" class="layout-menu-badge" />
            </el-menu-item>
            <el-menu-item v-if="showMenu('WORKER_QUALIFICATION_SUBMIT')" index="/worker/qualification">
              <span>资质材料</span>
              <el-badge
                v-if="!workerQualified"
                value="待处理"
                class="layout-menu-badge layout-menu-badge--text"
              />
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-container>
          <el-header class="worker-header">
            <div class="console-title-block">
              <div class="console-title">服务人员工作台</div>
              <div class="console-subtitle">
                {{ workerQualified ? '处理接单、上门服务和客户沟通' : '请先提交资质材料，审核通过后才能接单' }}
              </div>
            </div>
            <div class="console-header-actions">
              <el-tag
                effect="plain"
                :type="qualificationTagType"
                class="console-status-chip"
              >
                <span class="console-status-chip__label">资质状态</span>
                <strong>{{ qualificationLabel }}</strong>
              </el-tag>
              <el-tag
                v-if="workerQualified && workerTodoCount"
                effect="plain"
                type="warning"
                class="console-status-chip"
              >
                <span class="console-status-chip__label">当前待办</span>
                <strong>{{ workerTodoCount }} 项</strong>
              </el-tag>
              <el-badge v-if="showMenu('WORKER_MESSAGE_USE')" :value="unreadCount" :hidden="!unreadCount" :max="99">
                <el-button plain @click="router.push('/worker/messages')">消息</el-button>
              </el-badge>
              <el-button v-if="showMenu('WORKER_QUALIFICATION_SUBMIT')" plain @click="router.push('/worker/qualification')">
                {{ workerQualified ? '查看资质' : '完善资质' }}
              </el-button>
              <div class="console-user-chip">
                <el-avatar
                  :size="38"
                  :src="workerProfile?.avatarUrl || authStore.state.user?.avatarUrl || undefined"
                  class="console-user-avatar"
                >
                  {{ userInitial }}
                </el-avatar>
                <div class="console-user-meta">
                  <strong>{{ authStore.state.user?.realName || '服务人员' }}</strong>
                  <span>{{ formatRoleLabel(authStore.state.user?.roleCode) }}</span>
                </div>
              </div>
              <el-button type="danger" plain @click="logout">退出登录</el-button>
            </div>
          </el-header>
          <el-main class="worker-main">
            <router-view />
          </el-main>
        </el-container>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AmbientBackdrop from '../components/common/AmbientBackdrop.vue'
import { fetchCurrentWorkerProfile, fetchWorkerOrderSummary } from '../api'
import { authStore } from '../stores/auth'
import { notificationStore } from '../stores/notification'
import { formatRoleLabel } from '../utils/auth'
import { getWorkerTodoCount } from '../utils/workerConsole'
import {
  getWorkerQualificationLabel,
  getWorkerQualificationTagType,
  isWorkerQualificationApproved,
  normalizeWorkerQualificationStatus
} from '../utils/workerApplication'

const route = useRoute()
const router = useRouter()
const unreadCount = computed(() => notificationStore.getUnreadCount('worker'))
const userInitial = computed(() => (authStore.state.user?.realName || '服').slice(0, 1))
const workerProfile = ref(null)
const workerSummary = ref({
  total: 0,
  pending: 0,
  accepted: 0,
  confirmed: 0,
  inService: 0,
  waitingUserConfirmation: 0,
  todo: 0
})
let refreshTimer = null

const qualificationStatus = computed(() => normalizeWorkerQualificationStatus(workerProfile.value?.qualificationStatus))
const workerQualified = computed(() => isWorkerQualificationApproved(qualificationStatus.value))
const qualificationLabel = computed(() => getWorkerQualificationLabel(qualificationStatus.value))
const qualificationTagType = computed(() => getWorkerQualificationTagType(qualificationStatus.value))
const workerTodoCount = computed(() => {
  const summaryTodo = Number(workerSummary.value?.todo || 0)
  return summaryTodo || getWorkerTodoCount(workerSummary.value)
})

function showMenu(permissionCode) {
  if (!authStore.hasPermissionData()) {
    return true
  }
  return authStore.hasPermission(permissionCode)
}

async function loadWorkerConsoleState() {
  if (!authStore.hasRole('WORKER')) {
    workerProfile.value = null
    workerSummary.value = {
      total: 0,
      pending: 0,
      accepted: 0,
      confirmed: 0,
      inService: 0,
      waitingUserConfirmation: 0,
      todo: 0
    }
    return
  }

  try {
    const profile = await fetchCurrentWorkerProfile()
    workerProfile.value = profile

    if (!isWorkerQualificationApproved(profile?.qualificationStatus)) {
      workerSummary.value = {
        total: 0,
        pending: 0,
        accepted: 0,
        confirmed: 0,
        inService: 0,
        waitingUserConfirmation: 0,
        todo: 0
      }
      return
    }

    const summary = await fetchWorkerOrderSummary()
    workerSummary.value = {
      total: Number(summary?.total || 0),
      pending: Number(summary?.pending || 0),
      accepted: Number(summary?.accepted || 0),
      confirmed: Number(summary?.confirmed || 0),
      inService: Number(summary?.inService || 0),
      waitingUserConfirmation: Number(summary?.waitingUserConfirmation || 0),
      todo: Number(summary?.todo || 0)
    }
  } catch {
    workerProfile.value = null
  }
}

function startRefreshTimer() {
  stopRefreshTimer()
  refreshTimer = window.setInterval(() => {
    loadWorkerConsoleState()
  }, 30000)
}

function stopRefreshTimer() {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
    refreshTimer = null
  }
}

function logout() {
  authStore.logout()
  notificationStore.reset()
  stopRefreshTimer()
  router.push('/login')
}

watch(
  () => route.fullPath,
  () => {
    loadWorkerConsoleState()
  }
)

onMounted(() => {
  loadWorkerConsoleState()
  notificationStore.ensureLoaded('worker').catch(() => {})
  startRefreshTimer()
})

onBeforeUnmount(stopRefreshTimer)
</script>
