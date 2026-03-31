import { getOrderStatusLabel, normalizeOrderStatus } from './order'

const ORDER_STATUS_SEQUENCE = [
  'PENDING',
  'ACCEPTED',
  'CONFIRMED',
  'IN_SERVICE',
  'WAITING_USER_CONFIRMATION',
  'COMPLETED'
]

export function buildOrderStatusMap(orders = []) {
  return orders.reduce((accumulator, item) => {
    const key = normalizeOrderStatus(item.status)
    accumulator[key] = (accumulator[key] || 0) + 1
    return accumulator
  }, {})
}

export function buildOrderStatusSeriesData(orders = []) {
  const record = buildOrderStatusMap(orders)
  return ORDER_STATUS_SEQUENCE.filter((key) => record[key]).map((key) => ({
    name: getOrderStatusLabel(key),
    value: record[key]
  }))
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

export function buildCompactDonutOption(seriesData = [], options = {}) {
  const rows = (seriesData || []).filter((item) => Number(item?.value) > 0)
  const total = rows.reduce((sum, item) => sum + Number(item.value || 0), 0)
  const unit = options.unit || '单'
  const colors = options.colors || ['#0071e3', '#5e9eff', '#8cc0ff', '#b6d8ff', '#d5e9ff', '#ebf3ff']
  const centerX = options.centerX || '30%'

  return {
    tooltip: {
      trigger: 'item',
      formatter: ({ name, value }) => `${name}<br/>${value}${unit}`
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 0,
      top: 'center',
      itemWidth: 10,
      itemHeight: 10,
      icon: 'roundRect',
      pageIconColor: '#0071e3',
      pageTextStyle: { color: '#6e6e73' },
      textStyle: {
        color: '#6e6e73',
        fontSize: 12,
        width: 104,
        overflow: 'truncate'
      },
      formatter: (name) => {
        const matched = rows.find((item) => item.name === name)
        return `${name}  ${matched?.value ?? 0}${unit}`
      }
    },
    title: rows.length
      ? [
          {
            text: String(total),
            left: centerX,
            top: '40%',
            textAlign: 'center',
            textStyle: {
              color: '#1d1d1f',
              fontSize: 28,
              fontWeight: 700
            }
          },
          {
            text: options.centerLabel || '总计',
            left: centerX,
            top: '56%',
            textAlign: 'center',
            textStyle: {
              color: '#6e6e73',
              fontSize: 12,
              fontWeight: 500
            }
          }
        ]
      : [],
    color: colors,
    series: [
      {
        type: 'pie',
        center: [centerX, '50%'],
        radius: ['52%', '72%'],
        minAngle: 8,
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 12,
          borderColor: '#ffffff',
          borderWidth: 4
        },
        label: { show: false },
        labelLine: { show: false },
        emphasis: {
          scale: true,
          label: {
            show: true,
            formatter: `{b}\n{c}${unit}`,
            color: '#1d1d1f',
            fontSize: 13,
            fontWeight: 600
          }
        },
        data: rows
      }
    ]
  }
}
