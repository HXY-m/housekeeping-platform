<template>
  <div class="layout-shell public-shell">
    <ConsumerHeader />
    <main class="layout-main public-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import ConsumerHeader from '../components/public/ConsumerHeader.vue'
</script>
