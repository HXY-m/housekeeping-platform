<template>
  <div class="page-stack">
    <div class="console-overview">
      <div>
        <el-tag type="success" round>资料中心</el-tag>
        <h1>个人资料与地址簿</h1>
        <p>维护联系人、头像和常用地址，下单时会优先带出默认地址。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</el-button>
      </div>
    </div>

    <div class="metric-strip">
      <div class="metric-chip">
        <span class="metric-chip__label">资料完整度</span>
        <strong>{{ profileCompletion }}%</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">常用地址</span>
        <strong>{{ addresses.length }}</strong>
      </div>
      <div class="metric-chip">
        <span class="metric-chip__label">默认地址</span>
        <strong>{{ defaultAddressLabel }}</strong>
      </div>
    </div>

    <el-row :gutter="18" v-loading="loading">
      <el-col :xs="24" :xl="10">
        <el-card shadow="never" class="page-panel profile-card">
          <div class="card-header-between">
            <div>
              <strong>基本资料</strong>
              <p class="section-caption">建议填写完整，预约时联系人信息会自动带入。</p>
            </div>
          </div>

          <div class="profile-identity">
            <el-avatar :size="92" :src="profileForm.avatarUrl || undefined" class="profile-identity__avatar">
              {{ profileForm.realName?.slice(0, 1) || '安' }}
            </el-avatar>
            <div class="profile-identity__content">
              <div class="profile-identity__title">
                <strong>{{ profileForm.realName || '未设置姓名' }}</strong>
                <span>{{ profileForm.phone || '未绑定手机号' }}</span>
              </div>
              <div class="tag-group">
                <el-tag effect="plain" type="success">{{ profileForm.city || '未选择城市' }}</el-tag>
                <el-tag effect="plain">{{ profileForm.gender || '未设置性别' }}</el-tag>
              </div>
              <div class="hero-actions">
                <el-button plain :loading="uploadingAvatar" @click="openAvatarPicker">上传头像</el-button>
              </div>
            </div>
          </div>

          <el-form label-position="top" class="profile-form">
            <el-form-item label="姓名">
              <el-input v-model="profileForm.realName" maxlength="50" placeholder="请输入联系人姓名" />
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
                  <el-select
                    v-model="profileForm.city"
                    filterable
                    clearable
                    placeholder="请选择所在城市"
                    style="width: 100%"
                  >
                    <el-option v-for="city in CITY_OPTIONS" :key="city" :label="city" :value="city" />
                  </el-select>
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
                placeholder="简单说明你的家庭服务偏好、时间偏好或特殊提醒。"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="14">
        <el-card shadow="never" class="page-panel">
          <div class="card-header-between">
            <div>
              <strong>常用地址簿</strong>
              <p class="section-caption">默认地址会优先出现在预约表单中。</p>
            </div>
            <el-button type="primary" plain @click="openAddressDialog()">新增地址</el-button>
          </div>

          <div v-if="addresses.length" class="address-list">
            <div v-for="address in addresses" :key="address.id" class="address-card">
              <div class="address-card__main">
                <div class="address-card__title-row">
                  <strong>{{ address.contactName }}</strong>
                  <div class="tag-group">
                    <el-tag size="small" effect="plain">{{ address.addressTag || '常用地址' }}</el-tag>
                    <el-tag v-if="address.defaultAddress" size="small" type="success">默认</el-tag>
                  </div>
                </div>
                <div class="stack-cell">
                  <span>{{ address.contactPhone }}</span>
                  <span class="muted-line">{{ address.city }} {{ address.detailAddress }}</span>
                </div>
              </div>
              <div class="hero-actions">
                <el-button size="small" @click="openAddressDialog(address)">编辑</el-button>
                <el-button size="small" type="danger" plain @click="confirmDeleteAddress(address)">删除</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="还没有常用地址，新建一个后下单会更快。" class="empty-surface" />
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
              <el-input v-model="addressForm.contactName" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="联系电话">
              <el-input v-model="addressForm.contactPhone" maxlength="20" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="所在城市">
              <el-select
                v-model="addressForm.city"
                filterable
                clearable
                placeholder="请选择城市"
                style="width: 100%"
              >
                <el-option v-for="city in CITY_OPTIONS" :key="city" :label="city" :value="city" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="地址标签">
              <el-input v-model="addressForm.addressTag" maxlength="20" placeholder="例如：家 / 公司 / 父母家" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址">
          <el-input v-model="addressForm.detailAddress" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-switch v-model="addressForm.defaultAddress" active-text="设为默认地址" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingAddress" @click="submitAddress">保存地址</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
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
import { CITY_OPTIONS } from '../../constants/cities'

const loading = ref(false)
const savingProfile = ref(false)
const savingAddress = ref(false)
const uploadingAvatar = ref(false)
const avatarInputRef = ref(null)
const addresses = ref([])
const addressDialogVisible = ref(false)
const addressDialogMode = ref('create')
const editingAddressId = ref(null)

const profileForm = reactive({
  realName: '',
  phone: '',
  gender: '',
  city: '',
  bio: '',
  avatarUrl: ''
})

const addressForm = reactive({
  contactName: '',
  contactPhone: '',
  city: '',
  detailAddress: '',
  addressTag: '',
  defaultAddress: false
})

const profileCompletion = computed(() => {
  const fields = [
    profileForm.realName,
    profileForm.phone,
    profileForm.gender,
    profileForm.city,
    profileForm.bio,
    profileForm.avatarUrl
  ]
  const completed = fields.filter((value) => String(value || '').trim()).length
  return Math.round((completed / fields.length) * 100)
})

const defaultAddressLabel = computed(() => {
  const defaultAddress = addresses.value.find((item) => item.defaultAddress)
  return defaultAddress ? '已设置' : '未设置'
})

async function loadData() {
  loading.value = true
  try {
    const [profile, addressRows] = await Promise.all([fetchUserProfile(), fetchUserAddresses()])
    Object.assign(profileForm, {
      realName: profile?.realName || '',
      phone: profile?.phone || '',
      gender: profile?.gender || '',
      city: profile?.city || '',
      bio: profile?.bio || '',
      avatarUrl: profile?.avatarUrl || ''
    })
    addresses.value = Array.isArray(addressRows) ? addressRows : []
  } catch (error) {
    ElMessage.error(error.message || '获取资料失败')
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
    profileForm.avatarUrl = result?.url || ''
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
    Object.assign(addressForm, {
      contactName: address.contactName || '',
      contactPhone: address.contactPhone || '',
      city: address.city || '',
      detailAddress: address.detailAddress || '',
      addressTag: address.addressTag || '',
      defaultAddress: Boolean(address.defaultAddress)
    })
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
  if (
    !addressForm.contactName.trim() ||
    !addressForm.contactPhone.trim() ||
    !addressForm.city.trim() ||
    !addressForm.detailAddress.trim()
  ) {
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
