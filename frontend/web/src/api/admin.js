import { request } from './http'

export function fetchAdminDashboard() {
  return request('/api/admin/dashboard')
}

export function fetchAdminOrders() {
  return request('/api/admin/orders')
}

export function fetchAdminUsers(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) {
      search.set(key, value)
    }
  })
  const query = search.toString()
  return request(`/api/admin/users${query ? `?${query}` : ''}`)
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
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) {
      search.set(key, value)
    }
  })
  const query = search.toString()
  return request(`/api/admin/categories${query ? `?${query}` : ''}`)
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
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) {
      search.set(key, value)
    }
  })
  const query = search.toString()
  return request(`/api/admin/operation-logs${query ? `?${query}` : ''}`)
}
