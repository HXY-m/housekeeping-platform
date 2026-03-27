<template>
  <article class="card worker-card">
    <div class="worker-top">
      <div>
        <p class="eyebrow">{{ worker.roleLabel }}</p>
        <h3>{{ worker.name }}</h3>
      </div>
      <strong class="price">¥{{ worker.hourlyPrice }}/小时</strong>
    </div>
    <p class="worker-meta">评分 {{ worker.rating }} · 已完成 {{ worker.completedOrders }} 单 · {{ worker.city }}</p>
    <p>{{ worker.intro }}</p>
    <div class="tag-row">
      <span v-for="tag in worker.tags" :key="tag" class="tag">{{ tag }}</span>
    </div>
    <div class="service-footer">
      <span>{{ worker.nextAvailable }}</span>
      <RouterLink class="button button-ghost" :to="`/workers/${worker.id}`">查看详情</RouterLink>
    </div>
  </article>
</template>

<script setup>
defineProps({
  worker: {
    type: Object,
    required: true
  }
})
</script>
