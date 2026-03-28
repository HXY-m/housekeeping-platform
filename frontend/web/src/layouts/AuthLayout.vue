<template>
  <div class="layout-shell auth-shell">
    <div class="auth-stage auth-stage--minimal">
      <section class="auth-showcase auth-showcase--minimal">
        <RouterLink to="/" class="brand-link">
          <div class="brand-mark">安</div>
          <div class="brand-copy">
            <strong>安心家政</strong>
            <span>登录或注册</span>
          </div>
        </RouterLink>
      </section>

      <main class="auth-main">
        <router-view />
        <div class="auth-switch">
          <span>{{ isRegisterPage ? '已有账号' : '还没有账号' }}</span>
          <RouterLink :to="isRegisterPage ? '/login' : '/register'">
            {{ isRegisterPage ? '去登录' : '去注册' }}
          </RouterLink>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isRegisterPage = computed(() => route.path === '/register')
</script>
