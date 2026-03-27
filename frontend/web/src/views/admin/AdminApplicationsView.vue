<template>
  <div class="page-stack">
    <div class="console-overview console-overview--admin">
      <div>
        <el-tag type="danger" round>资质审核</el-tag>
        <h1>服务人员资质审核</h1>
        <p>审核已注册服务人员提交的资质资料，决定是否开放展示与接单资格。</p>
      </div>
      <div class="hero-actions">
        <el-select v-model="statusFilter" clearable placeholder="筛选状态" style="width: 160px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-button @click="loadApplications">刷新</el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="资质提交总数" :value="applications.length" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="待审核" :value="pendingCount" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="审核通过" :value="approvedCount" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="审核驳回" :value="rejectedCount" />
      </el-card>
    </div>

    <el-card shadow="never">
      <el-table :data="filteredApplications" v-loading="loading" stripe>
        <el-table-column prop="realName" label="服务人员" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="serviceTypes" label="服务类型" min-width="180" show-overflow-tooltip />
        <el-table-column prop="yearsOfExperience" label="从业年限" width="100" />
        <el-table-column prop="certificates" label="证书 / 培训" min-width="180" show-overflow-tooltip />
        <el-table-column prop="serviceAreas" label="服务区域" min-width="160" show-overflow-tooltip />
        <el-table-column prop="availableSchedule" label="可服务时段" min-width="170" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="审核备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-space wrap>
              <el-button size="small" @click="openDetail(row)">查看资料</el-button>
              <template v-if="row.status === 'PENDING'">
                <el-button size="small" type="success" @click="openReview(row, 'APPROVE')">通过</el-button>
                <el-button size="small" type="danger" @click="openReview(row, 'REJECT')">驳回</el-button>
              </template>
              <span v-else class="muted-line">已处理</span>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="detailVisible" title="资质资料详情" size="520px">
      <el-empty v-if="!selectedApplication" description="暂无可展示资料" />
      <div v-else class="page-stack">
        <el-tag size="large" :type="statusType(selectedApplication.status)">
          {{ statusLabel(selectedApplication.status) }}
        </el-tag>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="服务人员">{{ selectedApplication.realName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedApplication.phone }}</el-descriptions-item>
          <el-descriptions-item label="服务类型">{{ selectedApplication.serviceTypes || '--' }}</el-descriptions-item>
          <el-descriptions-item label="从业年限">{{ selectedApplication.yearsOfExperience || 0 }} 年</el-descriptions-item>
          <el-descriptions-item label="证书 / 培训">{{ selectedApplication.certificates || '--' }}</el-descriptions-item>
          <el-descriptions-item label="服务区域">{{ selectedApplication.serviceAreas || '--' }}</el-descriptions-item>
          <el-descriptions-item label="可服务时段">{{ selectedApplication.availableSchedule || '--' }}</el-descriptions-item>
          <el-descriptions-item label="个人介绍">{{ selectedApplication.intro || '--' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ selectedApplication.createdAt || '--' }}</el-descriptions-item>
          <el-descriptions-item label="审核备注">{{ selectedApplication.adminRemark || '暂无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>

    <el-dialog v-model="dialogVisible" title="审核服务人员资质" width="480px">
      <el-form label-position="top">
        <el-form-item label="审核动作">
          <el-tag :type="reviewAction === 'APPROVE' ? 'success' : 'danger'">
            {{ reviewAction === 'APPROVE' ? '审核通过' : '审核驳回' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input
            v-model="reviewRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入审核说明，便于服务人员后续补充资料"
          />
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
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminWorkerApplications, reviewWorkerApplication } from '../../api'

const applications = ref([])
const loading = ref(false)
const statusFilter = ref('')
const dialogVisible = ref(false)
const detailVisible = ref(false)
const selectedId = ref(null)
const selectedApplication = ref(null)
const reviewAction = ref('APPROVE')
const reviewRemark = ref('')

const filteredApplications = computed(() => {
  if (!statusFilter.value) {
    return applications.value
  }
  return applications.value.filter((item) => item.status === statusFilter.value)
})

const pendingCount = computed(() => applications.value.filter((item) => item.status === 'PENDING').length)
const approvedCount = computed(() => applications.value.filter((item) => item.status === 'APPROVED').length)
const rejectedCount = computed(() => applications.value.filter((item) => item.status === 'REJECTED').length)

function statusType(status) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

function statusLabel(status) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  return '待审核'
}

function openDetail(row) {
  selectedApplication.value = row
  detailVisible.value = true
}

function openReview(row, action) {
  selectedApplication.value = row
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
    ElMessage.error(error.message || '获取资质资料失败')
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
    ElMessage.success('资质审核完成')
    dialogVisible.value = false
    await loadApplications()
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  }
}

onMounted(loadApplications)
</script>
