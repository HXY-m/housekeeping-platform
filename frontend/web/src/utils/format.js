const currencyFormatter = new Intl.NumberFormat('zh-CN', {
  style: 'currency',
  currency: 'CNY',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

export function formatCurrency(value) {
  const amount = Number(value)
  if (!Number.isFinite(amount)) {
    return currencyFormatter.format(0)
  }
  return currencyFormatter.format(amount)
}

export function formatDateTime(value) {
  if (!value) {
    return '未记录'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}
