<template>
  <div class="layout-shell auth-shell">
    <div class="auth-stage">
      <section class="auth-showcase">
        <el-tag type="success" round>安心家政</el-tag>
        <h1>为家政服务平台提供稳定、清晰、可扩展的统一账号入口</h1>
        <p>
          普通用户可以注册、预约与评价，服务人员可以直接注册并维护资质资料，管理员则负责审核、运营与平台治理。
        </p>

        <div class="auth-feature-list">
          <div class="auth-feature-item">
            <strong>统一认证入口</strong>
            <span>登录与注册入口统一，但登录后会自动进入各自独立的业务工作台。</span>
          </div>
          <div class="auth-feature-item">
            <strong>普通用户和服务人员都可直接注册</strong>
            <span>服务人员注册完成后即可进入工作台，后续再补充资质文件与接单信息。</span>
          </div>
          <div class="auth-feature-item">
            <strong>便于后续扩展</strong>
            <span>当前已经拆分认证、角色跳转和权限控制，后面继续接短信登录、找回密码也更顺手。</span>
          </div>
        </div>
      </section>

      <main class="auth-main">
        <router-view />
        <div class="auth-switch">
          <span>{{ isRegisterPage ? '已经有账号了？' : '还没有账号？' }}</span>
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
