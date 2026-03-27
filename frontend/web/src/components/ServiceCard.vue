<template>
  <el-card shadow="hover" class="service-card">
    <template #header>
      <div class="service-card__header">
        <span>{{ category.name }}</span>
        <el-tag type="warning">{{ category.priceLabel }}</el-tag>
      </div>
    </template>
    <p class="service-card__desc">{{ category.description }}</p>
    <RouterLink :to="`/workers?serviceName=${encodeURIComponent(category.name)}`">
      <el-button type="primary" link>查看服务人员</el-button>
    </RouterLink>
  </el-card>
</template>

<script setup>
defineProps({
  category: { type: Object, required: true }
})
</script>
