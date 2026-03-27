<template>
  <div class="layout-shell public-shell">
    <AppHeader />
    <main class="layout-main public-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import AppHeader from '../components/AppHeader.vue'
</script>
