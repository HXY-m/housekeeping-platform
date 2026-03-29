<template>
  <div class="page-stack">
    <div class="console-overview console-overview--admin">
      <div>
        <el-tag type="danger" round>用户管理</el-tag>
        <h1>平台账号管理</h1>
        <p>统一维护普通用户、服务人员和管理员账号，重点关注状态、角色和服务档案绑定情况。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="openCreate">新增用户</el-button>
      </div>
    </div>

    <div class="metric-strip">
      <div class="metric-chip">
        <span class="metric-chip__label">账号总数</span>
        <strong>{{ summary.total }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">启用账号</span>
        <strong>{{ summary.active }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">服务人员账号</span>
        <strong>{{ summary.workers }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">管理员账号</span>
        <strong>{{ summary.admins }}</strong>
      </div>
    </div>

    <el-card shadow="never" class="page-panel">
      <div class="card-header-between">
        <div>
          <strong>筛选与列表</strong>
          <p class="section-caption">按姓名、手机号、角色和状态快速定位账号。</p>
        </div>
      </div>

      <div class="table-toolbar table-toolbar--dense">
        <div class="table-toolbar__filters">
          <el-input v-model="filters.realName" clearable placeholder="姓名" style="width: 180px" />
          <el-input v-model="filters.phone" clearable placeholder="手机号" style="width: 180px" />
          <el-select v-model="filters.roleCode" clearable placeholder="角色" style="width: 160px">
            <el-option label="普通用户" value="USER" />
            <el-option label="服务人员" value="WORKER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
          <el-select v-model="filters.status" clearable placeholder="状态" style="width: 160px">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
            <el-option label="已删除" value="DELETED" />
          </el-select>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <el-table :data="users" v-loading="loading" stripe style="margin-top: 16px">
        <el-table-column label="用户信息" min-width="220">
          <template #default="{ row }">
            <div class="table-cell-primary">
              <strong>{{ row.realName || '未命名用户' }}</strong>
              <span class="table-cell-secondary">{{ row.phone || '未绑定手机号' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            <div class="tag-group">
              <el-tag
                v-for="roleCode in row.roleCodes || []"
                :key="roleCode"
                :type="roleTagTypeMap[roleCode] || 'info'"
                effect="plain"
              >
                {{ roleLabelMap[roleCode] || roleCode }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagTypeMap[row.status] || 'info'">
              {{ statusLabelMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="所在城市" width="140" />
        <el-table-column label="服务档案" width="140">
          <template #default="{ row }">
            <el-tag :type="row.workerProfileBound ? 'success' : 'info'" effect="plain">
              {{ row.workerProfileBound ? '已绑定' : '未绑定' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-space>
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增用户' : '编辑用户'" width="640px">
      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="form.realName" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="dialogMode === 'create' ? '登录密码' : '重置密码（可选）'">
              <el-input v-model="form.password" type="password" show-password maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账号状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="启用" value="ACTIVE" />
                <el-option label="禁用" value="DISABLED" />
                <el-option label="已删除" value="DELETED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="角色">
          <el-checkbox-group v-model="form.roleCodes">
            <el-checkbox label="USER">普通用户</el-checkbox>
            <el-checkbox label="WORKER">服务人员</el-checkbox>
            <el-checkbox label="ADMIN">管理员</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminUser,
  deleteAdminUser,
  fetchAdminUserSummary,
  fetchAdminUsers,
  updateAdminUser
} from '../../api'
import ListPagination from '../../components/common/ListPagination.vue'
import { useServerPagination } from '../../composables/useServerPagination'

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

const statusLabelMap = {
  ACTIVE: '启用',
  DISABLED: '禁用',
  DELETED: '已删除'
}

const statusTagTypeMap = {
  ACTIVE: 'success',
  DISABLED: 'warning',
  DELETED: 'info'
}

const loading = ref(false)
const users = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const summary = ref({
  total: 0,
  active: 0,
  workers: 0,
  admins: 0
})

const filters = reactive({
  roleCode: '',
  realName: '',
  phone: '',
  status: ''
})

const form = reactive({
  realName: '',
  phone: '',
  password: '',
  status: 'ACTIVE',
  roleCodes: ['USER']
})

const { currentPage, pageSize, pageSizes, total, buildParams, applyPageResult, resetPage } = useServerPagination(10)

function resetForm() {
  form.realName = ''
  form.phone = ''
  form.password = ''
  form.status = 'ACTIVE'
  form.roleCodes = ['USER']
  editingId.value = null
}

function openCreate() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  form.realName = row.realName || ''
  form.phone = row.phone || ''
  form.password = ''
  form.status = row.status || 'ACTIVE'
  form.roleCodes = Array.isArray(row.roleCodes) && row.roleCodes.length ? [...row.roleCodes] : ['USER']
  dialogVisible.value = true
}

function buildFilters() {
  return {
    roleCode: filters.roleCode,
    realName: filters.realName.trim(),
    phone: filters.phone.trim(),
    status: filters.status
  }
}

function handleSearch() {
  if (currentPage.value !== 1) {
    resetPage()
    return
  }
  loadUsers()
}

function resetFilters() {
  filters.roleCode = ''
  filters.realName = ''
  filters.phone = ''
  filters.status = ''
  handleSearch()
}

async function loadUsers() {
  loading.value = true
  try {
    const params = buildFilters()
    const [result, summaryResult] = await Promise.all([
      fetchAdminUsers(buildParams(params)),
      fetchAdminUserSummary(params)
    ])
    users.value = applyPageResult(result)
    summary.value = {
      total: Number(summaryResult?.total || 0),
      active: Number(summaryResult?.active || 0),
      workers: Number(summaryResult?.workers || 0),
      admins: Number(summaryResult?.admins || 0)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

async function submitForm() {
  if (!form.realName.trim() || !form.phone.trim()) {
    ElMessage.warning('请先填写姓名和手机号')
    return
  }
  if (dialogMode.value === 'create' && form.password.trim().length < 6) {
    ElMessage.warning('新建用户时密码至少 6 位')
    return
  }
  if (!form.roleCodes.length) {
    ElMessage.warning('请至少选择一个角色')
    return
  }

  const payload = {
    realName: form.realName.trim(),
    phone: form.phone.trim(),
    password: form.password.trim(),
    status: form.status,
    roleCodes: [...form.roleCodes]
  }

  try {
    if (dialogMode.value === 'create') {
      await createAdminUser(payload)
      ElMessage.success('用户创建成功')
    } else {
      await updateAdminUser(editingId.value, payload)
      ElMessage.success('用户更新成功')
    }
    dialogVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '保存用户失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除用户“${row.realName || row.phone}”吗？`, '删除提示', {
      type: 'warning'
    })
    await deleteAdminUser(row.id)
    ElMessage.success('用户已标记删除')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除用户失败')
    }
  }
}

watch([currentPage, pageSize], () => {
  loadUsers()
})

onMounted(loadUsers)
</script>
