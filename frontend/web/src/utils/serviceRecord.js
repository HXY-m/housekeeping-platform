const SERVICE_RECORD_STAGE_LABELS = {
  CHECK_IN: '上门打卡',
  SERVICE_PROOF: '服务过程',
  FINISH_PROOF: '完工凭证'
}

const SERVICE_RECORD_STAGE_TAGS = {
  CHECK_IN: 'warning',
  SERVICE_PROOF: 'primary',
  FINISH_PROOF: 'success'
}

export function getServiceRecordStageLabel(stage) {
  return SERVICE_RECORD_STAGE_LABELS[stage] || stage || '过程记录'
}

export function getServiceRecordStageTagType(stage) {
  return SERVICE_RECORD_STAGE_TAGS[stage] || 'info'
}
