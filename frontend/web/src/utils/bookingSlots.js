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

export function formatBookingSlot(slot) {
  const meta = getBookingSlotMeta(slot)
  if (!slot) {
    return '--'
  }
  return `${meta.label} ${meta.period}`
}

export function formatBookingDateTime(date, slot) {
  return [date, formatBookingSlot(slot)].filter(Boolean).join(' ')
}
