<template>
  <div class="page-stack">
    <el-card v-if="loading" shadow="never" class="worker-detail-card worker-detail-card--loading">
      <el-skeleton animated>
        <template #template>
          <div class="worker-detail-skeleton">
            <el-skeleton-item variant="circle" class="worker-detail-skeleton__avatar" />
            <div class="worker-detail-skeleton__meta">
              <el-skeleton-item variant="h1" style="width: 42%" />
              <el-skeleton-item variant="text" style="width: 24%" />
              <el-skeleton-item variant="text" style="width: 100%" />
              <el-skeleton-item variant="text" style="width: 72%" />
            </div>
          </div>
        </template>
      </el-skeleton>
    </el-card>

    <el-card v-else-if="worker" shadow="never" class="worker-detail-card">
      <div class="worker-detail-hero">
        <el-avatar :src="getWorkerImage(worker)" :size="140" class="worker-detail-hero__avatar" />

        <div class="worker-detail-hero__content">
          <div class="tag-wrap">
            <el-tag type="success" effect="plain">实名认证</el-tag>
            <el-tag type="success" effect="plain">平台可预约</el-tag>
          </div>
          <h1>{{ worker.name }}</h1>
          <p class="muted-line">{{ worker.city }}</p>

          <div class="worker-detail-metrics">
            <div class="worker-detail-metric">
              <span>评分</span>
              <strong>{{ worker.rating }}</strong>
            </div>
            <div class="worker-detail-metric">
              <span>完成订单</span>
              <strong>{{ worker.completedOrders }}</strong>
            </div>
            <div class="worker-detail-metric">
              <span>从业年限</span>
              <strong>{{ worker.yearsOfExperience }} 年</strong>
            </div>
          </div>

          <p class="worker-detail-intro">{{ worker.intro }}</p>

          <div class="tag-wrap">
            <el-tag v-for="tag in worker.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
          </div>

          <div class="worker-detail-actions">
            <el-button type="primary" @click="router.push(`/booking/${worker.id}`)">立即预约</el-button>
            <el-button :type="isFavorited ? 'warning' : 'default'" plain @click="handleFavorite">
              {{ isFavorited ? '取消收藏' : '收藏服务人员' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-row v-if="worker && !loading" :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header><strong>服务信息</strong></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="参考时薪">{{ worker.hourlyPrice }} 元 / 小时</el-descriptions-item>
            <el-descriptions-item label="最近可约">{{ worker.nextAvailable }}</el-descriptions-item>
            <el-descriptions-item label="服务区域">{{ worker.serviceAreas.join('、') }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header><strong>资质与案例</strong></template>
          <h4>资质证书</h4>
          <div class="tag-wrap">
            <el-tag v-for="item in worker.certificates" :key="item">{{ item }}</el-tag>
          </div>
          <h4>服务案例</h4>
          <el-timeline>
            <el-timeline-item v-for="item in worker.serviceCases" :key="item">{{ item }}</el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { favoriteWorker, fetchFavoriteWorkerIds, fetchWorker, unfavoriteWorker } from '../../api'
import { authStore } from '../../stores/auth'
import { getWorkerImage } from '../../utils/displayAssets'

const route = useRoute()
const router = useRouter()
const worker = ref(null)
const loading = ref(false)
const favoriteIds = ref([])

const isFavorited = computed(() => favoriteIds.value.includes(Number(route.params.id)))

async function loadFavoriteIds() {
  if (!authStore.isLoggedIn() || !authStore.hasRole('USER')) {
    favoriteIds.value = []
    return
  }
  try {
    favoriteIds.value = (await fetchFavoriteWorkerIds()).map((item) => Number(item))
  } catch {
    favoriteIds.value = []
  }
}

async function handleFavorite() {
  if (!worker.value) {
    return
  }
  if (!authStore.isLoggedIn() || !authStore.hasRole('USER')) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  try {
    if (isFavorited.value) {
      await unfavoriteWorker(worker.value.id)
      ElMessage.success('已取消收藏')
    } else {
      await favoriteWorker(worker.value.id)
      ElMessage.success('已加入收藏')
    }
    await loadFavoriteIds()
  } catch (error) {
    ElMessage.error(error.message || '收藏操作失败')
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await authStore.ensureLoaded()
    const [workerDetail] = await Promise.all([
      fetchWorker(route.params.id),
      loadFavoriteIds()
    ])
    worker.value = workerDetail
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.worker-detail-card {
  border: 1px solid rgba(16, 24, 40, 0.08);
}

.worker-detail-hero {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  gap: 28px;
  align-items: start;
}

.worker-detail-hero__avatar {
  border: 3px solid #fff;
  box-shadow: 0 14px 30px rgba(16, 24, 40, 0.08);
}

.worker-detail-hero__content {
  display: grid;
  align-content: start;
  gap: 16px;
}

.worker-detail-hero__content h1 {
  margin: 0;
}

.worker-detail-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.worker-detail-metric {
  border: 1px solid rgba(16, 24, 40, 0.08);
  border-radius: 16px;
  padding: 14px 16px;
  display: grid;
  gap: 6px;
  background: #fafafa;
}

.worker-detail-metric span {
  color: #667085;
  font-size: 12px;
}

.worker-detail-intro {
  margin: 0;
  line-height: 1.8;
}

.worker-detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.worker-detail-skeleton {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  gap: 28px;
  align-items: center;
}

.worker-detail-skeleton__avatar {
  width: 140px;
  height: 140px;
}

.worker-detail-skeleton__meta {
  display: grid;
  gap: 14px;
}

@media (max-width: 960px) {
  .worker-detail-hero,
  .worker-detail-skeleton {
    grid-template-columns: 1fr;
  }

  .worker-detail-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
