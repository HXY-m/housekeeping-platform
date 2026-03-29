<template>
  <div class="page-stack">
    <el-card class="hero-card hero-card--minimal" shadow="never">
      <div class="hero-grid hero-grid--minimal">
        <div class="hero-content--minimal">
          <h1 class="hero-title">预约值得信赖的家政服务</h1>
          <p class="hero-copy">按服务类型快速筛选，查看真实服务人员资料，再完成预约。</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="router.push('/workers')">找服务人员</el-button>
            <el-button size="large" @click="router.push('/register?roleCode=WORKER')">注册服务人员</el-button>
          </div>

          <div class="trust-strip">
            <span>实名认证</span>
            <span>价格透明</span>
            <span>支持售后</span>
          </div>
        </div>

        <div class="trust-panel">
          <div class="trust-panel__item">
            <strong>{{ home?.featuredWorkers?.length || 0 }}</strong>
            <span>推荐服务人员</span>
          </div>
          <div class="trust-panel__item">
            <strong>{{ home?.categories?.length || 0 }}</strong>
            <span>服务类型</span>
          </div>
        </div>
      </div>
    </el-card>

    <section class="section-block">
      <div class="section-title">
        <div>
          <h2>服务项目</h2>
        </div>
        <el-button text type="primary" @click="router.push('/workers')">查看全部</el-button>
      </div>

      <el-row v-if="loading" :gutter="18">
        <el-col v-for="item in 3" :key="`service-skeleton-${item}`" :xs="24" :sm="12" :lg="8">
          <el-card shadow="never" class="skeleton-card">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" class="skeleton-card__image" />
                <div class="skeleton-card__body">
                  <el-skeleton-item variant="h3" style="width: 56%" />
                  <el-skeleton-item variant="text" style="width: 100%" />
                  <el-skeleton-item variant="text" style="width: 72%" />
                </div>
              </template>
            </el-skeleton>
          </el-card>
        </el-col>
      </el-row>

      <el-row v-else :gutter="18">
        <el-col
          v-for="category in home?.categories || []"
          :key="category.id"
          :xs="24"
          :sm="12"
          :lg="8"
        >
          <ServiceCard :category="category" />
        </el-col>
      </el-row>
    </section>

    <section class="section-block">
      <div class="section-title">
        <div>
          <h2>推荐服务人员</h2>
        </div>
        <el-button text type="primary" @click="router.push('/workers')">更多服务人员</el-button>
      </div>

      <el-row v-if="loading" :gutter="18">
        <el-col v-for="item in 3" :key="`worker-skeleton-${item}`" :xs="24" :sm="12" :lg="8">
          <el-card shadow="never" class="skeleton-card skeleton-card--worker">
            <el-skeleton animated>
              <template #template>
                <div class="worker-skeleton__top">
                  <el-skeleton-item variant="circle" class="worker-skeleton__avatar" />
                  <div class="worker-skeleton__meta">
                    <el-skeleton-item variant="h3" style="width: 72%" />
                    <el-skeleton-item variant="text" style="width: 48%" />
                  </div>
                </div>
                <div class="skeleton-card__body">
                  <el-skeleton-item variant="text" style="width: 100%" />
                  <el-skeleton-item variant="text" style="width: 88%" />
                  <el-skeleton-item variant="text" style="width: 64%" />
                </div>
              </template>
            </el-skeleton>
          </el-card>
        </el-col>
      </el-row>

      <el-row v-else :gutter="18">
        <el-col
          v-for="worker in home?.featuredWorkers || []"
          :key="worker.id"
          :xs="24"
          :sm="12"
          :lg="8"
        >
          <WorkerCard :worker="worker" />
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchHome } from '../../api'
import ServiceCard from '../../components/ServiceCard.vue'
import WorkerCard from '../../components/WorkerCard.vue'

const router = useRouter()
const home = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    home.value = await fetchHome()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hero-grid--minimal {
  grid-template-columns: minmax(0, 1fr) 220px;
  align-items: center;
}

.hero-content--minimal {
  display: grid;
  gap: 18px;
}

.trust-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.trust-strip span,
.trust-panel__item {
  border: 1px solid rgba(16, 24, 40, 0.08);
  border-radius: 999px;
  background: #fff;
  padding: 10px 14px;
  color: #344054;
  font-size: 13px;
}

.trust-panel {
  display: grid;
  gap: 12px;
}

.trust-panel__item {
  border-radius: 16px;
  padding: 16px;
  display: grid;
  gap: 6px;
}

.trust-panel__item strong {
  font-size: 28px;
  line-height: 1;
}

.skeleton-card {
  border: 1px solid #e4e7ec;
}

.skeleton-card__image {
  width: 100%;
  height: 196px;
}

.skeleton-card__body {
  display: grid;
  gap: 12px;
  padding-top: 18px;
}

.worker-skeleton__top {
  display: flex;
  align-items: center;
  gap: 16px;
}

.worker-skeleton__avatar {
  width: 76px;
  height: 76px;
}

.worker-skeleton__meta {
  flex: 1;
  display: grid;
  gap: 10px;
}

@media (max-width: 960px) {
  .hero-grid--minimal {
    grid-template-columns: 1fr;
  }
}
</style>
