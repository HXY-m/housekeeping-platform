<template>
  <el-dialog
    v-model="visible"
    title="地图选点"
    width="980px"
    top="6vh"
    class="address-map-dialog"
  >
    <div class="map-picker">
      <div class="map-picker__toolbar">
        <el-select
          v-model="searchCity"
          filterable
          clearable
          allow-create
          default-first-option
          placeholder="选择城市"
          style="width: 180px"
        >
          <el-option v-for="city in CITY_OPTIONS" :key="city" :label="city" :value="city" />
        </el-select>
        <el-input
          v-model.trim="searchKeyword"
          placeholder="搜索小区、街道、写字楼或地标"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-button :loading="searching" type="primary" @click="handleSearch">搜索地址</el-button>
        <el-button plain :loading="locatingCurrent" @click="locateCurrentPosition">我的位置</el-button>
      </div>

      <div class="map-picker__layout">
        <div class="map-picker__map-card" v-loading="loadingMap || reverseGeocoding">
          <div ref="mapContainerRef" class="map-picker__map"></div>
        </div>

        <div class="map-picker__side">
          <div class="info-panel">
            <span class="info-panel__label">使用方式</span>
            <strong>点击地图或搜索地址</strong>
            <span class="muted-line">点击地图即可反查地址，搜索结果也可以直接定位并选择。</span>
          </div>

          <div class="info-panel">
            <span class="info-panel__label">当前已选地址</span>
            <strong>{{ selectedLocation?.displayAddress || '还未选择具体地址' }}</strong>
            <span class="muted-line">
              {{ selectedLocation ? `经纬度：${selectedLocation.latitude.toFixed(6)}, ${selectedLocation.longitude.toFixed(6)}` : '可在地图上点击设置具体位置' }}
            </span>
          </div>

          <div class="map-picker__results">
            <div class="card-header-between">
              <strong>搜索结果</strong>
              <span class="section-caption">{{ searchResults.length ? `共 ${searchResults.length} 条` : '暂无结果' }}</span>
            </div>

            <div v-if="searchResults.length" class="map-picker__result-list">
              <button
                v-for="item in searchResults"
                :key="item.key"
                type="button"
                class="map-picker__result-item"
                @click="focusSearchResult(item)"
              >
                <strong>{{ item.displayAddress }}</strong>
                <span class="muted-line">{{ item.city }}</span>
              </button>
            </div>
            <el-empty v-else description="搜索后可在这里快速选择地址" class="empty-surface" />
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="confirmSelection">使用该地址</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import 'leaflet/dist/leaflet.css'
import { CITY_OPTIONS, CITY_CENTER_MAP, DEFAULT_CITY_CENTER } from '../../constants/cities'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  city: {
    type: String,
    default: ''
  },
  detailAddress: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'select'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const mapContainerRef = ref(null)
const searchCity = ref(props.city || CITY_OPTIONS[0])
const searchKeyword = ref(props.detailAddress || '')
const searchResults = ref([])
const selectedLocation = ref(null)
const loadingMap = ref(false)
const searching = ref(false)
const reverseGeocoding = ref(false)
const locatingCurrent = ref(false)

let leafletInstance = null
let map = null
let marker = null

function getCityCenter(city) {
  return CITY_CENTER_MAP[city] || DEFAULT_CITY_CENTER
}

async function ensureLeaflet() {
  if (leafletInstance) {
    return leafletInstance
  }
  const module = await import('leaflet')
  leafletInstance = module.default || module
  return leafletInstance
}

async function ensureMap() {
  if (map || !mapContainerRef.value) {
    return
  }

  const L = await ensureLeaflet()
  map = L.map(mapContainerRef.value, {
    zoomControl: true,
    attributionControl: true
  }).setView(getCityCenter(searchCity.value), 12)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap'
  }).addTo(map)

  map.on('click', handleMapClick)
}

function clearMarker() {
  if (marker) {
    marker.remove()
    marker = null
  }
}

