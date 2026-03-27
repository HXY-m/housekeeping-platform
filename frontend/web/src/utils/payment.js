const PAYMENT_STATUS_META = {
  UNPAID: { label: '待支付', tagType: 'danger' },
  PAID: { label: '已支付', tagType: 'success' }
}

const PAYMENT_METHOD_LABELS = {
  WECHAT_PAY: '微信支付',
  ALIPAY: '支付宝',
  BANK_CARD: '银行卡'
}

export function getPaymentStatusLabel(status) {
  return PAYMENT_STATUS_META[status]?.label || status || '待支付'
}

export function getPaymentStatusTagType(status) {
  return PAYMENT_STATUS_META[status]?.tagType || 'info'
}

export function getPaymentMethodLabel(method) {
  return PAYMENT_METHOD_LABELS[method] || method || '未支付'
}
