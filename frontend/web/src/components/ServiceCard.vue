<template>
  <el-card shadow="hover" class="service-card service-card--rich">
    <div class="service-card__media">
      <el-image :src="coverImage" :alt="category.name" fit="cover" class="service-card__image" />
      <div class="service-card__overlay">
        <span class="service-card__eyebrow">甄选家政服务</span>
        <el-tag effect="dark" class="service-card__price">{{ category.priceLabel }}</el-tag>
      </div>
    </div>

    <div class="service-card__body">
      <div class="service-card__header">
        <div>
          <div class="service-card__title">{{ category.name }}</div>
          <div class="service-card__subtitle">适合注重品质与效率的上门服务场景</div>
        </div>
      </div>

      <p class="service-card__desc">{{ category.description }}</p>

      <div class="service-card__meta">
        <span v-if="category.serviceDuration">{{ category.serviceDuration }}</span>
        <span v-if="category.serviceArea">{{ category.serviceArea }}</span>
      </div>

      <div class="service-card__actions">
        <RouterLink :to="`/workers?serviceName=${encodeURIComponent(category.name)}`">
          <el-button type="primary">查看服务人员</el-button>
        </RouterLink>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { getServiceImage } from '../utils/displayAssets'

const props = defineProps({
  category: { type: Object, required: true }
})

const coverImage = computed(() => getServiceImage(props.category))
</script>

<style scoped>
.service-card--rich {
  overflow: hidden;
  border: none;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(251, 247, 241, 0.94));
}

.service-card__media {
  position: relative;
}

.service-card__image {
  width: 100%;
  height: 208px;
  display: block;
}

.service-card__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 18px;
  background: linear-gradient(180deg, rgba(38, 28, 18, 0.2), transparent 48%);
}

.service-card__eyebrow {
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff8f1;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.service-card__price {
  border: none;
  background: rgba(49, 34, 24, 0.56);
}

.service-card__body {
  padding-top: 20px;
  display: grid;
  gap: 14px;
}

.service-card__title {
  font-size: 22px;
  font-weight: 700;
}

.service-card__subtitle {
  margin-top: 8px;
  color: #7a6f64;
  font-size: 13px;
}

.service-card__desc {
  margin: 0;
  line-height: 1.8;
}

.service-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.service-card__meta span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(143, 106, 73, 0.1);
  color: #74583e;
  font-size: 12px;
}

.service-card__actions {
  margin-top: 4px;
}
</style>
