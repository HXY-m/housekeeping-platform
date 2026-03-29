<template>
  <div class="layout-shell auth-shell layout-shell--ambient">
    <AmbientBackdrop variant="auth" />
    <div class="layout-shell__content">
      <div
        class="auth-stage auth-stage--trust auth-stage--horizontal"
        :class="{ 'auth-stage--login': isLoginPage }"
      >
        <section
          class="auth-showcase auth-showcase--trust"
          :class="{ 'auth-showcase--login': isLoginPage }"
        >
          <RouterLink to="/" class="brand-link auth-brand-link">
            <div class="brand-mark">安</div>
            <div class="brand-copy">
              <strong>安心家政</strong>
              <span>清晰、可信、可追踪</span>
            </div>
          </RouterLink>

          <div v-if="!isLoginPage" class="auth-showcase__copy">
            <p class="auth-showcase__eyebrow">账户入口</p>
            <h1>{{ isRegisterPage ? '创建账户，开始预约家政服务' : '登录后继续处理预约与服务进度' }}</h1>
            <p>
              {{ isRegisterPage ? '普通用户与服务人员都可以直接注册，完成后进入对应工作台。' : '支持用户、服务人员、管理员三种角色登录，所有订单和消息集中可查。' }}
            </p>
          </div>

          <div v-if="!isLoginPage" class="auth-trust-list">
            <div class="auth-trust-chip">实名信息可追踪</div>
            <div class="auth-trust-chip">订单状态全程留痕</div>
            <div class="auth-trust-chip">售后与消息统一处理</div>
          </div>
        </section>

        <main class="auth-main auth-main--trust">
          <router-view />
          <div class="auth-switch auth-switch--compact">
            <span>{{ isRegisterPage ? '已经有账号' : '还没有账号' }}</span>
            <RouterLink :to="isRegisterPage ? '/login' : '/register'">
              {{ isRegisterPage ? '去登录' : '去注册' }}
            </RouterLink>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AmbientBackdrop from '../components/common/AmbientBackdrop.vue'

const route = useRoute()
const isRegisterPage = computed(() => route.path === '/register')
const isLoginPage = computed(() => route.path === '/login')
</script>
