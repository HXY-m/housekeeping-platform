function splitValues(value) {
  return String(value || '')
    .split(/[、,，/\n]/)
    .map((item) => item.trim())
    .filter(Boolean)
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
  const application = typeof applicationOrCertificates === 'string'
    ? { certificates: applicationOrCertificates, attachments: [] }
    : (applicationOrCertificates || {})

  const labels = getWorkerCertificateLabels(application.certificates)
  const attachments = getWorkerQualificationAttachments(application)
  const labelPart = labels.length ? labels.join('、') : '未填写资质标签'
  const attachmentPart = attachments.length ? `${attachments.length} 份附件` : '无附件'
  return `${labelPart} · ${attachmentPart}`
}
