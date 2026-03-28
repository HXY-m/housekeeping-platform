<template>
  <div class="page-stack">
    <div class="console-overview console-overview--admin">
      <div>
        <el-tag type="success" round>服务管理</el-tag>
        <h1>服务项目</h1>
        <p>维护前台展示的服务项目、价格标签和图片。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="openCreate">新增项目</el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="项目总数" :value="summary.total" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="启用项目" :value="summary.enabled" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="停用项目" :value="summary.disabled" />
      </el-card>
      <el-card shadow="never" class="summary-card">
        <el-statistic title="资料完整" :value="summary.richInfo" />
      </el-card>
    </div>

    <el-card shadow="never">
      <div class="table-toolbar">
        <div class="table-toolbar__filters">
          <el-input
            v-model="filters.keyword"
            clearable
            placeholder="搜索项目名称或描述"
            style="width: 280px"
          />
          <el-select v-model="filters.enabled" clearable placeholder="启用状态" style="width: 180px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
          <el-button type="primary" @click="loadCategories">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
        <span class="section-caption">统计数据来自后端汇总接口</span>
      </div>

      <el-table :data="categories" v-loading="loading" stripe style="margin-top: 16px">
        <el-table-column label="图片" width="110">
          <template #default="{ row }">
            <el-image
              :src="getServiceImage(row)"
              :preview-src-list="[getServiceImage(row)]"
              fit="cover"
              preview-teleported
              class="table-cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="项目名称" min-width="150" />
        <el-table-column prop="priceLabel" label="价格标签" width="140" />
        <el-table-column prop="serviceDuration" label="服务时长" width="140" />
        <el-table-column prop="serviceArea" label="服务范围" min-width="180" show-overflow-tooltip />
        <el-table-column prop="serviceScene" label="适用场景" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增服务项目' : '编辑服务项目'" width="760px">
      <el-form label-position="top">
        <div class="media-upload-panel">
          <div class="media-upload-panel__preview">
            <el-image :src="previewImage" fit="cover" class="media-upload-panel__image" preview-teleported />
          </div>
          <div class="media-upload-panel__meta">
            <strong>展示图片</strong>
            <span class="muted-line">未上传时前台会使用系统默认图。</span>
            <div class="hero-actions">
              <el-button plain :loading="uploadingImage" @click="openImagePicker">上传图片</el-button>
              <el-button text @click="clearImage">恢复默认图</el-button>
            </div>
          </div>
        </div>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目名称">
              <el-input v-model="form.name" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格标签">
              <el-input v-model="form.priceLabel" maxlength="50" placeholder="例如：128 元起 / 2 小时起" />
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
              <el-input v-model="form.serviceDuration" maxlength="100" placeholder="例如：2 小时 / 4 小时" />
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

      <input
        ref="imageInputRef"
        type="file"
        accept="image/*"
        class="visually-hidden-input"
        @change="handleImageChange"
      />

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
  createAdminCategory,
  deleteAdminCategory,
  fetchAdminCategories,
  fetchAdminCategorySummary,
  updateAdminCategory,
  uploadImage
} from '../../api'
import ListPagination from '../../components/common/ListPagination.vue'
import { useServerPagination } from '../../composables/useServerPagination'
import { getServiceImage } from '../../utils/displayAssets'

const loading = ref(false)
const uploadingImage = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const imageInputRef = ref(null)
const summary = ref({
  total: 0,
  enabled: 0,
  disabled: 0,
  richInfo: 0
})

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
  imageUrl: '',
  enabled: true
})

const { currentPage, pageSize, pageSizes, total, buildParams, applyPageResult, resetPage } = useServerPagination(10)

const previewImage = computed(() =>
  getServiceImage({
    ...form,
    imageUrl: form.imageUrl
  })
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
  form.imageUrl = ''
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
  form.imageUrl = row.imageUrl || ''
  form.enabled = row.enabled
  dialogVisible.value = true
}

function resetFilters() {
  filters.keyword = ''
  filters.enabled = null
  if (currentPage.value !== 1) {
    resetPage()
    return
  }
  loadCategories()
}

function openImagePicker() {
  imageInputRef.value?.click()
}

function clearImage() {
  form.imageUrl = ''
  if (imageInputRef.value) {
    imageInputRef.value.value = ''
  }
}

async function handleImageChange(event) {
  const file = event.target.files?.[0]
  if (!file) {
    return
  }
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片格式的展示图')
    event.target.value = ''
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('展示图不能超过 5MB')
    event.target.value = ''
    return
  }

  uploadingImage.value = true
  try {
    const uploaded = await uploadImage(file)
    form.imageUrl = uploaded.url
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error(error.message || '上传图片失败')
  } finally {
    uploadingImage.value = false
    event.target.value = ''
  }
}

async function loadCategories() {
  loading.value = true
  try {
    const params = {
      keyword: filters.keyword.trim(),
      enabled: filters.enabled
    }
    const [result, summaryResult] = await Promise.all([
      fetchAdminCategories(buildParams(params)),
      fetchAdminCategorySummary(params)
    ])
    categories.value = applyPageResult(result)
    summary.value = {
      total: Number(summaryResult?.total || 0),
      enabled: Number(summaryResult?.enabled || 0),
      disabled: Number(summaryResult?.disabled || 0),
      richInfo: Number(summaryResult?.richInfo || 0)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取服务项目失败')
  } finally {
    loading.value = false
  }
}

async function submitForm() {
  if (!form.name.trim() || !form.description.trim() || !form.priceLabel.trim()) {
    ElMessage.warning('请先完善项目名称、描述和价格标签')
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
    imageUrl: form.imageUrl.trim(),
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

watch([currentPage, pageSize], () => {
  loadCategories()
})

onMounted(loadCategories)
</script>

<style scoped>
.table-cover {
  width: 68px;
  height: 52px;
  border-radius: 12px;
  overflow: hidden;
}

.media-upload-panel {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  gap: 18px;
  padding: 16px;
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid #e4e7ec;
  margin-bottom: 18px;
}

.media-upload-panel__image {
  width: 100%;
  height: 126px;
  border-radius: 14px;
  overflow: hidden;
}

.media-upload-panel__meta {
  display: grid;
  align-content: center;
  gap: 10px;
}

@media (max-width: 768px) {
  .media-upload-panel {
    grid-template-columns: 1fr;
  }
}
</style>
