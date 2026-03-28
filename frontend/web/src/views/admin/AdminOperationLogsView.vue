<template>
  <div class="page-stack">
    <div class="console-overview console-overview--admin">
      <div>
        <el-tag type="danger" round>审计追踪</el-tag>
        <h1>平台操作日志</h1>
        <p>按人员、角色、动作类型和日期筛选关键治理操作，便于排查后台改动与业务流转。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="loadLogs">刷新日志</el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="日志总量" :value="logs.length" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="创建类操作" :value="createCount" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="审核类操作" :value="reviewCount" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="今日日志" :value="todayCount" />
      </el-card>
    </div>

    <el-card shadow="never">
      <div class="filter-row">
        <el-input v-model="filters.operatorName" clearable placeholder="操作人姓名" style="width: 180px" />
        <el-select v-model="filters.roleCode" clearable placeholder="角色" style="width: 160px">
          <el-option label="普通用户" value="USER" />
          <el-option label="服务人员" value="WORKER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
        <el-select v-model="filters.actionType" clearable placeholder="动作类型" style="width: 220px">
          <el-option
            v-for="item in actionOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
        <el-button type="primary" @click="loadLogs">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="pagedLogs" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="createdAt" label="时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="roleTagTypeMap[row.roleCode] || 'info'">
              {{ roleLabelMap[row.roleCode] || row.roleCode || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="动作类型" width="200">
          <template #default="{ row }">
            {{ actionLabelMap[row.actionType] || row.actionType }}
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="目标类型" width="140" />
        <el-table-column prop="targetId" label="目标ID" width="100" />
        <el-table-column prop="content" label="操作内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP 地址" width="140" />
      </el-table>

      <ListPagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizes"
        :total="total"
      />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminOperationLogs } from '../../api'
import ListPagination from '../../components/common/ListPagination.vue'
import { useServerPagination } from '../../composables/useServerPagination'
import { formatDateTime } from '../../utils/format'

const roleLabelMap = {
  USER: '普通用户',
  WORKER: '服务人员',
  ADMIN: '管理员'
}

const roleTagTypeMap = {
  USER: 'info',
  WORKER: 'warning',
  ADMIN: 'danger'
}

const actionLabelMap = {
  ORDER_CREATE: '创建订单',
  ORDER_ACCEPT: '服务人员接单',
  ORDER_START: '开始服务',
  ORDER_COMPLETE: '完成服务',
  ORDER_REVIEW: '订单评价',
  AFTER_SALE_CREATE: '创建售后',
  AFTER_SALE_HANDLE: '处理售后',
  WORKER_APPLICATION_SUBMIT: '提交资质资料',
  WORKER_APPLICATION_REVIEW: '审核服务人员资质',
  USER_CREATE: '创建用户',
  USER_UPDATE: '更新用户',
  USER_DELETE: '删除用户',
  CATEGORY_CREATE: '创建服务项目',
  CATEGORY_UPDATE: '更新服务项目',
  CATEGORY_DELETE: '删除服务项目'
}

const actionOptions = Object.entries(actionLabelMap).map(([value, label]) => ({ value, label }))

const loading = ref(false)
const logs = ref([])
const dateRange = ref([])

const filters = reactive({
  operatorName: '',
  roleCode: '',
  actionType: ''
})

const { currentPage, pageSize, pageSizes, total, buildParams, applyPageResult, resetPage } = useServerPagination(10)
const pagedLogs = logs

const createCount = computed(() =>
  logs.value.filter((item) => String(item.actionType || '').includes('CREATE')).length
)
const reviewCount = computed(() =>
  logs.value.filter((item) => String(item.actionType || '').includes('REVIEW')).length
)
const todayCount = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return logs.value.filter((item) => String(item.createdAt || '').slice(0, 10) === today).length
})

function resetFilters() {
  filters.operatorName = ''
  filters.roleCode = ''
  filters.actionType = ''
  dateRange.value = []
  if (currentPage.value !== 1) {
    resetPage()
    return
  }
  loadLogs()
}

async function loadLogs() {
  loading.value = true
  try {
    const result = await fetchAdminOperationLogs(buildParams({
      operatorName: filters.operatorName,
      roleCode: filters.roleCode,
      actionType: filters.actionType,
      dateFrom: dateRange.value?.[0] || '',
      dateTo: dateRange.value?.[1] || ''
    }))
    logs.value = applyPageResult(result)
  } catch (error) {
    ElMessage.error(error.message || '获取操作日志失败')
  } finally {
    loading.value = false
  }
}

watch([currentPage, pageSize], () => {
  loadLogs()
})

onMounted(loadLogs)
</script>
