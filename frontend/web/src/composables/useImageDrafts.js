import { computed, onBeforeUnmount, ref } from 'vue'
import { ElMessage } from 'element-plus'

export function useImageDrafts(options = {}) {
  const {
    maxCount = 3,
    maxSize = 5 * 1024 * 1024,
    countExceededMessage = `最多上传 ${maxCount} 张图片`
  } = options

  const inputRef = ref(null)
  const drafts = ref([])

  const previewList = computed(() => drafts.value.map((item) => item.url))

  function openPicker() {
    inputRef.value?.click()
  }

  function handleInputChange(event) {
    const files = Array.from(event?.target?.files || [])
    if (!files.length) {
      return
    }

    const nextDrafts = [...drafts.value]
    for (const file of files) {
      if (!file.type.startsWith('image/')) {
        ElMessage.warning(`文件 ${file.name} 不是图片格式`)
        continue
      }
      if (file.size > maxSize) {
        ElMessage.warning(`文件 ${file.name} 超过大小限制`)
        continue
      }
      if (nextDrafts.length >= maxCount) {
        ElMessage.warning(countExceededMessage)
        break
      }

      const duplicated = nextDrafts.some(
        (item) =>
          item.name === file.name &&
          item.size === file.size &&
          item.lastModified === file.lastModified
      )
      if (duplicated) {
        continue
      }

      nextDrafts.push({
        uid: `${Date.now()}-${Math.random()}`,
        file,
        name: file.name,
        size: file.size,
        lastModified: file.lastModified,
        url: URL.createObjectURL(file)
      })
    }

    drafts.value = nextDrafts
    if (event?.target) {
      event.target.value = ''
    }
  }

  function removeDraft(uid) {
    const current = drafts.value.find((item) => item.uid === uid)
    if (current?.url) {
      URL.revokeObjectURL(current.url)
    }
    drafts.value = drafts.value.filter((item) => item.uid !== uid)
  }

  function resetDrafts() {
    drafts.value.forEach((item) => {
      if (item.url) {
        URL.revokeObjectURL(item.url)
      }
    })
    drafts.value = []
    if (inputRef.value) {
      inputRef.value.value = ''
    }
  }

  function formatFileSize(size) {
    if (size < 1024) return `${size} B`
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
    return `${(size / 1024 / 1024).toFixed(1)} MB`
  }

  onBeforeUnmount(resetDrafts)

  return {
    inputRef,
    drafts,
    previewList,
    openPicker,
    handleInputChange,
    removeDraft,
    resetDrafts,
    formatFileSize
  }
}
