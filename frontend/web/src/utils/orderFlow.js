import { normalizeOrderStatus } from './order'

const ORDER_STEPS = [
  { key: 'PENDING', title: '提交预约' },
  { key: 'ACCEPTED', title: '服务接单' },
  { key: 'CONFIRMED', title: '用户确认' },
  { key: 'IN_SERVICE', title: '上门服务' },
  { key: 'WAITING_USER_CONFIRMATION', title: '确认完工' }
]

export function getOrderSteps() {
  return ORDER_STEPS
}

export function getOrderStepProgress(status) {
  const code = normalizeOrderStatus(status)
  if (code === 'COMPLETED') {
    return ORDER_STEPS.length
  }
  const index = ORDER_STEPS.findIndex((item) => item.key === code)
  return index < 0 ? 1 : index + 1
}

export function getUserOrderFlowMeta(order) {
  const status = normalizeOrderStatus(order?.status)

  if (status === 'PENDING') {
    return {
      owner: '服务人员',
      ownerType: 'warning',
      title: '等待服务人员接单',
      description: '你已提交预约，当前由服务人员决定是否接单。',
      hint: '接单后需要你再次确认预约安排。'
    }
  }

  if (status === 'ACCEPTED') {
    return {
      owner: '用户',
      ownerType: 'primary',
      title: '等待你确认预约安排',
      description: '服务人员已经接单，现在需要你确认服务时间与地址无误。',
      hint: '确认后订单才会进入正式上门准备阶段。'
    }
  }

  if (status === 'CONFIRMED') {
    return {
      owner: '服务人员',
      ownerType: 'warning',
      title: '等待服务人员上门',
      description: '你已确认预约安排，服务人员接下来会准备上门并开始服务。',
      hint: '如联系方式或地址有变化，建议尽快补充沟通。'
    }
  }

  if (status === 'IN_SERVICE') {
    return {
      owner: '服务人员',
      ownerType: 'success',
      title: '服务进行中',
      description: '服务人员正在履约，过程记录和现场凭证会同步展示在时间线里。',
      hint: '如果出现明显问题，可以保留凭证并在服务结束后发起售后。'
    }
  }

  if (status === 'WAITING_USER_CONFIRMATION') {
    return {
      owner: '用户',
      ownerType: 'danger',
      title: '等待你确认完工',
      description: '服务人员已提交完工并上传收尾凭证，现在需要你确认本次服务是否完成。',
      hint: '确认完工后才能进入评价环节。'
    }
  }

  return {
    owner: order?.afterSale ? '管理员' : (order?.reviewed ? '已闭环' : '用户'),
    ownerType: order?.afterSale ? 'danger' : 'success',
    title: order?.afterSale ? '订单进入售后处理' : '订单流程已完成',
    description: order?.afterSale
      ? '你已经提交售后工单，当前由平台客服继续跟进处理。'
      : '订单已经完成，可以评价服务体验或继续预约其他服务。',
    hint: order?.afterSale ? '可在订单卡片中持续查看售后处理进度。' : '评价完成后，这笔订单就形成完整闭环。'
  }
}

export function getWorkerOrderFlowMeta(order) {
  const status = normalizeOrderStatus(order?.status)

  if (status === 'PENDING') {
    return {
      owner: '服务人员',
      ownerType: 'warning',
      title: '等待你接单',
      description: '用户已经提交预约，当前需要你决定是否接单。',
      hint: '接单后，订单会进入“等待用户确认”阶段。'
    }
  }

  if (status === 'ACCEPTED') {
    return {
      owner: '用户',
      ownerType: 'info',
      title: '等待用户确认预约安排',
      description: '你已接单，但还需要用户确认服务时间与地址。',
      hint: '在用户确认前，暂时不能直接开始服务。'
    }
  }

  if (status === 'CONFIRMED') {
    return {
      owner: '服务人员',
      ownerType: 'primary',
      title: '等待你开始服务',
      description: '用户已经确认预约安排，现在可以上传上门打卡并开始服务。',
      hint: '建议先补一条上门记录，再正式开始服务。'
    }
  }

  if (status === 'IN_SERVICE') {
    return {
      owner: '服务人员',
      ownerType: 'success',
      title: '等待你补充过程记录',
      description: '服务进行中，你可以持续上传过程凭证，并在结束时提交完工。',
      hint: '提交完工前，建议至少上传一条完工凭证。'
    }
  }

  if (status === 'WAITING_USER_CONFIRMATION') {
    return {
      owner: '用户',
      ownerType: 'danger',
      title: '等待用户确认完工',
      description: '你已提交完工，接下来需要等待用户确认服务结果。',
      hint: '若用户有异议，订单可能进入售后阶段。'
    }
  }

  return {
    owner: order?.reviewed ? '已闭环' : '用户',
    ownerType: 'success',
    title: order?.reviewed ? '订单流程已闭环' : '等待用户评价',
    description: order?.reviewed
      ? '用户已经完成评价，本次履约闭环结束。'
      : '订单已完成，当前等待用户补充评价或发起售后。',
    hint: order?.reviewed ? '可以继续处理新的订单。' : '如有争议，平台会转入售后处理。'
  }
}
