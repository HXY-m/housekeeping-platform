<template>
  <div v-if="items.length" class="file-link-list">
    <a
      v-for="(item, index) in items"
      :key="item.id || item.uid || `${item.url}-${index}`"
      :href="item.url"
      target="_blank"
      rel="noreferrer"
      class="file-link-chip"
    >
      <div class="file-link-chip__title">{{ item.name }}</div>
      <div class="file-link-chip__meta">
        <span>{{ fileExtension(item.name) }}</span>
        <span v-if="item.size">{{ formatFileSize(item.size) }}</span>
      </div>
    </a>
  </div>
  <span v-else class="muted-line">{{ emptyText }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  files: {
    type: Array,
    default: () => []
  },
  emptyText: {
    type: String,
    default: '未上传附件'
  }
})

const items = computed(() =>
  (props.files || []).filter((item) => item?.url)
)

function fileExtension(name) {
  const parts = String(name || '').split('.')
  if (parts.length <= 1) {
    return 'FILE'
  }
  return parts[parts.length - 1].toUpperCase()
}

function formatFileSize(size) {
  const value = Number(size || 0)
  if (!value) return ''
  if (value < 1024) return `${value} B`
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}
</script>
