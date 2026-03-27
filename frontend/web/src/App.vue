<template>
  <div class="app-shell">
    <AppHeader />
    <main class="page-shell">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import AppHeader from './components/AppHeader.vue'
</script>
