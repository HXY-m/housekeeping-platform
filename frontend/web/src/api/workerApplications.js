import { request } from './http'

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

export function submitWorkerApplication(payload) {
  return request('/api/worker-applications', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function fetchMyWorkerApplications() {
  return request('/api/worker-applications/my')
}

export function fetchAdminWorkerApplications(params = {}) {
  return request(`/api/worker-applications/admin${buildQuery(params)}`)
}

export function fetchAdminWorkerApplicationSummary(params = {}) {
  return request(`/api/worker-applications/admin/summary${buildQuery(params)}`)
}

export function reviewWorkerApplication(id, payload) {
  return request(`/api/worker-applications/admin/${id}/review`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}
