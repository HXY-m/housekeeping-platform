<template>
  <el-card shadow="hover" class="worker-card worker-card--clean">
    <el-image :src="coverImage" :alt="worker.name" fit="cover" class="worker-card__image" />

    <div class="worker-card__body">
      <div class="worker-card__top">
        <div>
          <div class="worker-card__name">{{ worker.name }}</div>
          <div class="worker-card__meta">{{ worker.city }}</div>
        </div>
        <el-tag type="success" effect="plain">{{ formatCurrency(worker.hourlyPrice) }}/小时</el-tag>
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
        <span class="worker-card__availability">{{ worker.nextAvailable }}</span>
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
.worker-card--clean {
  overflow: hidden;
  border: 1px solid #e4e7ec;
  background: #fff;
}

.worker-card__image {
  width: 100%;
  height: 220px;
  display: block;
}

.worker-card__body {
  display: grid;
  gap: 14px;
  padding-top: 18px;
}

.worker-card__top,
.worker-card__footer {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.worker-card__name {
  font-size: 20px;
  font-weight: 700;
  color: #101828;
}

.worker-card__meta,
.worker-card__availability {
  color: #667085;
  font-size: 13px;
}

.worker-card__stats,
.worker-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.worker-card__stats span {
  padding: 8px 12px;
  border-radius: 999px;
  background: #f8fafc;
  color: #344054;
  font-size: 12px;
}

.worker-card__intro {
  margin: 0;
  color: #475467;
  line-height: 1.7;
}
</style>
