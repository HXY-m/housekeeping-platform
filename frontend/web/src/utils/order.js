export const ORDER_STATUS_LABEL_MAP = {
  PENDING: '待接单',
  ACCEPTED: '待用户确认',
  CONFIRMED: '待上门',
  IN_SERVICE: '服务中',
  WAITING_USER_CONFIRMATION: '待用户确认完工',
  COMPLETED: '已完成'
}

export const ORDER_STATUS_SORT_WEIGHT_MAP = {
  PENDING: 0,
  ACCEPTED: 1,
  CONFIRMED: 2,
  IN_SERVICE: 3,
  WAITING_USER_CONFIRMATION: 4,
  COMPLETED: 5
}

export const ORDER_STATUS_OPTIONS = Object.entries(ORDER_STATUS_LABEL_MAP).map(([value, label]) => ({
  label,
  value
}))

export const WORKER_ORDER_STATUS_OPTIONS = [
  { label: '待接单', value: 'PENDING' },
  { label: '待用户确认', value: 'ACCEPTED' },
  { label: '待开工', value: 'CONFIRMED' },
  { label: '服务中', value: 'IN_SERVICE' },
  { label: '待确认完工', value: 'WAITING_USER_CONFIRMATION' },
  { label: '已完成', value: 'COMPLETED' }
]

const LEGACY_STATUS_MAP = {
  待服务: 'PENDING',
  待接单: 'PENDING',
  已接单: 'ACCEPTED',
  用户已确认: 'CONFIRMED',
  服务中: 'IN_SERVICE',
  待用户确认完工: 'WAITING_USER_CONFIRMATION',
  已完成: 'COMPLETED'
}

export function normalizeOrderStatus(status) {
  if (!status) return 'PENDING'
  if (ORDER_STATUS_LABEL_MAP[status]) return status
  return LEGACY_STATUS_MAP[status] || status
}

export function getOrderStatusLabel(status) {
  const code = normalizeOrderStatus(status)
  return ORDER_STATUS_LABEL_MAP[code] || status
}

export function getOrderStatusSortWeight(status) {
  const code = normalizeOrderStatus(status)
  return ORDER_STATUS_SORT_WEIGHT_MAP[code] ?? 99
}

export function getOrderStatusTagType(status) {
  const code = normalizeOrderStatus(status)
  if (code === 'PENDING') return 'info'
  if (code === 'ACCEPTED') return 'warning'
  if (code === 'CONFIRMED') return 'primary'
  if (code === 'IN_SERVICE') return 'success'
  if (code === 'WAITING_USER_CONFIRMATION') return 'danger'
  if (code === 'COMPLETED') return 'success'
  return 'info'
}
