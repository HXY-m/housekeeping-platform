<template>
  <div class="page-stack">
    <div class="console-overview console-overview--admin">
      <div>
        <el-tag type="danger" round>用户治理</el-tag>
        <h1>平台用户管理</h1>
        <p>统一管理普通用户、服务人员与管理员账号，支持筛选、建档、编辑与逻辑删除。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="openCreate">新增用户</el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="账号总数" :value="users.length" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="启用账号" :value="activeCount" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="服务人员账号" :value="workerCount" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="管理员账号" :value="adminCount" />
      </el-card>
    </div>

    <el-card shadow="never">
      <div class="filter-row">
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
        <el-button type="primary" @click="loadUsers">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="pagedUsers" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="realName" label="姓名" width="140" />
        <el-table-column prop="phone" label="手机号" width="160" />
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag
                v-for="roleCode in row.roleCodes"
                :key="roleCode"
                :type="roleTagTypeMap[roleCode] || 'info'"
              >
                {{ roleLabelMap[roleCode] || roleCode }}
              </el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagTypeMap[row.status] || 'info'">
              {{ statusLabelMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="城市" width="120" />
        <el-table-column label="服务档案" width="120">
          <template #default="{ row }">
            <el-tag :type="row.workerProfileBound ? 'success' : 'info'">
              {{ row.workerProfileBound ? '已绑定' : '未绑定' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminUser,
  deleteAdminUser,
  fetchAdminUsers,
  updateAdminUser
} from '../../api'
import { useServerPagination } from '../../composables/useServerPagination'
import ListPagination from '../../components/common/ListPagination.vue'

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
const pagedUsers = users

const activeCount = computed(() => users.value.filter((item) => item.status === 'ACTIVE').length)
const workerCount = computed(() => users.value.filter((item) => item.roleCodes.includes('WORKER')).length)
const adminCount = computed(() => users.value.filter((item) => item.roleCodes.includes('ADMIN')).length)

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
  form.realName = row.realName
  form.phone = row.phone
  form.password = ''
  form.status = row.status
  form.roleCodes = [...row.roleCodes]
  dialogVisible.value = true
}

function resetFilters() {
  filters.roleCode = ''
  filters.realName = ''
  filters.phone = ''
  filters.status = ''
  if (currentPage.value !== 1) {
    resetPage()
    return
  }
  loadUsers()
}

async function loadUsers() {
  loading.value = true
  try {
    const result = await fetchAdminUsers(buildParams(filters))
    users.value = applyPageResult(result)
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
    roleCodes: form.roleCodes
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
    await ElMessageBox.confirm(`确认删除用户“${row.realName}”吗？`, '删除提示', {
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
