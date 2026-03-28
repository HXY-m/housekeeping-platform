import { computed, ref, unref, watch } from 'vue'

export function useClientPagination(source, defaultPageSize = 8) {
  const currentPage = ref(1)
  const pageSize = ref(defaultPageSize)
  const pageSizes = [6, 8, 10, 20]

  const total = computed(() => {
    const items = unref(source)
    return Array.isArray(items) ? items.length : 0
  })

  const pagedItems = computed(() => {
    const items = unref(source)
    if (!Array.isArray(items) || !items.length) {
      return []
    }
    const start = (currentPage.value - 1) * pageSize.value
    return items.slice(start, start + pageSize.value)
  })

  function resetPage() {
    currentPage.value = 1
  }

  watch([total, pageSize], ([nextTotal]) => {
    const pageCount = Math.max(1, Math.ceil(nextTotal / pageSize.value))
    if (currentPage.value > pageCount) {
      currentPage.value = 1
    }
  })

  return {
    currentPage,
    pageSize,
    pageSizes,
    total,
    pagedItems,
    resetPage
  }
}
