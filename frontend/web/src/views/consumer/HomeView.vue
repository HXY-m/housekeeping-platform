<template>
  <div v-loading="loading" class="page-stack">
    <el-card class="hero-card home-hero" shadow="never">
      <div class="hero-grid">
        <div class="home-hero__content">
          <el-tag type="success" round>同城雅致家政服务平台</el-tag>
          <h1 class="hero-title">{{ home?.headline || '为每一次上门服务，保留从容与体面' }}</h1>
          <p class="hero-copy">
            {{
              home?.subHeadline ||
              '围绕服务浏览、在线预约、订单履约、售后反馈与后台治理，构建更适合毕业设计展示的完整家政服务预约平台。'
            }}
          </p>

          <div class="hero-actions">
            <el-button type="primary" size="large" @click="router.push('/workers')">浏览服务人员</el-button>
            <el-button size="large" @click="router.push('/register?roleCode=WORKER')">注册服务人员账号</el-button>
          </div>

          <div class="home-hero__notes">
            <div class="home-note">
              <strong>精选服务</strong>
              <span>热门保洁、收纳、陪护与专项护理一站式覆盖</span>
            </div>
            <div class="home-note">
              <strong>履约透明</strong>
              <span>订单状态、过程记录、评价与售后均可追踪</span>
            </div>
            <div class="home-note">
              <strong>角色清晰</strong>
              <span>普通用户、服务人员、管理员三端独立协同</span>
            </div>
          </div>
        </div>

        <el-card shadow="hover" class="home-hero__side">
          <div class="home-hero__side-title">平台亮点</div>
          <div class="home-highlight-list">
            <div v-for="highlight in home?.highlights || []" :key="highlight.title" class="home-highlight-card">
              <strong>{{ highlight.title }}</strong>
              <p>{{ highlight.description }}</p>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <section class="section-block">
      <div class="section-title">
        <div>
          <h2>热门服务项目</h2>
          <p>支持自定义展示图片，让首页服务卡片更适合项目演示与品牌表达。</p>
        </div>
        <el-button text type="primary" @click="router.push('/workers')">查看全部服务人员</el-button>
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
          <p>展示评分、经验、服务标签与下一可约时段，帮助用户更快完成选择。</p>
        </div>
        <el-button text type="primary" @click="router.push('/workers')">进入服务人员列表</el-button>
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
.home-hero {
  overflow: hidden;
}

.home-hero__content {
  display: grid;
  gap: 20px;
}

.home-hero__notes {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.home-note {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(143, 106, 73, 0.12);
  display: grid;
  gap: 6px;
}

.home-note span {
  color: #7a6f64;
  line-height: 1.7;
}

.home-hero__side {
  border: none;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(247, 240, 231, 0.88));
}

.home-hero__side-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 16px;
}

.home-highlight-list {
  display: grid;
  gap: 12px;
}

.home-highlight-card {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(143, 106, 73, 0.12);
}

.home-highlight-card p {
  margin: 10px 0 0;
  color: #7a6f64;
  line-height: 1.8;
}

@media (max-width: 960px) {
  .home-hero__notes {
    grid-template-columns: 1fr;
  }
}
</style>
