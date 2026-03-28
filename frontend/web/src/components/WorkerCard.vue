<template>
  <el-card shadow="hover" class="worker-card worker-card--rich">
    <el-image :src="coverImage" :alt="worker.name" fit="cover" class="worker-card__image" />

    <div class="worker-card__body">
      <div class="worker-card__top">
        <div>
          <div class="worker-card__name">{{ worker.name }}</div>
          <div class="worker-card__meta">{{ worker.roleLabel }} · {{ worker.city }}</div>
        </div>
        <el-tag type="success">{{ formatCurrency(worker.hourlyPrice) }}/小时</el-tag>
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
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { formatCurrency } from '../utils/format'
import { getWorkerImage } from '../utils/displayAssets'

const props = defineProps({
  worker: { type: Object, required: true }
})

const coverImage = computed(() => getWorkerImage(props.worker))
</script>

<style scoped>
.worker-card--rich {
  overflow: hidden;
  border: none;
}

.worker-card__image {
  width: 100%;
  height: 220px;
  display: block;
}

.worker-card__body {
  padding-top: 18px;
}
</style>
