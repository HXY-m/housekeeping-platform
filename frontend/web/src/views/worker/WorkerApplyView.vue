<template>
  <div class="page-stack">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <template #header>
            <div class="section-title">
              <div>
                <h1>申请成为服务人员</h1>
                <p>提交个人资质、服务类型和服务区域，等待管理员审核。</p>
              </div>
            </div>
          </template>

          <el-form :model="form" label-position="top">
            <el-row :gutter="16">
              <el-col :xs="24" :md="12">
                <el-form-item label="真实姓名">
                  <el-input v-model="form.realName" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="联系方式">
                  <el-input v-model="form.phone" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="擅长服务类型">
              <el-input v-model="form.serviceTypes" placeholder="例如：日常保洁,深度清洁" />
            </el-form-item>
            <el-form-item label="从业年限">
              <el-input-number v-model="form.yearsOfExperience" :min="0" :max="30" />
            </el-form-item>
            <el-form-item label="资质证书">
              <el-input v-model="form.certificates" placeholder="例如：家政服务培训证书,健康证" />
            </el-form-item>
            <el-form-item label="服务区域">
              <el-input v-model="form.serviceAreas" placeholder="例如：浦东新区,闵行区" />
            </el-form-item>
            <el-form-item label="服务时段">
              <el-input v-model="form.availableSchedule" placeholder="例如：工作日 09:00-18:00" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="form.intro" type="textarea" :rows="4" />
            </el-form-item>
            <el-button type="primary" @click="submitApplication">提交申请</el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header><strong>我的申请记录</strong></template>
          <el-timeline v-if="applications.length">
            <el-timeline-item
              v-for="item in applications"
              :key="item.id"
              :type="item.status === 'APPROVED' ? 'success' : item.status === 'REJECTED' ? 'danger' : 'warning'"
              :timestamp="item.createdAt"
            >
              <strong>{{ item.realName }}</strong>
              <p>服务类型：{{ item.serviceTypes }}</p>
              <p>状态：{{ item.status }}</p>
              <p v-if="item.adminRemark">备注：{{ item.adminRemark }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="还没有入驻申请记录" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMyWorkerApplications, submitWorkerApplication } from '../../api'

const applications = ref([])
const form = reactive({
  realName: '',
  phone: '',
  serviceTypes: '',
  yearsOfExperience: 0,
  certificates: '',
  serviceAreas: '',
  availableSchedule: '',
  intro: ''
})

async function loadApplications() {
  applications.value = await fetchMyWorkerApplications().catch(() => [])
}

async function submitApplication() {
  try {
    await submitWorkerApplication(form)
    ElMessage.success('入驻申请已提交')
    await loadApplications()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}

onMounted(loadApplications)
</script>
