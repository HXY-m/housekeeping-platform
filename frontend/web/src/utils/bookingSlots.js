export const BOOKING_SLOT_META_MAP = {
  '08:00-10:00': {
    label: '上午',
    period: '08:00 - 10:00',
    desc: '适合常规上门'
  },
  '10:00-12:00': {
    label: '午前',
    period: '10:00 - 12:00',
    desc: '适合半日服务'
  },
  '13:00-15:00': {
    label: '下午',
    period: '13:00 - 15:00',
    desc: '适合深度清洁'
  },
  '15:00-17:00': {
    label: '傍晚',
    period: '15:00 - 17:00',
    desc: '适合下班前后预约'
  },
  '18:00-20:00': {
    label: '晚间',
    period: '18:00 - 20:00',
    desc: '适合夜间上门'
  }
}

export const BOOKING_SLOT_ORDER = Object.keys(BOOKING_SLOT_META_MAP)

export function getBookingSlotMeta(slot) {
  return BOOKING_SLOT_META_MAP[slot] || {
    label: '预约时段',
    period: slot || '--',
    desc: '按选定时间上门'
  }
}

export function splitBookingSlots(slot) {
  if (!slot) {
    return []
  }
  return String(slot)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .sort((a, b) => BOOKING_SLOT_ORDER.indexOf(a) - BOOKING_SLOT_ORDER.indexOf(b))
}

export function resolveRequiredSlotCount(serviceDuration = '') {
  const text = String(serviceDuration || '')
  const hours = Array.from(text.matchAll(/(\d+)/g)).map((match) => Number(match[1]))
  if (hours.length) {
    return Math.max(1, Math.ceil(Math.max(...hours) / 2))
  }
  if (text.includes('全天')) {
    return 4
  }
  if (text.includes('半天')) {
    return 2
  }
  return 1
}

export function buildContinuousSlotGroups(availableSlots = [], requiredCount = 1) {
  const availableSet = new Set(availableSlots)
  return BOOKING_SLOT_ORDER.map((startSlot, startIndex) => {
    const slots = BOOKING_SLOT_ORDER.slice(startIndex, startIndex + requiredCount)
    const complete = slots.length === requiredCount
    const available = complete && slots.every((slot) => availableSet.has(slot))
    return {
      startSlot,
      value: slots.join(','),
      slots,
      available
    }
  }).filter((group) => group.slots.length === requiredCount)
}

function mergeSlotPeriods(slots) {
  if (!slots.length) {
    return '--'
  }
  const ranges = []
  let currentStart = ''
  let currentEnd = ''

  slots.forEach((slot) => {
    const [start, end] = slot.split('-')
    if (!currentStart) {
      currentStart = start
      currentEnd = end
      return
    }
    if (start === currentEnd) {
      currentEnd = end
      return
    }
    ranges.push(`${currentStart} - ${currentEnd}`)
    currentStart = start
    currentEnd = end
  })

  ranges.push(`${currentStart} - ${currentEnd}`)
  return ranges.join('、')
}

export function formatBookingSlot(slot) {
  const slots = splitBookingSlots(slot)
  if (!slots.length) {
    return '--'
  }
  if (slots.length === 1) {
    const meta = getBookingSlotMeta(slots[0])
    return `${meta.label} ${meta.period}`
  }
  return `${slots.length * 2}小时 ${mergeSlotPeriods(slots)}`
}

export function formatBookingDateTime(date, slot) {
  return [date, formatBookingSlot(slot)].filter(Boolean).join(' ')
}
