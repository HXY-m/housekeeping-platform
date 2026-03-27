<template>
  <div class="page-stack">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-between">
          <div>
            <strong>服务人员入驻审核</strong>
            <p class="muted-line">管理员可以审批普通用户提交的服务人员入驻申请。</p>
          </div>
          <el-button @click="loadApplications">刷新</el-button>
        </div>
      </template>

      <el-table :data="applications" v-loading="loading" stripe>
        <el-table-column prop="realName" label="申请人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="serviceTypes" label="服务类型" min-width="180" />
        <el-table-column prop="yearsOfExperience" label="从业年限" width="100" />
        <el-table-column prop="serviceAreas" label="服务区域" min-width="160" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="审核备注" min-width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-space v-if="row.status === 'PENDING'">
              <el-button size="small" type="success" @click="openReview(row, 'APPROVE')">通过</el-button>
              <el-button size="small" type="danger" @click="openReview(row, 'REJECT')">驳回</el-button>
            </el-space>
            <span v-else class="muted-line">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="审核入驻申请" width="480px">
      <el-form label-position="top">
        <el-form-item label="审核动作">
          <el-tag :type="reviewAction === 'APPROVE' ? 'success' : 'danger'">
            {{ reviewAction === 'APPROVE' ? '通过' : '驳回' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="reviewRemark" type="textarea" :rows="4" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminWorkerApplications, reviewWorkerApplication } from '../../api'

const applications = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const selectedId = ref(null)
const reviewAction = ref('APPROVE')
const reviewRemark = ref('')

function statusType(status) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

function openReview(row, action) {
  selectedId.value = row.id
  reviewAction.value = action
  reviewRemark.value = ''
  dialogVisible.value = true
}

async function loadApplications() {
  loading.value = true
  try {
    applications.value = await fetchAdminWorkerApplications()
  } catch (error) {
    ElMessage.error(error.message || '获取入驻申请失败')
  } finally {
    loading.value = false
  }
}

async function submitReview() {
  try {
    await reviewWorkerApplication(selectedId.value, {
      action: reviewAction.value,
      adminRemark: reviewRemark.value
    })
    ElMessage.success('审核完成')
    dialogVisible.value = false
    await loadApplications()
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  }
}

onMounted(loadApplications)
</script>
