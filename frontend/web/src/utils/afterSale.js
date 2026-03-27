const AFTER_SALE_STATUS_META = {
  PENDING: { label: '待处理', tagType: 'warning' },
  PROCESSING: { label: '处理中', tagType: 'primary' },
  RESOLVED: { label: '已解决', tagType: 'success' },
  REJECTED: { label: '已驳回', tagType: 'danger' }
}

export function getAfterSaleStatusLabel(status) {
  return AFTER_SALE_STATUS_META[status]?.label || status || '未知状态'
}

export function getAfterSaleStatusTagType(status) {
  return AFTER_SALE_STATUS_META[status]?.tagType || 'info'
}

export function normalizeAttachments(attachments) {
  if (!Array.isArray(attachments)) {
    return []
  }

  return attachments.filter((item) => item?.url)
}
