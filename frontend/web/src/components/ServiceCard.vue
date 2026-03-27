<template>
  <article class="card service-card">
    <p class="eyebrow">服务项目</p>
    <h3>{{ category.name }}</h3>
    <p>{{ category.description }}</p>
    <div class="service-footer">
      <span>{{ category.priceLabel }}</span>
      <RouterLink class="text-link" :to="`/workers?serviceName=${encodeURIComponent(category.name)}`">
        立即查看
      </RouterLink>
    </div>
  </article>
</template>

<script setup>
defineProps({
  category: {
    type: Object,
    required: true
  }
})
</script>
