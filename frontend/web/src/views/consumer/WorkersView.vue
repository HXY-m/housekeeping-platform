<template>
  <div class="page-stack">
    <el-card shadow="never">
      <div class="section-title">
        <div>
          <h1>找服务人员</h1>
          <p>按服务类型快速筛选，查看技能、价格和最近可约时间，也可以先收藏再比较。</p>
        </div>
        <el-space>
          <el-button plain @click="openFavorites">查看收藏</el-button>
        </el-space>
      </div>

      <el-form inline>
        <el-form-item label="服务类型">
          <el-select v-model="serviceName" placeholder="全部服务" style="width: 220px" @change="loadWorkers">
            <el-option label="全部" value="" />
            <el-option label="日常保洁" value="日常保洁" />
            <el-option label="深度清洁" value="深度清洁" />
            <el-option label="母婴护理" value="母婴护理" />
            <el-option label="老人陪护" value="老人陪护" />
            <el-option label="家电清洗" value="家电清洗" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" v-loading="loading">
      <el-col v-for="worker in workers" :key="worker.id" :xs="24" :md="12">
        <el-card shadow="hover" class="worker-card">
          <template #header>
            <div class="card-header-between">
              <div>
                <strong>{{ worker.name }}</strong>
                <p class="muted-line">{{ worker.roleLabel }}</p>
              </div>
              <el-tag type="success">{{ formatCurrency(worker.hourlyPrice) }}/小时</el-tag>
            </div>
          </template>

          <p>{{ worker.intro }}</p>

          <div class="tag-wrap">
            <el-tag v-for="tag in worker.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
          </div>

          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="评分">{{ worker.rating }}</el-descriptions-item>
            <el-descriptions-item label="完成单量">{{ worker.completedOrders }}</el-descriptions-item>
            <el-descriptions-item label="城市">{{ worker.city }}</el-descriptions-item>
            <el-descriptions-item label="最近可约">{{ worker.nextAvailable }}</el-descriptions-item>
          </el-descriptions>

          <div class="action-row">
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
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { favoriteWorker, fetchFavoriteWorkerIds, fetchWorkers, unfavoriteWorker } from '../../api'
import { authStore } from '../../stores/auth'
import { formatCurrency } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const workers = ref([])
const loading = ref(false)
const serviceName = ref(route.query.serviceName ?? '')
const favoriteIds = ref([])

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

async function loadWorkers() {
  loading.value = true
  try {
    const [workerRows] = await Promise.all([
      fetchWorkers(serviceName.value),
      loadFavoriteIds()
    ])
    workers.value = workerRows
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query.serviceName,
  (value) => {
    serviceName.value = value ?? ''
    loadWorkers()
  }
)

onMounted(async () => {
  await authStore.ensureLoaded()
  await loadWorkers()
})
</script>
