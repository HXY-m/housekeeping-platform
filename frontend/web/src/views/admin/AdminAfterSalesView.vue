<template>
  <div class="page-stack">
    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="售后总量" :value="afterSaleStats.total" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="待处理" :value="afterSaleStats.pending" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="处理中" :value="afterSaleStats.processing" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="已解决" :value="afterSaleStats.resolved" />
      </el-card>
    </div>

    <el-card shadow="never">
      <template #header>
        <div class="section-title">
          <div>
            <h2>售后处理中台</h2>
            <p>查看售后诉求、核对凭证图片，并快速更新工单处理状态。</p>
          </div>
          <el-button @click="loadAfterSales">刷新</el-button>
        </div>
      </template>

      <div class="filter-row">
        <span class="muted-line">状态筛选</span>
        <el-radio-group v-model="statusFilter" size="small">
          <el-radio-button label="ALL">全部</el-radio-button>
          <el-radio-button label="PENDING">待处理</el-radio-button>
          <el-radio-button label="PROCESSING">处理中</el-radio-button>
          <el-radio-button label="RESOLVED">已解决</el-radio-button>
          <el-radio-button label="REJECTED">已驳回</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="filteredAfterSales" v-loading="loading" stripe>
        <el-table-column prop="orderId" label="订单号" width="90" />
        <el-table-column prop="serviceName" label="服务项目" min-width="140" />
        <el-table-column prop="workerName" label="服务人员" width="120" />
        <el-table-column prop="customerName" label="联系人" width="120" />
        <el-table-column label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusTagType(row.orderStatus)">
              {{ getOrderStatusLabel(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issueType" label="问题类型" min-width="160" />
        <el-table-column prop="content" label="问题描述" min-width="240" show-overflow-tooltip />
        <el-table-column label="凭证图片" min-width="220">
          <template #default="{ row }">
            <AttachmentGallery
              :attachments="row.attachments"
              compact
              empty-text="未上传凭证"
            />
          </template>
        </el-table-column>
        <el-table-column label="售后状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getAfterSaleStatusTagType(row.status)">
              {{ getAfterSaleStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="处理备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-space v-if="canHandle(row)">
              <el-button size="small" type="primary" @click="openHandle(row, 'PROCESSING')">处理中</el-button>
              <el-button size="small" type="success" @click="openHandle(row, 'RESOLVED')">已解决</el-button>
              <el-button size="small" type="danger" @click="openHandle(row, 'REJECTED')">驳回</el-button>
            </el-space>
            <span v-else class="muted-line">已处理完成</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="处理售后反馈" width="520px">
      <el-form label-position="top">
        <el-form-item label="处理状态">
          <el-tag :type="getAfterSaleStatusTagType(handleForm.status)">
            {{ getAfterSaleStatusLabel(handleForm.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="handleForm.adminRemark"
            type="textarea"
            :rows="4"
            maxlength="255"
            show-word-limit
            placeholder="例如：已电话回访用户，安排补做服务；或因证据不足暂不支持诉求。"
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AttachmentGallery from '../../components/common/AttachmentGallery.vue'
import { fetchAdminAfterSales, handleAdminAfterSale } from '../../api'
import {
  getAfterSaleStatusLabel,
  getAfterSaleStatusTagType
} from '../../utils/afterSale'
import { getOrderStatusLabel, getOrderStatusTagType } from '../../utils/order'

const afterSales = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const selectedId = ref(null)
const statusFilter = ref('ALL')
const handleForm = reactive({
  status: 'PROCESSING',
  adminRemark: ''
})

const filteredAfterSales = computed(() => {
  if (statusFilter.value === 'ALL') {
    return afterSales.value
  }
  return afterSales.value.filter((item) => item.status === statusFilter.value)
})

const afterSaleStats = computed(() => ({
  total: afterSales.value.length,
  pending: afterSales.value.filter((item) => item.status === 'PENDING').length,
  processing: afterSales.value.filter((item) => item.status === 'PROCESSING').length,
  resolved: afterSales.value.filter((item) => item.status === 'RESOLVED').length
}))

function canHandle(row) {
  return row.status !== 'RESOLVED' && row.status !== 'REJECTED'
}

function openHandle(row, status) {
  selectedId.value = row.id
  handleForm.status = status
  handleForm.adminRemark = row.adminRemark || ''
  dialogVisible.value = true
}

async function loadAfterSales() {
  loading.value = true
  try {
    afterSales.value = await fetchAdminAfterSales()
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

onMounted(loadAfterSales)
</script>
