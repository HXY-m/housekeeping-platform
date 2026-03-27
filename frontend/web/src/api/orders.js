import { request } from './http'

export function fetchOrders() {
  return request('/api/orders')
}

export function createOrder(payload) {
  return request('/api/orders', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function submitOrderReview(id, payload) {
  return request(`/api/orders/${id}/review`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function fetchWorkerOrders() {
  return request('/api/worker/orders')
}

export function acceptWorkerOrder(id) {
  return request(`/api/worker/orders/${id}/accept`, {
    method: 'POST'
  })
}

export function startWorkerOrder(id) {
  return request(`/api/worker/orders/${id}/start`, {
    method: 'POST'
  })
}

export function completeWorkerOrder(id) {
  return request(`/api/worker/orders/${id}/complete`, {
    method: 'POST'
  })
}
