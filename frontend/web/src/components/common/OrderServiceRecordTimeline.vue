<template>
  <el-timeline v-if="records.length">
    <el-timeline-item
      v-for="record in records"
      :key="record.id"
      :timestamp="formatDateTime(record.createdAt)"
      :type="timelineType(record.stage)"
    >
      <div class="stack-cell">
        <div class="tag-group">
          <el-tag :type="getServiceRecordStageTagType(record.stage)">
            {{ getServiceRecordStageLabel(record.stage) }}
          </el-tag>
        </div>
        <strong>{{ record.description }}</strong>
        <AttachmentGallery
          :attachments="record.attachments"
          compact
          empty-text="暂未上传图片"
        />
      </div>
    </el-timeline-item>
  </el-timeline>
  <el-empty v-else :description="emptyText" />
</template>

<script setup>
import AttachmentGallery from './AttachmentGallery.vue'
import { formatDateTime } from '../../utils/format'
import {
  getServiceRecordStageLabel,
  getServiceRecordStageTagType
} from '../../utils/serviceRecord'

defineProps({
  records: {
    type: Array,
    default: () => []
  },
  emptyText: {
    type: String,
    default: '暂未上传服务记录'
  }
})

function timelineType(stage) {
  if (stage === 'CHECK_IN') return 'warning'
  if (stage === 'SERVICE_PROOF') return 'primary'
  if (stage === 'FINISH_PROOF') return 'success'
  return 'info'
}
</script>
