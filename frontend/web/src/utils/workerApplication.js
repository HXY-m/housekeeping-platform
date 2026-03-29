function splitValues(value) {
  return String(value || '')
    .split(/[、,，;\n]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

const WORKER_QUALIFICATION_STATUS_MAP = {
  UNSUBMITTED: {
    label: '待提交',
    tagType: 'info'
  },
  PENDING: {
    label: '审核中',
    tagType: 'warning'
  },
  APPROVED: {
    label: '已认证',
    tagType: 'success'
  },
  REJECTED: {
    label: '已驳回',
    tagType: 'danger'
  }
}

function normalizeAttachments(items) {
  if (!Array.isArray(items)) {
    return []
  }
  return items
    .map((item, index) => {
      if (!item?.url) {
        return null
      }
      return {
        uid: item.uid || item.id || `file-${index}`,
        id: item.id || null,
        name: item.name || `资质附件 ${index + 1}`,
        url: item.url,
        size: Number(item.size || 0),
        remoteUrl: item.url
      }
    })
    .filter(Boolean)
}

function parseLegacyCertificatePayload(raw) {
  if (!raw) {
    return { labels: [], attachments: [] }
  }

  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      return { labels: splitValues(parsed.join('、')), attachments: [] }
    }
    if (parsed && typeof parsed === 'object') {
      return {
        labels: Array.isArray(parsed.labels) ? parsed.labels.map((item) => String(item).trim()).filter(Boolean) : [],
        attachments: normalizeAttachments(parsed.attachments)
      }
    }
  } catch {
    return { labels: splitValues(raw), attachments: [] }
  }

  return { labels: splitValues(raw), attachments: [] }
}

export function getWorkerCertificateLabels(certificates) {
  const legacy = parseLegacyCertificatePayload(certificates)
  return legacy.labels.length ? legacy.labels : splitValues(certificates)
}

export function getWorkerQualificationAttachments(application) {
  if (Array.isArray(application?.attachments) && application.attachments.length) {
    return normalizeAttachments(application.attachments)
  }
  return parseLegacyCertificatePayload(application?.certificates).attachments
}

export function formatWorkerCertificateSummary(applicationOrCertificates) {
  const application =
    typeof applicationOrCertificates === 'string'
      ? { certificates: applicationOrCertificates, attachments: [] }
      : applicationOrCertificates || {}

  const labels = getWorkerCertificateLabels(application.certificates)
  const attachments = getWorkerQualificationAttachments(application)
  const labelPart = labels.length ? labels.join('、') : '未填写资质标签'
  const attachmentPart = attachments.length ? `${attachments.length} 份附件` : '无附件'
  return `${labelPart} · ${attachmentPart}`
}

export function normalizeWorkerQualificationStatus(status) {
  const code = String(status || '').trim().toUpperCase()
  return WORKER_QUALIFICATION_STATUS_MAP[code] ? code : 'UNSUBMITTED'
}

export function getWorkerQualificationLabel(status) {
  const code = normalizeWorkerQualificationStatus(status)
  return WORKER_QUALIFICATION_STATUS_MAP[code].label
}

export function getWorkerQualificationTagType(status) {
  const code = normalizeWorkerQualificationStatus(status)
  return WORKER_QUALIFICATION_STATUS_MAP[code].tagType
}

export function isWorkerQualificationApproved(status) {
  return normalizeWorkerQualificationStatus(status) === 'APPROVED'
}

export function getWorkerQualificationNotice(status) {
  const code = normalizeWorkerQualificationStatus(status)

  if (code === 'PENDING') {
    return {
      title: '资质审核中，暂时不能接单',
      description: '请等待管理员审核。审核通过后，接单、开工和服务记录上传入口会自动开放。',
      buttonText: '查看资质进度',
      type: 'warning'
    }
  }

  if (code === 'REJECTED') {
    return {
      title: '资质审核未通过，暂时不能接单',
      description: '请根据驳回意见补充服务信息和资质文件，重新提交审核后再处理订单。',
      buttonText: '重新完善资质',
      type: 'error'
    }
  }

  if (code === 'APPROVED') {
    return {
      title: '资质已认证，可以开始接单',
      description: '你的服务信息已经对外展示，现在可以正常接单、开工和上传服务记录。',
      buttonText: '查看资质资料',
      type: 'success'
    }
  }

  return {
    title: '请先填写资质信息并提交审核',
    description: '服务类型、服务区域、接单时段和资质附件都需要在资质材料页提交。审核通过前不能接单。',
    buttonText: '去填写资质信息',
    type: 'info'
  }
}
