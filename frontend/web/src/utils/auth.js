export const ROLE_USER = 'USER'
export const ROLE_WORKER = 'WORKER'
export const ROLE_ADMIN = 'ADMIN'

export function resolveHomePath(roleCode) {
  if (roleCode === ROLE_ADMIN) return '/admin/dashboard'
  if (roleCode === ROLE_WORKER) return '/worker/dashboard'
  return '/user/dashboard'
}

export function formatRoleLabel(roleCode) {
  if (roleCode === ROLE_ADMIN) return '管理员'
  if (roleCode === ROLE_WORKER) return '服务人员'
  return '普通用户'
}

export function resolveConsoleEntry(roleCode) {
  if (roleCode === ROLE_ADMIN) {
    return { label: '进入后台', path: '/admin/dashboard' }
  }
  if (roleCode === ROLE_WORKER) {
    return { label: '进入工作台', path: '/worker/dashboard' }
  }
  return { label: '进入用户中心', path: '/user/dashboard' }
}
