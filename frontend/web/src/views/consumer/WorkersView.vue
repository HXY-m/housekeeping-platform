<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">找服务人员</h1>
          <p class="page-panel__desc">按服务类型筛选，优先查看真实头像、评分与完单量。</p>
        </div>
        <div class="filter-actions">
          <el-button plain @click="openFavorites">查看收藏</el-button>
        </div>
      </div>

      <div class="table-toolbar">
        <div class="table-toolbar__filters">
          <el-select
            v-model="serviceName"
            placeholder="全部服务类型"
            style="width: 220px"
            @change="handleFilterChange"
          >
            <el-option label="全部服务" value="" />
            <el-option v-for="item in serviceOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </div>
        <span class="section-caption">共 {{ total }} 位服务人员</span>
      </div>
    </el-card>

    <el-row v-if="loading" :gutter="16">
      <el-col v-for="item in pageSize" :key="`worker-loading-${item}`" :xs="24" :md="12" :xl="8">
        <el-card shadow="never" class="worker-browser-card worker-browser-card--skeleton">
          <el-skeleton animated>
            <template #template>
              <div class="worker-browser-card__skeleton-top">
                <el-skeleton-item variant="circle" class="worker-browser-card__skeleton-avatar" />
                <div class="worker-browser-card__skeleton-meta">
                  <el-skeleton-item variant="h3" style="width: 68%" />
                  <el-skeleton-item variant="text" style="width: 42%" />
                </div>
              </div>
              <div class="worker-browser-card__skeleton-body">
                <el-skeleton-item variant="text" style="width: 100%" />
                <el-skeleton-item variant="text" style="width: 88%" />
                <el-skeleton-item variant="text" style="width: 72%" />
              </div>
            </template>
          </el-skeleton>
        </el-card>
      </el-col>
    </el-row>

    <el-row v-else :gutter="16">
      <el-col v-for="worker in workers" :key="worker.id" :xs="24" :md="12" :xl="8">
        <WorkerCard :worker="worker">
          <template #actions>
            <div class="action-row worker-browser-card__actions">
              <el-button @click="router.push(`/workers/${worker.id}`)">查看详情</el-button>
              <el-button type="primary" @click="router.push(`/booking/${worker.id}`)">立即预约</el-button>
              <el-button
                :type="favoriteIds.includes(Number(worker.id)) ? 'warning' : 'default'"
                plain
                @click="handleFavorite(worker)"
              >
                {{ favoriteIds.includes(Number(worker.id)) ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </template>
        </WorkerCard>
      </el-col>
    </el-row>

    <ListPagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="pageSizes"
      :total="total"
    />
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { favoriteWorker, fetchFavoriteWorkerIds, fetchHome, fetchWorkers, unfavoriteWorker } from '../../api'
import { authStore } from '../../stores/auth'
import { useServerPagination } from '../../composables/useServerPagination'
import ListPagination from '../../components/common/ListPagination.vue'
import WorkerCard from '../../components/WorkerCard.vue'

const route = useRoute()
const router = useRouter()
const workers = ref([])
const loading = ref(false)
const serviceName = ref(route.query.serviceName ?? '')
const favoriteIds = ref([])
const serviceOptions = ref([])

const { currentPage, pageSize, pageSizes, total, buildParams, applyPageResult, resetPage } = useServerPagination(6)

async function loadServiceOptions() {
  try {
    const home = await fetchHome()
    serviceOptions.value = (home?.categories || []).map((item) => item.name)
  } catch {
    serviceOptions.value = ['日常保洁', '深度清洁', '母婴护理', '老人陪护', '家电清洗']
  }
}

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

async function handleFavorite(worker) {
  if (!authStore.isLoggedIn() || !authStore.hasRole('USER')) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  try {
    if (favoriteIds.value.includes(Number(worker.id))) {
      await unfavoriteWorker(worker.id)
      ElMessage.success('已取消收藏')
    } else {
      await favoriteWorker(worker.id)
      ElMessage.success('已加入收藏')
    }
    await loadFavoriteIds()
  } catch (error) {
    ElMessage.error(error.message || '收藏操作失败')
  }
}

function openFavorites() {
  if (!authStore.isLoggedIn() || !authStore.hasRole('USER')) {
    router.push({ path: '/login', query: { redirect: '/user/favorites' } })
    return
  }
  router.push('/user/favorites')
}

function handleFilterChange() {
  router.replace({
    path: '/workers',
    query: serviceName.value ? { serviceName: serviceName.value } : {}
  })
}

async function loadWorkers() {
  loading.value = true
  try {
    const [workerRows] = await Promise.all([
      fetchWorkers(buildParams({ serviceName: serviceName.value })),
      loadFavoriteIds()
    ])
    workers.value = applyPageResult(workerRows)
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query.serviceName,
  (value) => {
    serviceName.value = value ?? ''
    if (currentPage.value !== 1) {
      resetPage()
      return
    }
    loadWorkers()
  }
)

watch([currentPage, pageSize], () => {
  loadWorkers()
})

onMounted(async () => {
  await authStore.ensureLoaded()
  await loadServiceOptions()
  await loadWorkers()
})
</script>

<style scoped>
.worker-browser-card {
  border: 1px solid #e4e7ec;
}

.worker-browser-card--skeleton {
  min-height: 274px;
}

.worker-browser-card__skeleton-top {
  display: flex;
  align-items: center;
  gap: 16px;
}

.worker-browser-card__skeleton-avatar {
  width: 76px;
  height: 76px;
}

.worker-browser-card__skeleton-meta {
  flex: 1;
  display: grid;
  gap: 10px;
}

.worker-browser-card__skeleton-body {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.worker-browser-card__actions {
  width: 100%;
}
</style>
