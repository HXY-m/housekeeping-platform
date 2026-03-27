<template>
  <div class="layout-shell auth-shell">
    <div class="auth-stage">
      <section class="auth-showcase">
        <el-tag type="success" round>Housekeeping Hub</el-tag>
        <h1>为家政服务平台准备一套稳定的账户体系</h1>
        <p>
          普通用户可以注册、预约与评价，服务人员可以进入工作台履约，管理员则负责审核与运营。
        </p>

        <div class="auth-feature-list">
          <div class="auth-feature-item">
            <strong>统一登录入口</strong>
            <span>三类角色共用认证入口，但保持各自业务工作台独立。</span>
          </div>
          <div class="auth-feature-item">
            <strong>普通用户与服务人员都可直接注册</strong>
            <span>选择注册身份后即可进入对应控制台，服务人员后续在工作台维护资质资料与审核记录。</span>
          </div>
          <div class="auth-feature-item">
            <strong>后续功能更好扩展</strong>
            <span>认证 API 与角色默认跳转已解耦，便于继续添加找回密码或短信登录。</span>
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
