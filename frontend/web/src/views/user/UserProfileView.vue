<template>
  <div class="page-stack">
    <div class="console-overview">
      <div>
        <el-tag type="success" round>资料中心</el-tag>
        <h1>个人资料与地址簿</h1>
        <p>维护联系人、头像、城市信息和常用上门地址，后续下单可直接复用默认地址。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="saveProfile" :loading="savingProfile">保存资料</el-button>
      </div>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :xl="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-between">
              <strong>基本资料</strong>
              <span class="muted-line">同步展示在用户中心和预约流程中</span>
            </div>
          </template>

          <div class="profile-avatar-panel">
            <el-avatar :size="84" :src="profileForm.avatarUrl || undefined">
              {{ profileForm.realName?.slice(0, 1) || 'U' }}
            </el-avatar>
            <div class="page-stack">
              <el-button plain :loading="uploadingAvatar" @click="openAvatarPicker">上传头像</el-button>
              <span class="muted-line">支持 jpg/png/webp，建议 1:1 比例。</span>
            </div>
          </div>

          <el-form label-position="top" class="profile-form">
            <el-form-item label="姓名">
              <el-input v-model="profileForm.realName" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input :model-value="profileForm.phone" disabled />
            </el-form-item>
            <el-row :gutter="16">
              <el-col :xs="24" :md="12">
                <el-form-item label="性别">
                  <el-select v-model="profileForm.gender" style="width: 100%">
                    <el-option label="未设置" value="" />
                    <el-option label="女士" value="女士" />
                    <el-option label="男士" value="男士" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="所在城市">
                  <el-input v-model="profileForm.city" placeholder="例如：上海市" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="个人简介">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="5"
                maxlength="500"
                show-word-limit
                placeholder="例如：对家庭保洁、钟点服务、母婴护理有哪些偏好与关注点。"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-between">
              <strong>常用地址簿</strong>
              <el-button type="primary" plain @click="openAddressDialog()">新增地址</el-button>
            </div>
          </template>

          <el-table :data="addresses" stripe>
            <el-table-column label="联系人" min-width="120">
              <template #default="{ row }">
                <div class="stack-cell">
                  <strong>{{ row.contactName }}</strong>
                  <span class="muted-line">{{ row.contactPhone }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="地址信息" min-width="260">
              <template #default="{ row }">
                <div class="stack-cell">
                  <span>{{ row.city }} {{ row.detailAddress }}</span>
                  <div class="tag-group">
                    <el-tag size="small" effect="plain">{{ row.addressTag || '常用地址' }}</el-tag>
                    <el-tag v-if="row.defaultAddress" size="small" type="success">默认地址</el-tag>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-space>
                  <el-button size="small" @click="openAddressDialog(row)">编辑</el-button>
                  <el-button size="small" type="danger" plain @click="confirmDeleteAddress(row)">删除</el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <input
      ref="avatarInputRef"
      type="file"
      accept="image/*"
      class="visually-hidden-input"
      @change="handleAvatarChange"
    />

    <el-dialog
      v-model="addressDialogVisible"
      :title="addressDialogMode === 'create' ? '新增常用地址' : '编辑常用地址'"
      width="560px"
    >
      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="联系人">
              <el-input v-model="addressForm.contactName" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="联系电话">
              <el-input v-model="addressForm.contactPhone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="所在城市">
              <el-input v-model="addressForm.city" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="地址标签">
              <el-input v-model="addressForm.addressTag" placeholder="例如：家庭 / 父母家 / 公司" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址">
          <el-input v-model="addressForm.detailAddress" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item>
          <el-switch v-model="addressForm.defaultAddress" active-text="设为默认地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingAddress" @click="submitAddress">
          保存地址
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createUserAddress,
  deleteUserAddress,
  fetchUserAddresses,
  fetchUserProfile,
  updateUserAddress,
  updateUserProfile,
  uploadImage
} from '../../api'
import { authStore } from '../../stores/auth'

