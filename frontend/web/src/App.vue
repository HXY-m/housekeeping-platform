<template>
  <router-view />
</template>

<script setup>
import { onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

watch(
  () => route.meta.surface,
  (surface) => {
    document.body.dataset.portal = surface || 'public'
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  delete document.body.dataset.portal
})
</script>
