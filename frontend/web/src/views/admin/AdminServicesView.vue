<template>
  <div class="page-stack">
    <div class="console-overview console-overview--admin">
      <div>
        <el-tag type="danger" round>服务治理</el-tag>
        <h1>服务项目管理</h1>
        <p>维护前台展示的家政服务分类，控制启停状态，并补充时长、范围、场景与增值服务信息。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="openCreate">新增项目</el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="项目总数" :value="categories.length" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="启用项目" :value="enabledCount" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="停用项目" :value="disabledCount" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="扩展信息完整" :value="richInfoCount" />
      </el-card>
    </div>

    <el-card shadow="never">
      <div class="filter-row">
        <el-input v-model="filters.keyword" clearable placeholder="搜索项目名称或描述" style="width: 280px" />
        <el-select v-model="filters.enabled" clearable placeholder="启用状态" style="width: 180px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button type="primary" @click="loadCategories">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="pagedCategories" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="项目名称" min-width="140" />
        <el-table-column prop="priceLabel" label="价格标签" width="140" />
        <el-table-column prop="serviceDuration" label="服务时长" width="140" />
        <el-table-column prop="serviceArea" label="服务范围" min-width="180" show-overflow-tooltip />
        <el-table-column prop="serviceScene" label="适用场景" min-width="180" show-overflow-tooltip />
        <el-table-column prop="extraServices" label="增值服务" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="slug" label="标识" width="160" />
        <el-table-column prop="description" label="项目描述" min-width="220" show-overflow-tooltip />
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

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增服务项目' : '编辑服务项目'" width="720px">
      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目名称">
              <el-input v-model="form.name" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格标签">
              <el-input v-model="form.priceLabel" maxlength="50" placeholder="例如：128元起 / 2小时起" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="标识">
              <el-input v-model="form.slug" maxlength="100" placeholder="例如：deep-cleaning" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务时长">
              <el-input v-model="form.serviceDuration" maxlength="100" placeholder="例如：2小时 / 4小时" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="服务范围">
              <el-input v-model="form.serviceArea" maxlength="255" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用场景">
              <el-input v-model="form.serviceScene" maxlength="255" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="增值服务">
          <el-input v-model="form.extraServices" maxlength="255" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="300" show-word-limit />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.enabled" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminCategory,
  deleteAdminCategory,
  fetchAdminCategories,
  updateAdminCategory
} from '../../api'
import { useClientPagination } from '../../composables/useClientPagination'
import ListPagination from '../../components/common/ListPagination.vue'

const loading = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)

const filters = reactive({
  keyword: '',
  enabled: null
})

const form = reactive({
  name: '',
  description: '',
  priceLabel: '',
  slug: '',
  serviceDuration: '',
  serviceArea: '',
  serviceScene: '',
  extraServices: '',
  enabled: true
})

const { currentPage, pageSize, pageSizes, total, pagedItems: pagedCategories, resetPage } = useClientPagination(categories, 10)

const enabledCount = computed(() => categories.value.filter((item) => item.enabled).length)
const disabledCount = computed(() => categories.value.filter((item) => !item.enabled).length)
const richInfoCount = computed(() =>
  categories.value.filter((item) => item.serviceDuration || item.serviceArea || item.serviceScene).length
)

function resetForm() {
  form.name = ''
  form.description = ''
  form.priceLabel = ''
  form.slug = ''
  form.serviceDuration = ''
  form.serviceArea = ''
  form.serviceScene = ''
  form.extraServices = ''
  form.enabled = true
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
  form.name = row.name
  form.description = row.description
  form.priceLabel = row.priceLabel
  form.slug = row.slug
  form.serviceDuration = row.serviceDuration || ''
  form.serviceArea = row.serviceArea || ''
  form.serviceScene = row.serviceScene || ''
  form.extraServices = row.extraServices || ''
  form.enabled = row.enabled
  dialogVisible.value = true
}

function resetFilters() {
  filters.keyword = ''
  filters.enabled = null
  loadCategories()
}

async function loadCategories() {
  loading.value = true
  try {
    categories.value = await fetchAdminCategories(filters)
    resetPage()
  } catch (error) {
    ElMessage.error(error.message || '获取服务项目失败')
  } finally {
    loading.value = false
  }
}

async function submitForm() {
  if (!form.name.trim() || !form.description.trim() || !form.priceLabel.trim()) {
    ElMessage.warning('请先补全项目名称、描述和价格标签')
    return
  }

  const payload = {
    name: form.name.trim(),
    description: form.description.trim(),
    priceLabel: form.priceLabel.trim(),
    slug: form.slug.trim(),
    serviceDuration: form.serviceDuration.trim(),
    serviceArea: form.serviceArea.trim(),
    serviceScene: form.serviceScene.trim(),
    extraServices: form.extraServices.trim(),
    enabled: form.enabled
  }

  try {
    if (dialogMode.value === 'create') {
      await createAdminCategory(payload)
      ElMessage.success('服务项目创建成功')
    } else {
      await updateAdminCategory(editingId.value, payload)
      ElMessage.success('服务项目更新成功')
    }
    dialogVisible.value = false
    await loadCategories()
  } catch (error) {
    ElMessage.error(error.message || '保存服务项目失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除服务项目“${row.name}”吗？`, '删除提示', {
      type: 'warning'
    })
    await deleteAdminCategory(row.id)
    ElMessage.success('服务项目已删除')
    await loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除服务项目失败')
    }
  }
}

onMounted(loadCategories)
</script>