const loading = ref(false)
const savingProfile = ref(false)
const savingAddress = ref(false)
const uploadingAvatar = ref(false)
const avatarInputRef = ref(null)

const profileForm = reactive({
  realName: '',
  phone: '',
  gender: '',
  city: '',
  bio: '',
  avatarUrl: ''
})

const addresses = ref([])
const addressDialogVisible = ref(false)
const addressDialogMode = ref('create')
const editingAddressId = ref(null)
const addressForm = reactive({
  contactName: '',
  contactPhone: '',
  city: '',
  detailAddress: '',
  addressTag: '',
  defaultAddress: false
})

async function loadData() {
  loading.value = true
  try {
    const [profile, addressRows] = await Promise.all([
      fetchUserProfile(),
      fetchUserAddresses()
    ])
    Object.assign(profileForm, profile)
    addresses.value = addressRows
  } catch (error) {
    ElMessage.error(error.message || '获取用户资料失败')
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  if (!profileForm.realName.trim()) {
    ElMessage.warning('请先填写姓名')
    return
  }

  savingProfile.value = true
  try {
    await updateUserProfile({
      realName: profileForm.realName.trim(),
      gender: profileForm.gender,
      city: profileForm.city.trim(),
      bio: profileForm.bio.trim(),
      avatarUrl: profileForm.avatarUrl
    })
    await authStore.refresh()
    ElMessage.success('个人资料已保存')
  } catch (error) {
    ElMessage.error(error.message || '保存资料失败')
  } finally {
    savingProfile.value = false
  }
}

function openAvatarPicker() {
  avatarInputRef.value?.click()
}

async function handleAvatarChange(event) {
  const file = event.target.files?.[0]
  if (!file) {
    return
  }

  uploadingAvatar.value = true
  try {
    const result = await uploadImage(file)
    profileForm.avatarUrl = result.url
    ElMessage.success('头像上传成功')
  } catch (error) {
    ElMessage.error(error.message || '头像上传失败')
  } finally {
    uploadingAvatar.value = false
    event.target.value = ''
  }
}

function openAddressDialog(address = null) {
  if (address) {
    addressDialogMode.value = 'edit'
    editingAddressId.value = address.id
    Object.assign(addressForm, address)
    addressDialogVisible.value = true
    return
  }

  addressDialogMode.value = 'create'
  editingAddressId.value = null
  Object.assign(addressForm, {
    contactName: profileForm.realName || '',
    contactPhone: profileForm.phone || '',
    city: profileForm.city || '',
    detailAddress: '',
    addressTag: '家庭',
    defaultAddress: !addresses.value.length
  })
  addressDialogVisible.value = true
}

async function submitAddress() {
  if (!addressForm.contactName.trim() || !addressForm.contactPhone.trim() || !addressForm.city.trim() || !addressForm.detailAddress.trim()) {
    ElMessage.warning('请完整填写地址信息')
    return
  }

  savingAddress.value = true
  try {
    const payload = {
      contactName: addressForm.contactName.trim(),
      contactPhone: addressForm.contactPhone.trim(),
      city: addressForm.city.trim(),
      detailAddress: addressForm.detailAddress.trim(),
      addressTag: addressForm.addressTag.trim(),
      defaultAddress: addressForm.defaultAddress
    }

    if (addressDialogMode.value === 'create') {
      await createUserAddress(payload)
    } else {
      await updateUserAddress(editingAddressId.value, payload)
    }

    addressDialogVisible.value = false
    ElMessage.success('地址簿已更新')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存地址失败')
  } finally {
    savingAddress.value = false
  }
}

async function confirmDeleteAddress(address) {
  try {
    await ElMessageBox.confirm(
      `确认删除地址“${address.city} ${address.detailAddress}”吗？`,
      '删除地址',
      { type: 'warning' }
    )
    await deleteUserAddress(address.id)
    ElMessage.success('地址已删除')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除地址失败')
    }
  }
}

onMounted(loadData)
</script>
