export const ROLE_USER = 'USER'
export const ROLE_WORKER = 'WORKER'
export const ROLE_ADMIN = 'ADMIN'

export function resolveHomePath(roleCode) {
  if (roleCode === ROLE_ADMIN) return '/admin/dashboard'
  if (roleCode === ROLE_WORKER) return '/worker/orders'
  return '/'
}

export function resolveConsoleEntry(roleCode) {
  if (roleCode === ROLE_ADMIN) {
    return { label: '进入后台', path: '/admin/dashboard' }
  }
  if (roleCode === ROLE_WORKER) {
    return { label: '进入工作台', path: '/worker/orders' }
  }
  return null
}
