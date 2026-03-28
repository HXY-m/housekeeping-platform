<template>
  <div v-loading="loading" class="page-stack">
    <el-card class="hero-card" shadow="never">
      <div class="hero-grid">
        <div>
          <el-tag type="success" round>本地家政服务预约平台</el-tag>
          <h1 class="hero-title">{{ home?.headline || '快速找到靠谱的家政服务人员' }}</h1>
          <p class="hero-copy">
            {{ home?.subHeadline || '覆盖服务浏览、在线预约、订单履约、评价反馈和后台管理的一站式家政服务平台。' }}
          </p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="router.push('/workers')">立即预约</el-button>
            <el-button size="large" @click="router.push('/register?roleCode=WORKER')">注册服务人员账号</el-button>
          </div>
        </div>

        <el-card shadow="hover" class="hero-side">
          <h3>平台核心能力</h3>
          <el-timeline>
            <el-timeline-item v-for="highlight in home?.highlights || []" :key="highlight.title">
              <strong>{{ highlight.title }}</strong>
              <p>{{ highlight.description }}</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>
    </el-card>

    <section class="section-block">
      <div class="section-title">
        <div>
          <h2>热门服务分类</h2>
          <p>精选标准化家政服务，支持按项目快速查找合适的服务人员。</p>
        </div>
        <el-button text type="primary" @click="router.push('/workers')">查看全部服务人员</el-button>
      </div>
      <el-row :gutter="16">
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
          <p>展示评分、经验、服务标签和最近可预约时间，帮助你更快比较与选择。</p>
        </div>
        <el-button text type="primary" @click="router.push('/workers')">进入服务人员列表</el-button>
      </div>
      <el-row :gutter="16">
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
