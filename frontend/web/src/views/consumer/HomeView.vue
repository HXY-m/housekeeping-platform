<template>
  <div v-loading="loading" class="page-stack">
    <el-card class="hero-card" shadow="never">
      <div class="hero-grid">
        <div>
          <el-tag type="success" round>Taskrabbit 风格家政平台</el-tag>
          <h1>{{ home?.headline || '快速找到靠谱的家政服务人员' }}</h1>
          <p>{{ home?.subHeadline || '围绕家政服务预约、履约、评价和管理后台的完整平台。' }}</p>
          <el-space wrap>
            <el-button type="primary" size="large" @click="router.push('/workers')">立即预约</el-button>
            <el-button size="large" @click="router.push('/worker/apply')">申请成为服务人员</el-button>
          </el-space>
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
          <p>从标准化项目切入，快速打通平台交易闭环。</p>
        </div>
      </div>
      <el-row :gutter="16">
        <el-col v-for="category in home?.categories || []" :key="category.id" :xs="24" :sm="12" :lg="8">
          <el-card class="category-card" shadow="hover">
            <template #header>
              <div class="card-header-between">
                <span>{{ category.name }}</span>
                <el-tag type="warning">{{ category.priceLabel }}</el-tag>
              </div>
            </template>
            <p>{{ category.description }}</p>
            <el-button link type="primary" @click="router.push(`/workers?serviceName=${encodeURIComponent(category.name)}`)">
              查看服务人员
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="section-block">
      <div class="section-title">
        <div>
          <h2>推荐服务人员</h2>
          <p>展示评分、经验、服务标签与最近可约时间。</p>
        </div>
      </div>
      <el-row :gutter="16">
        <el-col v-for="worker in home?.featuredWorkers || []" :key="worker.id" :xs="24" :sm="12" :lg="8">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header-between">
                <div>
                  <strong>{{ worker.name }}</strong>
                  <p class="muted-line">{{ worker.roleLabel }}</p>
                </div>
                <el-tag type="success">¥{{ worker.hourlyPrice }}/小时</el-tag>
              </div>
            </template>
            <p>{{ worker.intro }}</p>
            <div class="tag-wrap">
              <el-tag v-for="tag in worker.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
            </div>
            <div class="worker-meta-row">
              <span>评分 {{ worker.rating }}</span>
              <span>已完成 {{ worker.completedOrders }} 单</span>
            </div>
            <div class="worker-meta-row">
              <span>{{ worker.city }}</span>
              <span>{{ worker.nextAvailable }}</span>
            </div>
            <el-button class="full-width" type="primary" plain @click="router.push(`/workers/${worker.id}`)">
              查看详情
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchHome } from '../../api'

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
