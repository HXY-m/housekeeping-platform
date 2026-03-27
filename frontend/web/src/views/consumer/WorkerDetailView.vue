<template>
  <div class="page-stack" v-loading="loading">
    <el-card v-if="worker" shadow="never">
      <div class="detail-header">
        <div>
          <el-tag type="success" round>{{ worker.roleLabel }}</el-tag>
          <h1>{{ worker.name }}</h1>
          <p>{{ worker.intro }}</p>
          <div class="tag-wrap">
            <el-tag v-for="tag in worker.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
          </div>
        </div>

        <el-card class="price-panel" shadow="hover">
          <el-statistic title="参考时薪" :value="worker.hourlyPrice" prefix="¥" suffix="/小时" />
          <p class="muted-line">{{ worker.nextAvailable }}</p>
          <el-space direction="vertical" fill style="width: 100%">
            <el-button class="full-width" type="primary" @click="router.push(`/booking/${worker.id}`)">
              预约这位服务人员
            </el-button>
            <el-button
              class="full-width"
              :type="isFavorited ? 'warning' : 'default'"
              plain
              @click="handleFavorite"
            >
              {{ isFavorited ? '取消收藏' : '收藏服务人员' }}
            </el-button>
          </el-space>
        </el-card>
      </div>
    </el-card>

    <el-row v-if="worker" :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header><strong>服务档案</strong></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="评分">{{ worker.rating }}</el-descriptions-item>
            <el-descriptions-item label="完成订单">{{ worker.completedOrders }}</el-descriptions-item>
            <el-descriptions-item label="从业年限">{{ worker.yearsOfExperience }} 年</el-descriptions-item>
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
