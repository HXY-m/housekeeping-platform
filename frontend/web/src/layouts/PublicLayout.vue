<template>
  <div class="layout-shell">
    <ConsumerHeader />
    <main class="layout-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import ConsumerHeader from '../components/public/ConsumerHeader.vue'
</script>
