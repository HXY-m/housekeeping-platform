import { ref } from 'vue'

function uniquePageSizes(defaultPageSize) {
  return Array.from(new Set([defaultPageSize, 10, 20, 50])).sort((left, right) => left - right)
}

export function useServerPagination(defaultPageSize = 10) {
  const currentPage = ref(1)
  const pageSize = ref(defaultPageSize)
  const total = ref(0)
  const totalPages = ref(0)
  const pageSizes = uniquePageSizes(defaultPageSize)

  function buildParams(extra = {}) {
    return {
      current: currentPage.value,
      size: pageSize.value,
      ...extra
    }
  }

  function applyPageResult(result) {
    total.value = Number(result?.total || 0)
    totalPages.value = Number(result?.pages || 0)
    return Array.isArray(result?.records) ? result.records : []
  }

  function resetPage() {
    currentPage.value = 1
  }

  return {
    currentPage,
    pageSize,
    pageSizes,
    total,
    totalPages,
    buildParams,
    applyPageResult,
    resetPage
  }
}
