<template>
  <div class="page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">售后处理</h1>
          <p class="page-panel__desc">
            售后工单集中展示问题类型、订单状态和凭证图片，先看详情，再更新处理状态，减少表格横向冗余。
          </p>
        </div>
        <div class="filter-actions">
          <el-radio-group v-model="statusFilter" size="small">
            <el-radio-button label="ALL">全部</el-radio-button>
            <el-radio-button label="PENDING">待处理</el-radio-button>
            <el-radio-button label="PROCESSING">处理中</el-radio-button>
            <el-radio-button label="RESOLVED">已解决</el-radio-button>
            <el-radio-button label="REJECTED">已驳回</el-radio-button>
          </el-radio-group>
          <el-button :loading="loading" @click="loadAfterSales">刷新</el-button>
        </div>
      </div>

      <div class="metric-strip">
        <div class="metric-chip">
          <span class="metric-chip__label">售后总量</span>
          <strong>{{ afterSaleStats.total }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">待处理</span>
          <strong>{{ afterSaleStats.pending }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">处理中</span>
          <strong>{{ afterSaleStats.processing }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">已解决</span>
          <strong>{{ afterSaleStats.resolved }}</strong>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <div class="table-toolbar">
        <div class="table-toolbar__filters">
          <el-input
            v-model.trim="keyword"
            clearable
            placeholder="搜索服务项目、用户、问题类型"
            style="width: 280px"
          />
        </div>
        <span class="section-caption">建议优先处理待处理和处理中工单</span>
      </div>

      <el-table :data="pagedAfterSales" v-loading="loading" stripe style="margin-top: 16px">
        <el-table-column label="订单 / 服务" min-width="180">
          <template #default="{ row }">
            <div class="table-cell-primary">
              <strong>#{{ row.orderId }} {{ row.serviceName }}</strong>
              <span class="table-cell-secondary">{{ row.workerName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="120">
          <template #default="{ row }">{{ row.customerName }}</template>
        </el-table-column>
        <el-table-column label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusTagType(row.orderStatus)">
              {{ getOrderStatusLabel(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issueType" label="问题类型" min-width="160" />
        <el-table-column prop="content" label="问题描述" min-width="240" show-overflow-tooltip />
        <el-table-column label="售后状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getAfterSaleStatusTagType(row.status)">
              {{ getAfterSaleStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="凭证" width="120">
          <template #default="{ row }">
            <span class="table-cell-secondary">{{ row.attachments?.length || 0 }} 张</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-space wrap>
              <el-button size="small" @click="openDetail(row)">查看详情</el-button>
              <el-button
                v-if="canHandle(row)"
                size="small"
                type="primary"
                @click="openHandle(row, 'PROCESSING')"
              >
                处理中
              </el-button>
              <el-button
                v-if="canHandle(row)"
                size="small"
                type="success"
                @click="openHandle(row, 'RESOLVED')"
              >
                已解决
              </el-button>
              <el-button
                v-if="canHandle(row)"
                size="small"
                type="danger"
                @click="openHandle(row, 'REJECTED')"
              >
                驳回
              </el-button>
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

    <el-drawer v-model="detailVisible" title="售后详情" size="560px">
      <el-empty v-if="!selectedAfterSale" description="暂无售后详情" />
      <div v-else class="drawer-section">
        <el-tag :type="getAfterSaleStatusTagType(selectedAfterSale.status)" size="large">
          {{ getAfterSaleStatusLabel(selectedAfterSale.status) }}
        </el-tag>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">#{{ selectedAfterSale.orderId }}</el-descriptions-item>
          <el-descriptions-item label="服务项目">{{ selectedAfterSale.serviceName || '--' }}</el-descriptions-item>
          <el-descriptions-item label="服务人员">{{ selectedAfterSale.workerName || '--' }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ selectedAfterSale.customerName || '--' }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            {{ getOrderStatusLabel(selectedAfterSale.orderStatus) }}
          </el-descriptions-item>
          <el-descriptions-item label="问题类型">{{ selectedAfterSale.issueType || '--' }}</el-descriptions-item>
          <el-descriptions-item label="问题描述">{{ selectedAfterSale.content || '--' }}</el-descriptions-item>
          <el-descriptions-item label="处理备注">
            {{ selectedAfterSale.adminRemark || '暂无' }}
          </el-descriptions-item>
        </el-descriptions>

        <div>
          <div class="drawer-section__title">凭证图片</div>
          <AttachmentGallery
            :attachments="selectedAfterSale.attachments"
            :limit="6"
            empty-text="未上传凭证"
          />
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="dialogVisible" title="处理售后工单" width="520px">
      <el-form label-position="top">
        <el-form-item label="处理状态">
          <el-tag :type="getAfterSaleStatusTagType(handleForm.status)">
            {{ getAfterSaleStatusLabel(handleForm.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model.trim="handleForm.adminRemark"
            type="textarea"
            :rows="4"
            maxlength="255"
            show-word-limit
            placeholder="记录处理过程和结果，便于后续追溯"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import AttachmentGallery from '../../components/common/AttachmentGallery.vue'
import ListPagination from '../../components/common/ListPagination.vue'
import { useServerPagination } from '../../composables/useServerPagination'
import { fetchAdminAfterSaleSummary, fetchAdminAfterSales, handleAdminAfterSale } from '../../api'
import { getAfterSaleStatusLabel, getAfterSaleStatusTagType } from '../../utils/afterSale'
import { getOrderStatusLabel, getOrderStatusTagType } from '../../utils/order'

const afterSales = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const selectedId = ref(null)
const selectedAfterSale = ref(null)
const statusFilter = ref('ALL')
const keyword = ref('')
const afterSaleStats = ref({
  total: 0,
  pending: 0,
  processing: 0,
  resolved: 0
})
const handleForm = reactive({
  status: 'PROCESSING',
  adminRemark: ''
})

const filteredAfterSales = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()

  return afterSales.value.filter((item) => {
    if (statusFilter.value !== 'ALL' && item.status !== statusFilter.value) {
      return false
    }

    if (!normalizedKeyword) {
      return true
    }

    return [item.serviceName, item.customerName, item.issueType]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(normalizedKeyword))
  })
})

const { currentPage, pageSize, pageSizes, total, buildParams, applyPageResult, resetPage } = useServerPagination(8)
const pagedAfterSales = afterSales

watch([keyword, statusFilter], () => {
  if (currentPage.value !== 1) {
    resetPage()
    return
  }
  loadAfterSales()
})

function canHandle(row) {
  return row.status !== 'RESOLVED' && row.status !== 'REJECTED'
}

function openDetail(row) {
  selectedAfterSale.value = row
  detailVisible.value = true
}

function openHandle(row, status) {
  selectedAfterSale.value = row
  selectedId.value = row.id
  handleForm.status = status
  handleForm.adminRemark = row.adminRemark || ''
  dialogVisible.value = true
}

async function loadAfterSales() {
  loading.value = true
  try {
    const params = {
      status: statusFilter.value === 'ALL' ? '' : statusFilter.value,
      keyword: keyword.value.trim()
    }
    const [result, summaryResult] = await Promise.all([
      fetchAdminAfterSales(buildParams(params)),
      fetchAdminAfterSaleSummary(params)
    ])
    afterSales.value = applyPageResult(result)
    afterSaleStats.value = {
      total: Number(summaryResult?.total || 0),
      pending: Number(summaryResult?.pending || 0),
      processing: Number(summaryResult?.processing || 0),
      resolved: Number(summaryResult?.resolved || 0)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取售后列表失败')
  } finally {
    loading.value = false
  }
}

async function submitHandle() {
  try {
    await handleAdminAfterSale(selectedId.value, {
      status: handleForm.status,
      adminRemark: handleForm.adminRemark
    })
    ElMessage.success('售后状态已更新')
    dialogVisible.value = false
    await loadAfterSales()
  } catch (error) {
    ElMessage.error(error.message || '处理售后失败')
  }
}

watch([currentPage, pageSize], () => {
  loadAfterSales()
})

onMounted(loadAfterSales)
</script>