function setMarker(latitude, longitude) {
  if (!map || !leafletInstance) {
    return
  }
  if (!marker) {
    marker = leafletInstance.circleMarker([latitude, longitude], {
      radius: 8,
      color: '#0071e3',
      weight: 2,
      fillColor: '#ffffff',
      fillOpacity: 1
    }).addTo(map)
  } else {
    marker.setLatLng([latitude, longitude])
  }
  map.setView([latitude, longitude], 15)
}

function normalizeCityName(address, fallbackCity = '') {
  const directCity = address?.city || address?.town || address?.municipality || ''
  const administrativeCity = address?.state || address?.province || address?.state_district || ''

  if (directCity && !/[区县旗]$/.test(directCity)) {
    return directCity
  }
  if (administrativeCity) {
    return administrativeCity
  }
  if (directCity) {
    return directCity
  }
  return fallbackCity || '上海市'
}

function normalizeDetailAddress(address, displayName = '', city = '') {
  const parts = [
    address?.suburb,
    address?.neighbourhood,
    address?.quarter,
    address?.road,
    address?.house_number,
    address?.building,
    address?.amenity
  ]
    .filter(Boolean)
    .filter((value, index, array) => array.indexOf(value) === index)

  if (parts.length) {
    return parts.join(' ')
  }

  const normalizedDisplay = String(displayName || '')
    .replace(/^中国[,，\s]*/, '')
    .replace(city, '')
    .replace(/^[,，\s]+/, '')

  return normalizedDisplay || '地图选点地址'
}

function normalizeGeocodeResult(item, fallbackCity = '') {
  const city = normalizeCityName(item.address, fallbackCity)
  const detailAddress = normalizeDetailAddress(item.address, item.display_name, city)
  const displayAddress = [city, detailAddress].filter(Boolean).join(' ')
  return {
    key: `${item.lat}-${item.lon}-${item.place_id || item.display_name}`,
    city,
    detailAddress,
    displayAddress,
    latitude: Number(item.lat),
    longitude: Number(item.lon)
  }
}

async function fetchLocationJson(url) {
  const response = await fetch(url, {
    headers: {
      'Accept-Language': 'zh-CN,zh;q=0.9'
    }
  })
  if (!response.ok) {
    throw new Error('地址服务暂时不可用')
  }
  return response.json()
}

async function searchAddress(keyword, city, limit = 8) {
  const query = encodeURIComponent([city, keyword].filter(Boolean).join(' '))
  const data = await fetchLocationJson(
    `https://nominatim.openstreetmap.org/search?format=jsonv2&limit=${limit}&accept-language=zh-CN&q=${query}`
  )
  return Array.isArray(data) ? data.map((item) => normalizeGeocodeResult(item, city)) : []
}

async function reverseGeocode(latitude, longitude, fallbackCity) {
  const data = await fetchLocationJson(
    `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${latitude}&lon=${longitude}&accept-language=zh-CN`
  )
  return normalizeGeocodeResult(data, fallbackCity)
}

async function focusInitialLocation() {
  if (!map) {
    return
  }

  const initialCity = props.city || searchCity.value
  const initialAddress = props.detailAddress?.trim()
  if (initialAddress) {
    const results = await searchAddress(initialAddress, initialCity, 1).catch(() => [])
    if (results.length) {
      searchResults.value = results
      focusSearchResult(results[0])
      return
    }
  }

  clearMarker()
  map.setView(getCityCenter(initialCity), 12)
}

async function handleSearch() {
  if (!searchKeyword.value) {
    ElMessage.warning('请先输入要搜索的地址关键词')
    return
  }

  searching.value = true
  try {
    const results = await searchAddress(searchKeyword.value, searchCity.value)
    searchResults.value = results
    if (!results.length) {
      ElMessage.warning('没有找到匹配地址，请尝试直接在地图上点选')
      return
    }
    focusSearchResult(results[0])
  } catch (error) {
    ElMessage.error(error.message || '地址搜索失败')
  } finally {
    searching.value = false
  }
}

function focusSearchResult(item) {
  searchCity.value = item.city || searchCity.value
  selectedLocation.value = item
  setMarker(item.latitude, item.longitude)
}

