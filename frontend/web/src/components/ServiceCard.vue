<template>
  <el-card shadow="hover" class="service-card service-card--clean">
    <el-image :src="coverImage" :alt="category.name" fit="cover" class="service-card__image" />

    <div class="service-card__body">
      <div class="service-card__header">
        <div class="service-card__title">{{ category.name }}</div>
        <el-tag effect="plain" type="success">{{ category.priceLabel }}</el-tag>
      </div>

      <p class="service-card__desc">{{ category.description }}</p>

      <div class="service-card__meta">
        <span v-if="category.serviceDuration">{{ category.serviceDuration }}</span>
        <span v-if="category.serviceArea">{{ category.serviceArea }}</span>
      </div>

      <RouterLink :to="`/workers?serviceName=${encodeURIComponent(category.name)}`" class="service-card__link">
        <el-button type="primary">查看服务人员</el-button>
      </RouterLink>
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
.service-card--clean {
  overflow: hidden;
  border: 1px solid #e4e7ec;
  background: rgba(255, 255, 255, 0.76);
  backdrop-filter: blur(18px);
  box-shadow: 0 12px 28px rgba(16, 24, 40, 0.04);
}

.service-card__image {
  width: 100%;
  height: 196px;
  display: block;
}

.service-card__body {
  display: grid;
  gap: 14px;
  padding-top: 18px;
}

.service-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.service-card__title {
  font-size: 20px;
  font-weight: 700;
  color: #101828;
}

.service-card__desc {
  margin: 0;
  color: #475467;
  line-height: 1.7;
}

.service-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.service-card__meta span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid #e4e7ec;
  color: #475467;
  font-size: 12px;
}

.service-card__link {
  width: fit-content;
}
</style>
