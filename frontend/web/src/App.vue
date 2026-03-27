<template>
  <div class="app-shell">
    <AppHeader />
    <main class="page-shell">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import AppHeader from './components/AppHeader.vue'
import { authStore } from './stores/auth'

onMounted(() => {
  authStore.ensureLoaded()
})
</script>
