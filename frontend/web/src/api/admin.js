import { downloadFile, request } from './http'

function buildQuery(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) {
      search.set(key, value)
    }
  })
  const query = search.toString()
  return query ? `?${query}` : ''
}

export function fetchAdminDashboard() {
  return request('/api/admin/dashboard')
}

export function fetchAdminOrders() {
  return request('/api/admin/orders')
}

export function fetchAdminUsers(params = {}) {
  return request(`/api/admin/users${buildQuery(params)}`)
}

export function createAdminUser(payload) {
  return request('/api/admin/users', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function updateAdminUser(id, payload) {
  return request(`/api/admin/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function deleteAdminUser(id) {
  return request(`/api/admin/users/${id}`, {
    method: 'DELETE'
  })
}

export function fetchAdminCategories(params = {}) {
  return request(`/api/admin/categories${buildQuery(params)}`)
}

export function createAdminCategory(payload) {
  return request('/api/admin/categories', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function updateAdminCategory(id, payload) {
  return request(`/api/admin/categories/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function deleteAdminCategory(id) {
  return request(`/api/admin/categories/${id}`, {
    method: 'DELETE'
  })
}

export function fetchAdminOperationLogs(params = {}) {
  return request(`/api/admin/operation-logs${buildQuery(params)}`)
}

export function exportAdminDashboardReport() {
  return downloadFile('/api/admin/reports/dashboard/export')
}

export function exportAdminOrdersReport(params = {}) {
  return downloadFile(`/api/admin/reports/orders/export${buildQuery(params)}`)
}

export function exportAdminUsersReport(params = {}) {
  return downloadFile(`/api/admin/reports/users/export${buildQuery(params)}`)
}

export function exportAdminAfterSalesReport(params = {}) {
  return downloadFile(`/api/admin/reports/after-sales/export${buildQuery(params)}`)
}

export function exportAdminOperationLogsReport(params = {}) {
  return downloadFile(`/api/admin/reports/operation-logs/export${buildQuery(params)}`)
}
