import { request } from './http'

export function fetchFavoriteWorkers() {
  return request('/api/favorites/workers')
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
