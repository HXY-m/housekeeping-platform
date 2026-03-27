<template>
  <section v-if="worker" class="stack">
    <div class="detail-hero">
      <div class="detail-copy">
        <p class="eyebrow">{{ worker.roleLabel }}</p>
        <h1>{{ worker.name }}</h1>
        <p class="worker-meta">评分 {{ worker.rating }} · 已完成 {{ worker.completedOrders }} 单 · {{ worker.city }}</p>
        <p>{{ worker.intro }}</p>
        <div class="tag-row">
          <span v-for="tag in worker.tags" :key="tag" class="tag">{{ tag }}</span>
        </div>
      </div>
      <aside class="detail-panel card">
        <p class="eyebrow">预约信息</p>
        <h3>¥{{ worker.hourlyPrice }}/小时</h3>
        <p>{{ worker.nextAvailable }}</p>
        <RouterLink class="button" :to="`/booking/${worker.id}`">预约这位服务人员</RouterLink>
      </aside>
    </div>

    <div class="grid grid-3">
      <article class="card">
        <h3>从业信息</h3>
        <p>经验年限：{{ worker.yearsOfExperience }} 年</p>
        <p>服务区域：{{ worker.serviceAreas.join('、') }}</p>
      </article>
      <article class="card">
        <h3>资质证书</h3>
        <p v-for="item in worker.certificates" :key="item">{{ item }}</p>
      </article>
      <article class="card">
        <h3>服务案例</h3>
        <p v-for="item in worker.serviceCases" :key="item">{{ item }}</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchWorker } from '../api'

const route = useRoute()
const worker = ref(null)

onMounted(async () => {
  worker.value = await fetchWorker(route.params.id)
})
</script>
