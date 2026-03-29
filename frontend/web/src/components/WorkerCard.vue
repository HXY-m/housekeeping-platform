<template>
  <el-card shadow="hover" class="worker-card worker-card--trust">
    <div class="worker-card__body">
      <div class="worker-card__top">
        <div class="worker-card__profile">
          <el-avatar :src="coverImage" :size="76" class="worker-card__avatar" />
          <div class="worker-card__identity">
            <div class="worker-card__name">{{ worker.name }}</div>
            <div class="worker-card__meta">{{ worker.city }}</div>
            <div class="worker-card__badges">
              <span class="worker-card__badge">实名认证</span>
              <span class="worker-card__badge">平台可预约</span>
            </div>
          </div>
        </div>
        <el-tag type="success" effect="plain">{{ formatCurrency(worker.hourlyPrice) }}/小时</el-tag>
      </div>

      <div class="worker-card__stats">
        <span>评分 {{ worker.rating }}</span>
        <span>已完成 {{ worker.completedOrders }} 单</span>
        <span>{{ worker.nextAvailable }}</span>
      </div>

      <p class="worker-card__intro">{{ worker.intro }}</p>

      <div class="worker-card__tags">
        <el-tag v-for="tag in worker.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
      </div>

      <div class="worker-card__footer">
        <slot name="actions">
          <RouterLink :to="`/workers/${worker.id}`">
            <el-button type="primary">查看详情</el-button>
          </RouterLink>
        </slot>
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
.worker-card--trust {
  border: 1px solid #e4e7ec;
  background: rgba(255, 255, 255, 0.76);
  backdrop-filter: blur(18px);
  box-shadow: 0 10px 24px rgba(16, 24, 40, 0.04);
}

.worker-card__body {
  display: grid;
  gap: 16px;
}

.worker-card__top,
.worker-card__footer {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.worker-card__profile {
  display: flex;
  align-items: center;
  gap: 16px;
}

.worker-card__avatar {
  border: 2px solid #fff;
  box-shadow: 0 8px 20px rgba(16, 24, 40, 0.08);
}

.worker-card__identity {
  display: grid;
  gap: 6px;
}

.worker-card__name {
  font-size: 20px;
  font-weight: 700;
  color: #101828;
}

.worker-card__meta {
  color: #667085;
  font-size: 13px;
}

.worker-card__badges,
.worker-card__stats,
.worker-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.worker-card__badge,
.worker-card__stats span {
  padding: 7px 11px;
  border-radius: 999px;
  font-size: 12px;
}

.worker-card__badge {
  background: rgba(0, 113, 227, 0.08);
  color: #005bb5;
}

.worker-card__stats span {
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid #e4e7ec;
  color: #344054;
}

.worker-card__intro {
  margin: 0;
  color: #475467;
  line-height: 1.7;
}

@media (max-width: 768px) {
  .worker-card__top,
  .worker-card__profile {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
