<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">服务人员资质审核</h1>
          <p class="page-panel__desc">
            审核服务人员提交的能力说明、服务区域和证明文件，所有材料集中展示在详情抽屉中，方便一次性核验。
          </p>
        </div>
        <div class="filter-actions">
          <el-radio-group v-model="statusFilter" size="small">
            <el-radio-button label="ALL">全部</el-radio-button>
            <el-radio-button label="PENDING">待审核</el-radio-button>
            <el-radio-button label="APPROVED">已通过</el-radio-button>
            <el-radio-button label="REJECTED">已驳回</el-radio-button>
          </el-radio-group>
          <el-button :loading="loading" @click="loadApplications">刷新</el-button>
        </div>
      </div>

      <div class="metric-strip">
        <div class="metric-chip">
          <span class="metric-chip__label">申请总数</span>
          <strong>{{ applications.length }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">待审核</span>
          <strong>{{ pendingCount }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">已通过</span>
          <strong>{{ approvedCount }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">已驳回</span>
          <strong>{{ rejectedCount }}</strong>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <div class="table-toolbar">
        <div class="table-toolbar__filters">
          <el-input
            v-model.trim="keyword"
            clearable
            placeholder="搜索姓名、电话、服务类型"
            style="width: 280px"
          />
        </div>
        <span class="section-caption">只有待审核记录保留操作按钮，已处理记录以结果追溯为主</span>
      </div>

      <el-table :data="pagedApplications" v-loading="loading" stripe style="margin-top: 16px">
        <el-table-column label="申请人" min-width="180">
          <template #default="{ row }">
            <div class="table-cell-primary">
              <strong>{{ row.realName }}</strong>
              <span class="table-cell-secondary">{{ row.phone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="serviceTypes" label="服务类型" min-width="180" show-overflow-tooltip />
        <el-table-column label="从业年限" width="100">
          <template #default="{ row }">{{ row.yearsOfExperience || 0 }} 年</template>
        </el-table-column>
        <el-table-column label="资质概览" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatWorkerCertificateSummary(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="serviceAreas" label="服务区域" min-width="180" show-overflow-tooltip />
        <el-table-column prop="availableSchedule" label="可接单时间" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-space wrap>
              <el-button size="small" @click="openDetail(row)">查看详情</el-button>
              <el-button
                v-if="row.status === 'PENDING'"
                size="small"
                type="success"
                @click="openReview(row, 'APPROVE')"
              >
                通过
              </el-button>
              <el-button
                v-if="row.status === 'PENDING'"
                size="small"
                type="danger"
                @click="openReview(row, 'REJECT')"
              >
                驳回
              </el-button>
              <span v-if="row.status !== 'PENDING'" class="muted-line">已处理</span>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <ListPagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizes"
        :total="total"
      />
    </el-card>

    <el-drawer v-model="detailVisible" title="资质申请详情" size="560px">
      <el-empty v-if="!selectedApplication" description="暂无可展示的申请资料" />
      <div v-else class="drawer-section">
        <el-tag size="large" :type="statusType(selectedApplication.status)">
          {{ statusLabel(selectedApplication.status) }}
        </el-tag>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请人">{{ selectedApplication.realName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedApplication.phone }}</el-descriptions-item>
          <el-descriptions-item label="服务类型">{{ selectedApplication.serviceTypes || '--' }}</el-descriptions-item>
          <el-descriptions-item label="从业年限">{{ selectedApplication.yearsOfExperience || 0 }} 年</el-descriptions-item>
          <el-descriptions-item label="服务区域">{{ selectedApplication.serviceAreas || '--' }}</el-descriptions-item>
          <el-descriptions-item label="可接单时间">{{ selectedApplication.availableSchedule || '--' }}</el-descriptions-item>
          <el-descriptions-item label="个人介绍">{{ selectedApplication.intro || '--' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ selectedApplication.createdAt || '--' }}</el-descriptions-item>
          <el-descriptions-item label="审核备注">{{ selectedApplication.adminRemark || '暂无' }}</el-descriptions-item>
        </el-descriptions>

        <div class="drawer-section__content">
          <div>
            <div class="drawer-section__title">资质标签</div>
            <div v-if="selectedLabels.length" class="tag-list">
              <el-tag v-for="item in selectedLabels" :key="item" effect="plain">
                {{ item }}
              </el-tag>
            </div>
            <span v-else class="muted-line">未填写资质标签</span>
          </div>

          <div>
            <div class="drawer-section__title">资质附件</div>
            <FileAttachmentList :files="selectedAttachments" empty-text="未上传资质附件" />
          </div>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="dialogVisible" title="审核资质申请" width="480px">
      <el-form label-position="top">
        <el-form-item label="审核动作">
          <el-tag :type="reviewAction === 'APPROVE' ? 'success' : 'danger'">
            {{ reviewAction === 'APPROVE' ? '审核通过' : '审核驳回' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input
            v-model.trim="reviewRemark"
            type="textarea"
            :rows="4"
            maxlength="255"
            show-word-limit
            placeholder="填写审核说明，便于服务人员补充资料或确认通过原因"
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
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import FileAttachmentList from '../../components/common/FileAttachmentList.vue'
import ListPagination from '../../components/common/ListPagination.vue'
import { useClientPagination } from '../../composables/useClientPagination'
import { fetchAdminWorkerApplications, reviewWorkerApplication } from '../../api'
import {
  formatWorkerCertificateSummary,
  getWorkerCertificateLabels,
  getWorkerQualificationAttachments
} from '../../utils/workerApplication'

const applications = ref([])
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref('ALL')
const dialogVisible = ref(false)
const detailVisible = ref(false)
const selectedId = ref(null)
const selectedApplication = ref(null)
const reviewAction = ref('APPROVE')
const reviewRemark = ref('')

const filteredApplications = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()

  return applications.value.filter((item) => {
    if (statusFilter.value !== 'ALL' && item.status !== statusFilter.value) {
      return false
    }

    if (!normalizedKeyword) {
      return true
    }

    return [item.realName, item.phone, item.serviceTypes, item.serviceAreas]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(normalizedKeyword))
  })
})

const { currentPage, pageSize, pageSizes, total, pagedItems: pagedApplications, resetPage } = useClientPagination(filteredApplications, 8)

watch([keyword, statusFilter], () => resetPage())

const pendingCount = computed(() => applications.value.filter((item) => item.status === 'PENDING').length)
const approvedCount = computed(() => applications.value.filter((item) => item.status === 'APPROVED').length)
const rejectedCount = computed(() => applications.value.filter((item) => item.status === 'REJECTED').length)
const selectedLabels = computed(() => getWorkerCertificateLabels(selectedApplication.value?.certificates))
const selectedAttachments = computed(() => getWorkerQualificationAttachments(selectedApplication.value))

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
  reviewRemark.value = row.adminRemark || ''
  dialogVisible.value = true
}

async function loadApplications() {
  loading.value = true
  try {
    const result = await fetchAdminWorkerApplications()
    applications.value = [...result].sort((left, right) => {
      if (left.status === right.status) {
        return String(right.createdAt || '').localeCompare(String(left.createdAt || ''))
      }
      if (left.status === 'PENDING') return -1
      if (right.status === 'PENDING') return 1
      return String(right.createdAt || '').localeCompare(String(left.createdAt || ''))
    })
    resetPage()
  } catch (error) {
    ElMessage.error(error.message || '获取资质申请失败')
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
    ElMessage.success('资质审核已完成')
    dialogVisible.value = false
    await loadApplications()
  } catch (error) {
    ElMessage.error(error.message || '资质审核失败')
  }
}

onMounted(loadApplications)
</script>
