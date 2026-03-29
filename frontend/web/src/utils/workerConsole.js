const APPROVED_GUIDE_PREFIX = 'housekeeping_worker_approved_guide_seen_'

function toNumber(value) {
  const parsed = Number(value || 0)
  return Number.isFinite(parsed) ? parsed : 0
}

export function getWorkerTodoCount(summary = {}) {
  return (
    toNumber(summary.pending) +
    toNumber(summary.confirmed) +
    toNumber(summary.inService)
  )
}

export function getWorkerTodoItems(summary = {}) {
  return [
    {
      key: 'pending',
      label: '待接单',
      value: toNumber(summary.pending),
      hint: '优先确认是否接单，减少用户等待时间。'
    },
    {
      key: 'confirmed',
      label: '待开工',
      value: toNumber(summary.confirmed),
      hint: '用户已确认预约，建议先上传上门打卡。'
    },
    {
      key: 'inService',
      label: '服务中',
      value: toNumber(summary.inService),
      hint: '补充过程记录和完工凭证，方便用户确认。'
    },
    {
      key: 'waitingUserConfirmation',
      label: '待用户确认',
      value: toNumber(summary.waitingUserConfirmation),
      hint: '已提交完工，等待用户确认结果。'
    }
  ]
}

export function shouldShowWorkerApprovedGuide(userId) {
  if (!userId) {
    return false
  }
  return localStorage.getItem(`${APPROVED_GUIDE_PREFIX}${userId}`) !== '1'
}

export function markWorkerApprovedGuideSeen(userId) {
  if (!userId) {
    return
  }
  localStorage.setItem(`${APPROVED_GUIDE_PREFIX}${userId}`, '1')
}
