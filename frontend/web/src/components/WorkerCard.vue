<template>
  <el-card shadow="hover" class="worker-card worker-card--rich">
    <div class="worker-card__media">
      <el-image :src="coverImage" :alt="worker.name" fit="cover" class="worker-card__image" />
      <div class="worker-card__overlay">
        <span class="worker-card__eyebrow">平台甄选</span>
      </div>
    </div>

    <div class="worker-card__body">
      <div class="worker-card__top">
        <div>
          <div class="worker-card__name">{{ worker.name }}</div>
          <div class="worker-card__meta">{{ worker.roleLabel }} · {{ worker.city }}</div>
        </div>
        <el-tag type="success" effect="light">{{ formatCurrency(worker.hourlyPrice) }}/小时</el-tag>
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
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(251, 247, 241, 0.94));
}

.worker-card__media {
  position: relative;
}

.worker-card__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  padding: 18px;
  background: linear-gradient(180deg, rgba(38, 28, 18, 0.2), transparent 46%);
}

.worker-card__eyebrow {
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  color: #fff8f1;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.worker-card__image {
  width: 100%;
  height: 228px;
  display: block;
}

.worker-card__body {
  padding-top: 20px;
  display: grid;
  gap: 14px;
}

.worker-card__intro {
  margin: 0;
  line-height: 1.8;
}
</style>