async function handleMapClick(event) {
  reverseGeocoding.value = true
  try {
    const result = await reverseGeocode(event.latlng.lat, event.latlng.lng, searchCity.value)
    searchCity.value = result.city || searchCity.value
    selectedLocation.value = result
    setMarker(result.latitude, result.longitude)
  } catch (error) {
    ElMessage.error(error.message || '地图反查地址失败')
  } finally {
    reverseGeocoding.value = false
  }
}

function locateCurrentPosition() {
  if (!navigator.geolocation) {
    ElMessage.warning('当前浏览器暂不支持定位')
    return
  }

  locatingCurrent.value = true
  navigator.geolocation.getCurrentPosition(
    async (position) => {
      try {
        const result = await reverseGeocode(
          position.coords.latitude,
          position.coords.longitude,
          searchCity.value
        )
        searchCity.value = result.city || searchCity.value
        selectedLocation.value = result
        setMarker(result.latitude, result.longitude)
      } catch (error) {
        ElMessage.error(error.message || '获取当前位置失败')
      } finally {
        locatingCurrent.value = false
      }
    },
    () => {
      locatingCurrent.value = false
      ElMessage.warning('定位失败，请检查浏览器定位权限')
    },
    {
      enableHighAccuracy: true,
      timeout: 10000
    }
  )
}

function confirmSelection() {
  if (!selectedLocation.value) {
    ElMessage.warning('请先搜索或在地图上选择一个地址')
    return
  }
  emit('select', selectedLocation.value)
  visible.value = false
}

watch(
  () => props.modelValue,
  async (value) => {
    if (!value) {
      return
    }

    searchCity.value = props.city || searchCity.value || CITY_OPTIONS[0]
    searchKeyword.value = props.detailAddress || ''
    searchResults.value = []
    selectedLocation.value = null
    clearMarker()

    loadingMap.value = true
    try {
      await nextTick()
      await ensureMap()
      window.setTimeout(() => {
        map?.invalidateSize()
      }, 120)
      await focusInitialLocation()
    } catch (error) {
      ElMessage.error(error.message || '地图加载失败，请先手动填写地址')
    } finally {
      loadingMap.value = false
    }
  }
)

watch(searchCity, (value) => {
  if (!value || !map || selectedLocation.value || !props.modelValue) {
    return
  }
  map.setView(getCityCenter(value), 12)
})

onBeforeUnmount(() => {
  clearMarker()
  if (map) {
    map.off('click', handleMapClick)
    map.remove()
    map = null
  }
})
</script>

<style scoped>
.map-picker {
  display: grid;
  gap: 16px;
}

.map-picker__toolbar {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr) auto auto;
  gap: 12px;
}

.map-picker__layout {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(280px, 0.85fr);
  gap: 16px;
}

.map-picker__map-card {
  min-height: 520px;
  border: 1px solid rgba(29, 29, 31, 0.08);
  border-radius: 24px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.64);
}

.map-picker__map {
  width: 100%;
  min-height: 520px;
}

.map-picker__side {
  display: grid;
  gap: 14px;
}

.map-picker__results {
  display: grid;
  gap: 12px;
}

.map-picker__result-list {
  display: grid;
  gap: 10px;
  max-height: 320px;
  overflow: auto;
  padding-right: 4px;
}

.map-picker__result-item {
  display: grid;
  gap: 6px;
  text-align: left;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(29, 29, 31, 0.08);
  background: rgba(255, 255, 255, 0.68);
  cursor: pointer;
  transition:
    transform 0.24s ease,
    border-color 0.24s ease,
    box-shadow 0.24s ease;
}

.map-picker__result-item:hover {
  transform: translateY(-1px);
  border-color: rgba(0, 113, 227, 0.22);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
}

@media (max-width: 960px) {
  .map-picker__toolbar {
    grid-template-columns: 1fr;
  }

  .map-picker__layout {
    grid-template-columns: 1fr;
  }

  .map-picker__map-card,
  .map-picker__map {
    min-height: 360px;
  }
}
</style>
