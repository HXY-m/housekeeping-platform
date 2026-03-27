import { request } from './http'

export function fetchWorkers(serviceName) {
  const query = serviceName ? `?serviceName=${encodeURIComponent(serviceName)}` : ''
  return request(`/api/workers${query}`)
}

export function fetchWorker(id) {
  return request(`/api/workers/${id}`)
}
