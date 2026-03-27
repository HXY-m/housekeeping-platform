<template>
  <section class="stack">
    <div class="section-head">
      <div>
        <p class="eyebrow">选择服务人员</p>
        <h1>按服务类型和口碑筛选阿姨/师傅</h1>
      </div>
      <label class="filter-box">
        <span>服务类型</span>
        <select v-model="serviceName" @change="loadWorkers">
          <option value="">全部</option>
          <option>日常保洁</option>
          <option>深度清洁</option>
          <option>母婴护理</option>
          <option>老人陪护</option>
          <option>家电清洗</option>
        </select>
      </label>
    </div>
    <div class="grid grid-2">
      <WorkerCard v-for="worker in workers" :key="worker.id" :worker="worker" />
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchWorkers } from '../api'
import WorkerCard from '../components/WorkerCard.vue'

const route = useRoute()
const serviceName = ref(route.query.serviceName ?? '')
const workers = ref([])

async function loadWorkers() {
  workers.value = await fetchWorkers(serviceName.value)
}

onMounted(loadWorkers)
</script>
