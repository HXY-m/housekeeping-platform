<template>
  <div class="permissions-page page-stack">
    <el-card shadow="never" class="permissions-hero">
      <div class="page-panel__header permissions-hero__header">
        <div>
          <el-tag type="danger" round>权限管理</el-tag>
          <h1 class="page-panel__title">角色权限配置</h1>
          <p class="page-panel__desc">
            按角色查看可用权限分组，快速调整后台功能入口、页面访问与操作能力。
          </p>
        </div>
        <div class="filter-actions">
          <el-button :loading="loading" @click="loadCatalog">刷新目录</el-button>
          <el-button type="primary" :disabled="!canSave" :loading="saving" @click="savePermissions">
            保存权限
          </el-button>
        </div>
      </div>

      <div class="metric-strip">
        <div class="metric-chip">
          <span class="metric-chip__label">角色数量</span>
          <strong>{{ roles.length }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">分组数量</span>
          <strong>{{ permissionGroups.length }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">权限总数</span>
          <strong>{{ totalPermissionCount }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">已选权限</span>
          <strong>{{ selectedPermissions.length }}</strong>
        </div>
      </div>
    </el-card>

    <el-alert
      v-if="!permissionDataAvailable"
      title="当前登录用户未返回细粒度权限字段，页面将按角色管理模式继续可用。"
      type="info"
      show-icon
      :closable="false"
    />
    <el-alert
      v-else-if="!canSave"
      title="当前账号只有查看权限，无法保存角色权限变更。"
      type="warning"
      show-icon
      :closable="false"
    />

    <el-row :gutter="16" class="permissions-grid">
      <el-col :xs="24" :lg="7">
        <el-card shadow="never" class="panel-card panel-card--sticky">
          <template #header>
            <div class="card-header-between">
              <strong>角色列表</strong>
              <span class="section-caption">点击角色切换对应权限配置</span>
            </div>
          </template>

          <el-scrollbar max-height="640px">
            <div class="role-list">
              <button
                v-for="role in roles"
                :key="role.roleCode"
                class="role-item"
                :class="{ 'role-item--active': role.roleCode === activeRoleCode }"
                type="button"
                @click="handleRoleSelect(role.roleCode)"
              >
                <div class="role-item__main">
                  <div class="role-item__title-row">
                    <strong>{{ role.roleName }}</strong>
                    <el-tag size="small" effect="plain">{{ role.roleCode }}</el-tag>
                  </div>
                  <div class="role-item__meta">
                    <span>{{ role.permissionCodes.length }} 项已配置</span>
                    <span v-if="role.description">{{ role.description }}</span>
                  </div>
                </div>
              </button>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="17">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header-between">
              <div>
                <strong>{{ selectedRole?.roleName || '请选择角色' }}</strong>
                <div class="section-caption">
                  {{ selectedRole?.roleCode || '未选择角色' }}
                  <span v-if="hasChanges" class="dirty-indicator">有未保存修改</span>
                </div>
              </div>
              <div class="card-header-actions">
                <el-tag v-if="selectedRole" type="info" effect="plain">
                  {{ selectedRole.permissionCodes.length }} / {{ totalPermissionCount }}
                </el-tag>
                <el-button type="primary" :disabled="!canSave || !selectedRole || !hasChanges" :loading="saving" @click="savePermissions">
                  保存当前角色
                </el-button>
              </div>
            </div>
          </template>

          <el-empty v-if="!selectedRole" description="请先选择一个角色" />

          <template v-else>
            <div class="selected-role-summary">
              <div class="summary-card">
                <span class="summary-card__label">角色</span>
                <strong>{{ selectedRole.roleName }}</strong>
                <span class="summary-card__meta">{{ selectedRole.roleCode }}</span>
              </div>
              <div class="summary-card">
                <span class="summary-card__label">已勾选</span>
                <strong>{{ selectedPermissions.length }}</strong>
                <span class="summary-card__meta">较原始配置 {{ selectedDeltaText }}</span>
              </div>
              <div class="summary-card">
                <span class="summary-card__label">可用分组</span>
                <strong>{{ permissionGroups.length }}</strong>
                <span class="summary-card__meta">{{ permissionDensityText }}</span>
              </div>
            </div>

            <el-scrollbar max-height="640px">
              <div class="permission-group-list">
                <el-card
                  v-for="group in permissionGroups"
                  :key="group.groupCode"
                  shadow="never"
                  class="permission-group-card"
                >
                  <template #header>
                    <div class="group-header">
                      <div>
                        <div class="group-header__title-row">
                          <strong>{{ group.groupLabel }}</strong>
                          <el-tag size="small" round>{{ group.permissions.length }} 项</el-tag>
                        </div>
                        <p v-if="group.description" class="group-header__desc">
                          {{ group.description }}
                        </p>
                      </div>

                      <el-checkbox
                        :model-value="groupState(group).allSelected"
                        :indeterminate="groupState(group).indeterminate"
                        @change="toggleGroup(group, $event)"
                      >
                        全选
                      </el-checkbox>
                    </div>
                  </template>

                  <el-checkbox-group v-model="selectedPermissions" class="permission-grid">
                    <el-checkbox
                      v-for="permission in group.permissions"
                      :key="permission.code"
                      :label="permission.code"
                      class="permission-card"
                      :disabled="isRequiredPermission(permission.code)"
                    >
                      <div class="permission-card__body">
                        <div class="permission-card__title-row">
                          <strong>{{ permission.label }}</strong>
                          <el-tag size="small" effect="plain">{{ permission.code }}</el-tag>
                        </div>
                        <div v-if="permission.description" class="permission-card__desc">
                          {{ permission.description }}
                        </div>
                      </div>
                    </el-checkbox>
                  </el-checkbox-group>
                </el-card>
              </div>
            </el-scrollbar>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAdminPermissionCatalog, updateAdminRolePermissions } from '../../api'
import { authStore } from '../../stores/auth'

const MANAGE_PERMISSION = 'ADMIN_PERMISSION_MANAGE'

const loading = ref(false)
const saving = ref(false)
const roles = ref([])
const permissionGroups = ref([])
const activeRoleCode = ref('')
const selectedPermissions = ref([])
const originalPermissions = ref([])

const permissionDataAvailable = computed(() => authStore.hasPermissionData())
const canSave = computed(
  () => !permissionDataAvailable.value || authStore.hasPermission(MANAGE_PERMISSION)
)

const selectedRole = computed(() =>
  roles.value.find((role) => role.roleCode === activeRoleCode.value) || null
)

const totalPermissionCount = computed(() =>
  permissionGroups.value.reduce((sum, group) => sum + group.permissions.length, 0)
)

const hasChanges = computed(() => {
  const current = normalizeCodes(selectedPermissions.value)
  const original = normalizeCodes(originalPermissions.value)
  if (current.length !== original.length) {
    return true
  }
  return current.some((code, index) => code !== original[index])
})

const selectedDeltaText = computed(() => {
  const delta = selectedPermissions.value.length - originalPermissions.value.length
  if (delta === 0) return '保持不变'
  return delta > 0 ? `新增 ${delta} 项` : `减少 ${Math.abs(delta)} 项`
})

const permissionDensityText = computed(() => {
  if (!totalPermissionCount.value) {
    return '暂无权限目录'
  }
  return `${Math.round((selectedPermissions.value.length / totalPermissionCount.value) * 100)}% 覆盖`
})

function asArray(value) {
  if (Array.isArray(value)) return value
  if (value === null || value === undefined || value === '') return []
  return [value]
}

function normalizeCodes(value) {
  return Array.from(
    new Set(
      asArray(value)
        .map((item) => {
          if (typeof item === 'string') return item.trim()
          if (item && typeof item === 'object') {
            return String(
              item.permissionCode ||
                item.code ||
                item.key ||
                item.value ||
                item.id ||
                item.name ||
                ''
            ).trim()
          }
          return ''
        })
        .filter(Boolean)
    )
  ).sort()
}

function extractPermissionList(value) {
  return asArray(value)
    .map((item) => {
      if (!item) return null
      if (typeof item === 'string') {
        return {
          code: item.trim(),
          label: item.trim(),
          description: '',
          groupCode: '',
          groupLabel: ''
        }
      }

      const code = String(
        item.permissionCode || item.code || item.key || item.value || item.id || ''
      ).trim()
      if (!code) return null

      return {
        code,
        label: String(item.permissionName || item.name || item.label || code).trim(),
        description: String(item.description || item.desc || item.remark || item.tip || '').trim(),
        groupCode: String(item.groupCode || item.categoryCode || item.moduleCode || '').trim(),
        groupLabel: String(
          item.groupName || item.categoryName || item.moduleName || item.groupLabel || ''
        ).trim()
      }
    })
    .filter(Boolean)
}

function normalizeRoles(raw) {
  return asArray(raw)
    .map((item, index) => {
      if (!item) return null
      if (typeof item === 'string') {
        return {
          roleCode: item.trim(),
          roleName: item.trim(),
          description: '',
          permissionCodes: []
        }
      }

      const roleCode = String(item.roleCode || item.code || item.value || item.id || '').trim()
      if (!roleCode) return null

      return {
        roleCode,
        roleName: String(item.roleName || item.name || item.label || roleCode).trim(),
        description: String(item.description || item.desc || item.remark || '').trim(),
        permissionCodes: normalizeCodes(
          item.permissionCodes ||
            item.permissions ||
            item.assignedPermissionCodes ||
            item.grantedPermissionCodes ||
            item.permissionList
        ),
        sortOrder: Number(item.sortOrder ?? index)
      }
    })
    .filter(Boolean)
    .sort((left, right) => (left.sortOrder ?? 0) - (right.sortOrder ?? 0))
}

function normalizePermissionGroups(raw) {
  const groupsSource =
    raw?.permissionGroups || raw?.groups || raw?.catalog || raw?.sections || raw?.modules || []

  if (Array.isArray(groupsSource) && groupsSource.length) {
    return groupsSource
      .map((group, index) => {
        if (!group) return null
        const groupCode = String(group.groupCode || group.code || group.key || group.id || index).trim()
        const groupLabel = String(group.groupName || group.name || group.label || groupCode).trim()
        const description = String(group.description || group.desc || group.remark || '').trim()
        const permissions = extractPermissionList(
          group.permissions || group.children || group.items || group.list || []
        )

        return {
          groupCode,
          groupLabel,
          description,
          permissions
        }
      })
      .filter(Boolean)
      .filter((group) => group.permissions.length)
  }

  const flatPermissions = extractPermissionList(raw?.permissions || raw?.permissionList || raw?.items || raw)
  if (!flatPermissions.length) {
    return []
  }

  const bucketMap = new Map()
  flatPermissions.forEach((permission, index) => {
    const bucketKey =
      permission.groupCode ||
      permission.groupLabel ||
      permission.description ||
      'default'

    if (!bucketMap.has(bucketKey)) {
      bucketMap.set(bucketKey, {
        groupCode: bucketKey === 'default' ? `group-${index}` : bucketKey,
        groupLabel:
          permission.groupLabel ||
          permission.groupCode ||
          permission.description ||
          '未分组权限',
        description: '',
        permissions: []
      })
    }

    bucketMap.get(bucketKey).permissions.push(permission)
  })

  return Array.from(bucketMap.values())
}

function groupState(group) {
  const codes = group.permissions.map((permission) => permission.code)
  const selectedCount = codes.filter((code) => selectedPermissions.value.includes(code)).length
  return {
    total: codes.length,
    selectedCount,
    allSelected: codes.length > 0 && selectedCount === codes.length,
    indeterminate: selectedCount > 0 && selectedCount < codes.length
  }
}

function normalizeCatalog(raw) {
  const normalizedRoles = normalizeRoles(raw?.roles || raw?.roleList || raw?.items || raw)
  const normalizedGroups = normalizePermissionGroups(raw)

  if (normalizedRoles.length) {
    roles.value = normalizedRoles
  }
  permissionGroups.value = normalizedGroups

  const preferredRoleCode =
    normalizedRoles.find((role) => role.roleCode === authStore.state.user?.roleCode)?.roleCode ||
    normalizedRoles.find((role) => role.roleCode === 'ADMIN')?.roleCode ||
    normalizedRoles[0]?.roleCode ||
    ''

  if (preferredRoleCode) {
    applyRoleSelection(preferredRoleCode, false)
  } else {
    activeRoleCode.value = ''
    selectedPermissions.value = []
    originalPermissions.value = []
  }
}

function applyRoleSelection(roleCode, syncOriginal = true) {
  const role = roles.value.find((item) => item.roleCode === roleCode)
  activeRoleCode.value = roleCode
  selectedPermissions.value = [...(role?.permissionCodes || [])]
  if (syncOriginal) {
    originalPermissions.value = [...(role?.permissionCodes || [])]
  }
}

async function handleRoleSelect(roleCode) {
  if (!roleCode || roleCode === activeRoleCode.value) {
    return
  }

  if (hasChanges.value) {
    try {
      await ElMessageBox.confirm(
        '当前角色还有未保存的权限调整，切换角色后这些修改会丢失。是否继续？',
        '切换角色',
        {
          type: 'warning',
          confirmButtonText: '继续切换',
          cancelButtonText: '留在当前角色'
        }
      )
    } catch {
      return
    }
  }

  applyRoleSelection(roleCode)
}

function toggleGroup(group, checked) {
  const next = new Set(selectedPermissions.value)
  group.permissions.forEach((permission) => {
    if (!checked && isRequiredPermission(permission.code)) {
      return
    }
    if (checked) {
      next.add(permission.code)
    } else {
      next.delete(permission.code)
    }
  })
  selectedPermissions.value = Array.from(next)
}

function isRequiredPermission(permissionCode) {
  return Boolean(selectedRole.value?.requiredPermissionCodes?.includes(permissionCode))
}

async function loadCatalog() {
  loading.value = true
  try {
    const raw = await fetchAdminPermissionCatalog()
    normalizeCatalog(raw || {})
  } catch (error) {
    ElMessage.error(error.message || '获取权限目录失败')
  } finally {
    loading.value = false
  }
}

async function savePermissions() {
  if (!selectedRole.value || !canSave.value) {
    return
  }

  saving.value = true
  try {
    const payload = {
      permissionCodes: normalizeCodes(selectedPermissions.value)
    }
    const result = await updateAdminRolePermissions(selectedRole.value.roleCode, payload)
    const savedCodes = normalizeCodes(
      result?.permissionCodes ||
        result?.permissions ||
        result?.assignedPermissionCodes ||
        payload.permissionCodes
    )

    if (savedCodes.length) {
      selectedPermissions.value = [...savedCodes]
      originalPermissions.value = [...savedCodes]
      const role = roles.value.find((item) => item.roleCode === selectedRole.value.roleCode)
      if (role) {
        role.permissionCodes = [...savedCodes]
      }
    } else {
      originalPermissions.value = normalizeCodes(selectedPermissions.value)
    }

    ElMessage.success('角色权限已保存')
    await loadCatalog()
  } catch (error) {
    ElMessage.error(error.message || '保存角色权限失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadCatalog)
</script>

<style scoped>
.permissions-page {
  gap: 16px;
}

.permissions-hero {
  background:
    radial-gradient(circle at top right, rgba(248, 113, 113, 0.16), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 1), rgba(255, 247, 247, 0.85));
}

.permissions-hero__header {
  align-items: flex-start;
}

.permissions-grid {
  align-items: stretch;
}

.panel-card {
  height: 100%;
}

.panel-card--sticky {
  position: sticky;
  top: 16px;
}

.role-list {
  display: grid;
  gap: 12px;
}

.role-item {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: #fff;
  padding: 14px 16px;
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.role-item:hover,
.role-item--active {
  border-color: #ef4444;
  box-shadow: 0 12px 28px rgba(239, 68, 68, 0.12);
  transform: translateY(-1px);
}

.role-item__title-row,
.permission-card__title-row,
.group-header,
.selected-role-summary,
.card-header-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.role-item__meta,
.group-header__desc,
.permission-card__desc,
.section-caption,
.summary-card__meta {
  color: #6b7280;
  font-size: 12px;
  line-height: 1.6;
}

.role-item__meta {
  display: grid;
  gap: 4px;
  margin-top: 10px;
}

.dirty-indicator {
  margin-left: 8px;
  color: #dc2626;
  font-weight: 600;
}

.selected-role-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-card {
  border: 1px solid #f1f5f9;
  border-radius: 16px;
  background: linear-gradient(180deg, #fff, #fdf2f2);
  padding: 14px 16px;
  display: grid;
  gap: 4px;
}

.summary-card__label {
  color: #9ca3af;
  font-size: 12px;
}

.permission-group-list {
  display: grid;
  gap: 16px;
}

.permission-group-card {
  border-radius: 18px;
}

.group-header {
  align-items: flex-start;
}

.group-header__title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.group-header__desc {
  margin: 6px 0 0;
}

.permission-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 14px;
}

.permission-card {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  background: #fff;
  padding: 14px;
  align-items: flex-start;
  margin-right: 0;
  margin-bottom: 0;
}

.permission-card__body {
  display: grid;
  gap: 6px;
  width: 100%;
}

@media (max-width: 1200px) {
  .selected-role-summary,
  .permission-grid {
    grid-template-columns: 1fr;
  }

  .panel-card--sticky {
    position: static;
  }
}
</style>
