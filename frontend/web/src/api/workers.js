import { request } from './http'
import { buildQuery } from './query'

export function fetchWorkers(params = {}) {
  return request(`/api/workers${buildQuery(params)}`)
}

export function fetchWorker(id) {
  return request(`/api/workers/${id}`)
}

export function fetchCurrentWorkerProfile() {
  return request('/api/worker/profile/me')
}

export function fetchWorkerDashboard() {
  return request('/api/worker/dashboard')
}
