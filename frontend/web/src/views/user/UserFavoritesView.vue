<template>
  <div class="page-stack">
    <div class="console-overview">
      <div>
        <el-tag type="success" round>我的收藏</el-tag>
        <h1>收藏的服务人员</h1>
        <p>这里会同步保存你标记过的服务人员，方便下次直接比较和预约。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="router.push('/workers')">继续找服务人员</el-button>
      </div>
    </div>

    <el-empty v-if="!favorites.length && !loading" description="还没有收藏任何服务人员" />

    <el-row v-else :gutter="16" v-loading="loading">
      <el-col v-for="worker in favorites" :key="worker.id" :xs="24" :md="12">
        <el-card shadow="hover">
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
            <el-descriptions-item label="单量">{{ worker.completedOrders }}</el-descriptions-item>
            <el-descriptions-item label="城市">{{ worker.city }}</el-descriptions-item>
            <el-descriptions-item label="可预约">{{ worker.nextAvailable }}</el-descriptions-item>
          </el-descriptions>

          <div class="action-row">
            <el-button @click="router.push(`/workers/${worker.id}`)">查看详情</el-button>
            <el-button type="primary" @click="router.push(`/booking/${worker.id}`)">立即预约</el-button>
            <el-button plain @click="handleRemove(worker.id)">取消收藏</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchFavoriteWorkers, unfavoriteWorker } from '../../api'
import { formatCurrency } from '../../utils/format'

const router = useRouter()
const favorites = ref([])
const loading = ref(false)

async function loadFavorites() {
  loading.value = true
  try {
    favorites.value = await fetchFavoriteWorkers()
  } catch (error) {
    ElMessage.error(error.message || '获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

async function handleRemove(workerId) {
  try {
    await unfavoriteWorker(workerId)
    ElMessage.success('已取消收藏')
    await loadFavorites()
  } catch (error) {
    ElMessage.error(error.message || '取消收藏失败')
  }
}

onMounted(loadFavorites)
</script>
