<template>
  <section v-if="home" class="stack">
    <section class="hero">
      <div>
        <p class="eyebrow">毕业设计落地版</p>
        <h1>{{ home.headline }}</h1>
        <p class="hero-copy">{{ home.subHeadline }}</p>
        <div class="hero-actions">
          <RouterLink class="button" to="/workers">立即预约服务</RouterLink>
          <RouterLink class="button button-ghost" to="/admin">查看数据统计</RouterLink>
        </div>
      </div>
      <aside class="hero-panel">
        <h3>任务书重点模块</h3>
        <ul>
          <li>多角色用户管理</li>
          <li>家政服务项目管理</li>
          <li>在线预约与下单</li>
          <li>订单进度跟踪与评价反馈</li>
        </ul>
      </aside>
    </section>

    <section>
      <div class="section-head">
        <h2>热门家政服务</h2>
        <p>页面结构参考 Taskrabbit，但业务范围围绕家政预约和本地服务履约。</p>
      </div>
      <div class="grid grid-3">
        <ServiceCard v-for="category in home.categories" :key="category.id" :category="category" />
      </div>
    </section>

    <section>
      <div class="section-head">
        <h2>平台能力</h2>
      </div>
      <div class="grid grid-3">
        <article v-for="highlight in home.highlights" :key="highlight.title" class="card">
          <h3>{{ highlight.title }}</h3>
          <p>{{ highlight.description }}</p>
        </article>
      </div>
    </section>

    <section>
      <div class="section-head">
        <h2>推荐服务人员</h2>
        <RouterLink class="text-link" to="/workers">查看全部</RouterLink>
      </div>
      <div class="grid grid-2">
        <WorkerCard v-for="worker in home.featuredWorkers" :key="worker.id" :worker="worker" />
      </div>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchHome } from '../api'
import ServiceCard from '../components/ServiceCard.vue'
import WorkerCard from '../components/WorkerCard.vue'

const home = ref(null)

onMounted(async () => {
  home.value = await fetchHome()
})
</script>
