<template>
  <div class="permissions-page page-stack">
    <el-card shadow="never" class="page-panel">
      <div class="page-panel__header">
        <div>
          <h1 class="page-panel__title">权限配置</h1>
          <p class="page-panel__desc">按角色勾选后台可访问模块，保留清晰的中文名称与必要说明。</p>
        </div>
        <div class="filter-actions">
          <el-button :loading="loading" @click="loadCatalog">刷新目录</el-button>
          <el-button type="primary" :disabled="!canSaveCurrent" :loading="saving" @click="savePermissions">
            保存当前角色
          </el-button>
        </div>
      </div>

      <div class="metric-strip">
        <div class="metric-chip">
          <span class="metric-chip__label">角色数量</span>
          <strong>{{ roles.length }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">权限分组</span>
          <strong>{{ permissionGroups.length }}</strong>
        </div>
        <div class="metric-chip">
          <span class="metric-chip__label">当前已选</span>
          <strong>{{ selectedPermissions.length }}</strong>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16" class="permissions-grid">
      <el-col :xs="24" :lg="7">
        <el-card shadow="never" class="panel-card panel-card--sticky">
          <template #header>
            <div class="card-header-between">
              <strong>角色列表</strong>
              <span class="section-caption">点击切换</span>
            </div>
          </template>

          <div class="role-list">
            <button
              v-for="role in roles"
              :key="role.roleCode"
              type="button"
              class="role-item"
              :class="{ 'role-item--active': role.roleCode === activeRoleCode }"
              @click="handleRoleSelect(role.roleCode)"
            >
              <div class="role-item__title-row">
                <strong>{{ role.roleName }}</strong>
                <span class="role-item__count">{{ role.userCount }} 人</span>
              </div>
              <div class="role-item__meta">
                <span>{{ role.permissionCodes.length }} 项权限</span>
              </div>
            </button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="17">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header-between">
              <div>
                <strong>{{ selectedRole?.roleName || '请选择角色' }}</strong>
                <div class="section-caption">
                  <span v-if="selectedRole">{{ selectedRole.userCount }} 个账号正在使用该角色</span>
                  <span v-if="hasChanges" class="dirty-indicator">有未保存的修改</span>
                </div>
              </div>
              <el-space>
                <el-tag v-if="selectedRole" effect="plain">{{ selectedPermissions.length }} 项已选</el-tag>
                <el-button type="primary" :disabled="!canSaveCurrent" :loading="saving" @click="savePermissions">
                  保存
                </el-button>
              </el-space>
            </div>
          </template>

          <el-empty v-if="!selectedRole" description="请先选择一个角色" />

          <div v-else class="permission-group-list">
            <el-card
              v-for="group in permissionGroups"
              :key="group.groupCode"
              shadow="never"
              class="permission-group-card"
            >
              <template #header>
                <div class="group-header">
                  <div class="group-header__title-row">
                    <strong>{{ group.groupLabel }}</strong>
                    <span class="section-caption">{{ group.permissions.length }} 项</span>
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
                  :disabled="isRequiredPermission(permission.code)"
                  class="permission-card"
                >
                  <div class="permission-card__body">
                    <div class="permission-card__title-row">
                      <strong>{{ permission.label }}</strong>
                      <el-tag v-if="isRequiredPermission(permission.code)" size="small" effect="plain">必选</el-tag>
                    </div>
                    <div v-if="permission.description" class="permission-card__desc">{{ permission.description }}</div>
                  </div>
                </el-checkbox>
              </el-checkbox-group>
            </el-card>
          </div>
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

const selectedRole = computed(
  () => roles.value.find((role) => role.roleCode === activeRoleCode.value) || null
)

const canSave = computed(() => {
  if (!authStore.hasPermissionData()) {
    return true
  }
  return authStore.hasPermission(MANAGE_PERMISSION)
})

const canSaveCurrent = computed(() => Boolean(canSave.value && selectedRole.value && hasChanges.value))

const hasChanges = computed(() => {
  const current = normalizeCodes(selectedPermissions.value)
  const original = normalizeCodes(originalPermissions.value)
  if (current.length !== original.length) {
    return true
  }
  return current.some((code, index) => code !== original[index])
})

function normalizeCodes(value) {
  return Array.from(
    new Set(
      (Array.isArray(value) ? value : [])
        .map((item) => String(item || '').trim())
        .filter(Boolean)
    )
  ).sort()
}

function normalizeCatalog(raw = {}) {
  const rawRoles = Array.isArray(raw.roles) ? raw.roles : []
  const rawGroups = Array.isArray(raw.permissionGroups) ? raw.permissionGroups : []

  roles.value = rawRoles.map((role) => ({
    roleCode: String(role.roleCode || '').trim(),
    roleName: String(role.roleName || role.roleCode || '').trim(),
    userCount: Number(role.userCount || 0),
    permissionCodes: normalizeCodes(role.permissionCodes),
    requiredPermissionCodes: normalizeCodes(role.requiredPermissionCodes)
  }))

  permissionGroups.value = rawGroups.map((group, groupIndex) => ({
    groupCode: String(group.key || `group-${groupIndex}`),
    groupLabel: String(group.label || `分组 ${groupIndex + 1}`),
    permissions: (Array.isArray(group.permissions) ? group.permissions : []).map((permission) => ({
      code: String(permission.code || '').trim(),
      label: String(permission.name || permission.code || '').trim(),
      description: String(permission.description || '').trim()
    }))
  }))

  const preferredRoleCode =
    roles.value.find((role) => role.roleCode === authStore.state.user?.roleCode)?.roleCode ||
    roles.value.find((role) => role.roleCode === 'ADMIN')?.roleCode ||
    roles.value[0]?.roleCode ||
    ''

  if (preferredRoleCode) {
    applyRoleSelection(preferredRoleCode)
  }
}

function applyRoleSelection(roleCode) {
  const role = roles.value.find((item) => item.roleCode === roleCode)
  activeRoleCode.value = roleCode
  selectedPermissions.value = [...(role?.permissionCodes || [])]
  originalPermissions.value = [...(role?.permissionCodes || [])]
}

function isRequiredPermission(permissionCode) {
  return selectedRole.value?.requiredPermissionCodes?.includes(permissionCode) || false
}

function groupState(group) {
  const codes = group.permissions.map((permission) => permission.code)
  const selectedCount = codes.filter((code) => selectedPermissions.value.includes(code)).length
  return {
    allSelected: codes.length > 0 && selectedCount === codes.length,
    indeterminate: selectedCount > 0 && selectedCount < codes.length
  }
}

async function handleRoleSelect(roleCode) {
  if (!roleCode || roleCode === activeRoleCode.value) {
    return
  }

  if (hasChanges.value) {
    try {
      await ElMessageBox.confirm(
        '当前角色还有未保存的修改，切换后这些更改会丢失，是否继续？',
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

async function loadCatalog() {
  loading.value = true
  try {
    const result = await fetchAdminPermissionCatalog()
    normalizeCatalog(result || {})
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
    const savedCodes = normalizeCodes(result?.permissionCodes || payload.permissionCodes)
    selectedPermissions.value = [...savedCodes]
    originalPermissions.value = [...savedCodes]

    const currentRole = roles.value.find((item) => item.roleCode === selectedRole.value.roleCode)
    if (currentRole) {
      currentRole.permissionCodes = [...savedCodes]
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

.role-list,
.permission-group-list {
  display: grid;
  gap: 12px;
}

.role-item {
  border: 1px solid #e4e7ec;
  border-radius: 16px;
  background: #fff;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.role-item:hover,
.role-item--active {
  border-color: rgba(0, 113, 227, 0.36);
  box-shadow: 0 16px 32px rgba(16, 24, 40, 0.06);
  transform: translateY(-1px);
}

.role-item__title-row,
.group-header,
.permission-card__title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.role-item__count,
.role-item__meta,
.permission-card__desc {
  color: #667085;
  font-size: 12px;
  line-height: 1.7;
}

.role-item__meta {
  margin-top: 8px;
}

.dirty-indicator {
  margin-left: 8px;
  color: #005bb5;
  font-weight: 600;
}

.permission-group-card {
  border-radius: 18px;
}

.group-header__title-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.permission-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.permission-card {
  border: 1px solid #eaecf0;
  border-radius: 14px;
  background: #fcfcfd;
  padding: 14px;
  margin-right: 0;
  margin-bottom: 0;
}

.permission-card__body {
  display: grid;
  gap: 8px;
  width: 100%;
}

@media (max-width: 1200px) {
  .permission-grid {
    grid-template-columns: 1fr;
  }

  .panel-card--sticky {
    position: static;
  }
}
</style>
