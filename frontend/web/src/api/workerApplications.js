import { request } from './http'

export function submitWorkerApplication(payload) {
  return request('/api/worker-applications', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function fetchMyWorkerApplications() {
  return request('/api/worker-applications/my')
}

export function fetchAdminWorkerApplications() {
  return request('/api/worker-applications/admin')
}

export function reviewWorkerApplication(id, payload) {
  return request(`/api/worker-applications/admin/${id}/review`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}
