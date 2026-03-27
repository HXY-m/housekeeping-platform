<template>
  <div v-if="items.length" class="attachment-gallery" :class="{ 'attachment-gallery--compact': compact }">
    <el-image
      v-for="(item, index) in visibleItems"
      :key="`${item.url}-${index}`"
      :src="item.url"
      :preview-src-list="previewList"
      :initial-index="index"
      fit="cover"
      preview-teleported
      class="attachment-thumb"
    />
    <span v-if="showCount" class="attachment-count">共 {{ items.length }} 张</span>
  </div>
  <span v-else class="muted-line">{{ emptyText }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { normalizeAttachments } from '../../utils/afterSale'

const props = defineProps({
  attachments: {
    type: Array,
    default: () => []
  },
  limit: {
    type: Number,
    default: 3
  },
  compact: {
    type: Boolean,
    default: false
  },
  showCount: {
    type: Boolean,
    default: true
  },
  emptyText: {
    type: String,
    default: '未上传凭证'
  }
})

const items = computed(() => normalizeAttachments(props.attachments))
const previewList = computed(() => items.value.map((item) => item.url))
const visibleItems = computed(() => items.value.slice(0, props.limit))
</script>
