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

export function fetchWorkers(params = {}) {
  return request(`/api/workers${buildQuery(params)}`)
}

export function fetchWorker(id) {
  return request(`/api/workers/${id}`)
}

export function fetchCurrentWorkerProfile() {
  return request('/api/worker/profile/me')
}
