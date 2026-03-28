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

export function fetchFavoriteWorkers(params) {
  if (!params) {
    return request(`/api/favorites/workers${buildQuery({ current: 1, size: 100 })}`).then((result) => result?.records || [])
  }
  return request(`/api/favorites/workers${buildQuery(params)}`)
}

export function fetchFavoriteWorkerIds() {
  return request('/api/favorites/workers/ids')
}

export function favoriteWorker(workerId) {
  return request(`/api/favorites/workers/${workerId}`, {
    method: 'POST'
  })
}

export function unfavoriteWorker(workerId) {
  return request(`/api/favorites/workers/${workerId}`, {
    method: 'DELETE'
  })
}
