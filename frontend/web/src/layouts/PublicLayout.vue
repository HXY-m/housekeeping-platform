<template>
  <div class="layout-shell public-shell layout-shell--ambient">
    <AmbientBackdrop variant="public" />
    <div class="layout-shell__content">
      <ConsumerHeader />
      <main class="layout-main public-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import AmbientBackdrop from '../components/common/AmbientBackdrop.vue'
import ConsumerHeader from '../components/public/ConsumerHeader.vue'
</script>
