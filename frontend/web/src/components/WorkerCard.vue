<template>
  <el-card shadow="hover" class="worker-card">
    <div class="worker-card__top">
      <div>
        <div class="worker-card__name">{{ worker.name }}</div>
        <div class="worker-card__meta">{{ worker.roleLabel }} · {{ worker.city }}</div>
      </div>
      <el-tag type="success">¥{{ worker.hourlyPrice }}/小时</el-tag>
    </div>

    <div class="worker-card__stats">
      <span>评分 {{ worker.rating }}</span>
      <span>已完成 {{ worker.completedOrders }} 单</span>
    </div>

    <p class="worker-card__intro">{{ worker.intro }}</p>

    <div class="worker-card__tags">
      <el-tag v-for="tag in worker.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
    </div>

    <div class="worker-card__footer">
      <span>{{ worker.nextAvailable }}</span>
      <RouterLink :to="`/workers/${worker.id}`">
        <el-button type="primary">查看详情</el-button>
      </RouterLink>
    </div>
  </el-card>
</template>

<script setup>
defineProps({
  worker: { type: Object, required: true }
})
</script>
