import { normalizeOrderStatus } from './order'

export function buildOrderStatusMap(orders = []) {
  return orders.reduce((accumulator, item) => {
    const key = normalizeOrderStatus(item.status)
    accumulator[key] = (accumulator[key] || 0) + 1
    return accumulator
  }, {})
}

export function buildServiceMap(orders = []) {
  return orders.reduce((accumulator, item) => {
    const key = item.serviceName || '未分类服务'
    accumulator[key] = (accumulator[key] || 0) + 1
    return accumulator
  }, {})
}

export function buildDateMap(orders = []) {
  return orders.reduce((accumulator, item) => {
    const key = item.bookingDate || '未安排'
    accumulator[key] = (accumulator[key] || 0) + 1
    return accumulator
  }, {})
}

export function buildAfterSaleStatusMap(afterSales = []) {
  return afterSales.reduce((accumulator, item) => {
    const key = item.status || 'UNKNOWN'
    accumulator[key] = (accumulator[key] || 0) + 1
    return accumulator
  }, {})
}

export function mapToSeriesData(record = {}, labelMap = {}) {
  return Object.entries(record).map(([key, value]) => ({
    name: labelMap[key] || key,
    value
  }))
}

export function mapToSortedRows(record = {}) {
  return Object.entries(record)
    .map(([name, value]) => ({ name, value }))
    .sort((left, right) => right.value - left.value)
}
