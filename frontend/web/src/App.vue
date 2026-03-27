<template>
  <router-view />
</template>

<script setup>
import { onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import { authStore } from './stores/auth'
import { notificationStore } from './stores/notification'

const route = useRoute()

watch(
  () => [route.meta.surface, authStore.state.user?.roleCode],
  async ([surface, roleCode]) => {
    document.body.dataset.portal = surface || 'public'

    notificationStore.setActivePortal(surface || 'public')

    if (!roleCode || !['user', 'worker', 'admin'].includes(surface)) {
      if (!roleCode) {
        notificationStore.reset()
      }
      return
    }

    try {
      await notificationStore.ensureLoaded(surface)
    } catch {
      // Ignore notification bootstrap failures at shell level.
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  delete document.body.dataset.portal
})
</script>
