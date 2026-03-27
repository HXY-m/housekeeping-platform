export const ORDER_STATUS_LABEL_MAP = {
  PENDING: '待接单',
  ACCEPTED: '已接单',
  IN_SERVICE: '服务中',
  COMPLETED: '已完成'
}

export function normalizeOrderStatus(status) {
  if (!status) return 'PENDING'
  if (ORDER_STATUS_LABEL_MAP[status]) return status
  if (status === '待服务' || status === '待接单') return 'PENDING'
  if (status === '已接单') return 'ACCEPTED'
  if (status === '服务中') return 'IN_SERVICE'
  if (status === '已完成') return 'COMPLETED'
  return status
}

export function getOrderStatusLabel(status) {
  const code = normalizeOrderStatus(status)
  return ORDER_STATUS_LABEL_MAP[code] || status
}

export function getOrderStatusTagType(status) {
  const code = normalizeOrderStatus(status)
  if (code === 'PENDING') return 'info'
  if (code === 'ACCEPTED') return 'warning'
  if (code === 'IN_SERVICE') return 'primary'
  if (code === 'COMPLETED') return 'success'
  return 'info'
}
