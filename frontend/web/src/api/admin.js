import { request } from './http'

export function fetchAdminDashboard() {
  return request('/api/admin/dashboard')
}
