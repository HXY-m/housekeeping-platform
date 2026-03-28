<template>
  <div v-loading="loading" class="page-stack">
    <el-card class="hero-card hero-card--minimal" shadow="never">
      <div class="hero-grid hero-grid--minimal">
        <div class="hero-content--minimal">
          <h1 class="hero-title">预订值得信赖的家政服务</h1>
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

      <el-row :gutter="18">
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

      <el-row :gutter="18">
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

@media (max-width: 960px) {
  .hero-grid--minimal {
    grid-template-columns: 1fr;
  }
}
</style>
