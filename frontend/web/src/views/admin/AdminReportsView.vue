<template>
  <div class="page-stack">
    <div class="console-overview console-overview--admin">
      <div>
        <el-tag type="danger" round>报表中心</el-tag>
        <h1>运营数据导出</h1>
        <p>按时间范围和状态筛选后，直接导出看板、订单、用户、售后和操作日志报表，方便测试、答辩和归档。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" :loading="downloading.dashboard" @click="exportDashboard">
          导出看板报表
        </el-button>
      </div>
    </div>

    <el-card shadow="never">
      <div class="filter-row reports-filter-row">
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          unlink-panels
        />
        <el-select v-model="filters.orderStatus" clearable placeholder="订单状态" style="width: 180px">
          <el-option label="待接单" value="PENDING" />
          <el-option label="已接单" value="ACCEPTED" />
          <el-option label="服务中" value="IN_SERVICE" />
          <el-option label="已完成" value="COMPLETED" />
        </el-select>
        <el-select v-model="filters.afterSaleStatus" clearable placeholder="售后状态" style="width: 180px">
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已解决" value="RESOLVED" />
          <el-option label="已关闭" value="CLOSED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-select v-model="filters.userRoleCode" clearable placeholder="用户角色" style="width: 180px">
          <el-option label="普通用户" value="USER" />
          <el-option label="服务人员" value="WORKER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
      </div>
      <div class="filter-row reports-filter-row">
        <el-input v-model.trim="filters.orderKeyword" clearable placeholder="订单关键词" style="width: 220px" />
        <el-input v-model.trim="filters.userKeyword" clearable placeholder="用户姓名" style="width: 180px" />
        <el-input v-model.trim="filters.userPhone" clearable placeholder="用户手机号" style="width: 180px" />
        <el-select v-model="filters.userStatus" clearable placeholder="用户状态" style="width: 180px">
          <el-option label="正常" value="ACTIVE" />
          <el-option label="禁用" value="DISABLED" />
          <el-option label="已删除" value="DELETED" />
        </el-select>
        <el-input v-model.trim="filters.operatorName" clearable placeholder="日志操作人" style="width: 180px" />
        <el-input v-model.trim="filters.actionType" clearable placeholder="日志动作类型" style="width: 180px" />
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12" :xl="8">
        <el-card shadow="never" class="report-card">
          <template #header><strong>订单报表</strong></template>
          <p class="muted-line">导出订单状态、预约信息、评价结果和最新履约进度。</p>
          <el-button
            class="full-width"
            type="primary"
            :loading="downloading.orders"
            @click="exportOrders"
          >
            导出订单 CSV
          </el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12" :xl="8">
        <el-card shadow="never" class="report-card">
          <template #header><strong>用户报表</strong></template>
          <p class="muted-line">导出用户角色、状态、所在城市和服务人员档案绑定情况。</p>
          <el-button
            class="full-width"
            type="primary"
            :loading="downloading.users"
            @click="exportUsers"
          >
            导出用户 CSV
          </el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12" :xl="8">
        <el-card shadow="never" class="report-card">
          <template #header><strong>售后报表</strong></template>
          <p class="muted-line">导出售后状态、问题类型、凭证数量和处理结果。</p>
          <el-button
            class="full-width"
            type="primary"
            :loading="downloading.afterSales"
            @click="exportAfterSales"
          >
            导出售后 CSV
          </el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12" :xl="8">
        <el-card shadow="never" class="report-card">
          <template #header><strong>操作日志报表</strong></template>
          <p class="muted-line">导出后台治理行为，便于论文和答辩展示平台可追溯能力。</p>
          <el-button
            class="full-width"
            type="primary"
            :loading="downloading.logs"
            @click="exportOperationLogs"
          >
            导出日志 CSV
          </el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12" :xl="8">
        <el-card shadow="never" class="report-card report-card--accent">
          <template #header><strong>导出建议</strong></template>
          <ul class="report-tips">
            <li>先筛选日期区间，再导出订单和售后，便于形成阶段性数据截图。</li>
            <li>论文材料建议同时导出看板和日志，能同时体现业务结果与后台治理过程。</li>
            <li>如果要做答辩演示，推荐先导出 CSV，再用 Excel 做二次图表排版。</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  exportAdminAfterSalesReport,
  exportAdminDashboardReport,
  exportAdminOperationLogsReport,
  exportAdminOrdersReport,
  exportAdminUsersReport
} from '../../api'

const downloading = reactive({
  dashboard: false,
  orders: false,
  users: false,
  afterSales: false,
  logs: false
})

const filters = reactive({
  dateRange: [],
  orderStatus: '',
  afterSaleStatus: '',
  userRoleCode: '',
  userKeyword: '',
  userPhone: '',
  userStatus: '',
  orderKeyword: '',
  operatorName: '',
  actionType: ''
})

function dateRangeParams() {
  return {
    dateFrom: filters.dateRange?.[0] || '',
    dateTo: filters.dateRange?.[1] || ''
  }
}

async function runExport(key, action, successMessage) {
  downloading[key] = true
  try {
    await action()
    ElMessage.success(successMessage)
  } catch (error) {
    ElMessage.error(error.message || '导出失败')
  } finally {
    downloading[key] = false
  }
}

function exportDashboard() {
  return runExport('dashboard', () => exportAdminDashboardReport(), '看板报表已开始下载')
}

function exportOrders() {
  return runExport(
    'orders',
    () =>
      exportAdminOrdersReport({
        status: filters.orderStatus,
        keyword: filters.orderKeyword,
        ...dateRangeParams()
      }),
    '订单报表已开始下载'
  )
}

function exportUsers() {
  return runExport(
    'users',
    () =>
      exportAdminUsersReport({
        roleCode: filters.userRoleCode,
        realName: filters.userKeyword,
        phone: filters.userPhone,
        status: filters.userStatus
      }),
    '用户报表已开始下载'
  )
}

function exportAfterSales() {
  return runExport(
    'afterSales',
    () =>
      exportAdminAfterSalesReport({
        status: filters.afterSaleStatus,
        ...dateRangeParams()
      }),
    '售后报表已开始下载'
  )
}

function exportOperationLogs() {
  return runExport(
    'logs',
    () =>
      exportAdminOperationLogsReport({
        operatorName: filters.operatorName,
        actionType: filters.actionType,
        ...dateRangeParams()
      }),
    '日志报表已开始下载'
  )
}
</script>

<style scoped>
.reports-filter-row {
  align-items: center;
}

.report-card {
  height: 100%;
}

.report-card p {
  min-height: 48px;
  line-height: 1.7;
}

.report-card--accent {
  background: linear-gradient(180deg, rgba(254, 242, 242, 0.9), rgba(255, 255, 255, 1));
}

.report-tips {
  margin: 0;
  padding-left: 18px;
  color: #475569;
  line-height: 1.8;
}
</style>
