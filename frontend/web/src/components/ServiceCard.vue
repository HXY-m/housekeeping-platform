<template>
  <el-card shadow="hover" class="service-card service-card--rich">
    <el-image :src="coverImage" :alt="category.name" fit="cover" class="service-card__image" />
    <div class="service-card__body">
      <div class="service-card__header">
        <div>
          <div class="service-card__title">{{ category.name }}</div>
          <div class="service-card__subtitle">标准化上门服务</div>
        </div>
        <el-tag type="warning">{{ category.priceLabel }}</el-tag>
      </div>
      <p class="service-card__desc">{{ category.description }}</p>
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
}

.service-card__image {
  width: 100%;
  height: 190px;
  display: block;
}

.service-card__body {
  padding-top: 18px;
}

.service-card__title {
  font-size: 20px;
  font-weight: 700;
}

.service-card__subtitle {
  margin-top: 6px;
  color: #6b7280;
  font-size: 13px;
}

.service-card__actions {
  margin-top: 18px;
}
</style>
