<template>
  <div class="page-stack">
    <el-card shadow="never">
      <template #header>
        <div class="section-title">
          <div>
            <h2>售后处理中心</h2>
            <p>管理员可以查看用户售后反馈，并更新处理状态与处理备注。</p>
          </div>
          <el-button @click="loadAfterSales">刷新</el-button>
        </div>
      </template>

      <el-table :data="afterSales" v-loading="loading" stripe>
        <el-table-column prop="orderId" label="订单号" width="100" />
        <el-table-column prop="serviceName" label="服务项目" min-width="140" />
        <el-table-column prop="workerName" label="服务人员" width="120" />
        <el-table-column prop="customerName" label="联系人" width="120" />
        <el-table-column label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusTagType(row.orderStatus)">{{ getOrderStatusLabel(row.orderStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issueType" label="问题类型" min-width="160" />
        <el-table-column prop="content" label="问题描述" min-width="240" />
        <el-table-column label="售后状态" width="120">
          <template #default="{ row }">
            <el-tag :type="afterSaleTagType(row.status)">{{ afterSaleStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="处理备注" min-width="180" />
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
          <el-tag :type="afterSaleTagType(handleForm.status)">
            {{ afterSaleStatusLabel(handleForm.status) }}
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminAfterSales, handleAdminAfterSale } from '../../api'
import { getOrderStatusLabel, getOrderStatusTagType } from '../../utils/order'

const afterSales = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const selectedId = ref(null)
const handleForm = reactive({
  status: 'PROCESSING',
  adminRemark: ''
})

function afterSaleStatusLabel(status) {
  if (status === 'PENDING') return '待处理'
  if (status === 'PROCESSING') return '处理中'
  if (status === 'RESOLVED') return '已解决'
  if (status === 'REJECTED') return '已驳回'
  return status
}

function afterSaleTagType(status) {
  if (status === 'PENDING') return 'warning'
  if (status === 'PROCESSING') return 'primary'
  if (status === 'RESOLVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

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
