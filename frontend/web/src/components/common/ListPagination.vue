<template>
  <div class="table-pagination">
    <el-pagination
      :current-page="currentPage"
      :page-size="pageSize"
      :page-sizes="pageSizes"
      :layout="layout"
      :total="total"
      :small="small"
      background
      @update:current-page="emit('update:currentPage', $event)"
      @update:page-size="emit('update:pageSize', $event)"
    />
  </div>
</template>

<script setup>
defineProps({
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 8 },
  pageSizes: { type: Array, default: () => [6, 8, 10, 20] },
  total: { type: Number, default: 0 },
  layout: { type: String, default: 'total, sizes, prev, pager, next, jumper' },
  small: { type: Boolean, default: false }
})

const emit = defineEmits(['update:currentPage', 'update:pageSize'])
</script>
