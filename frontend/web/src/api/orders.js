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

export function fetchBookingAvailability(workerId, bookingDate) {
  const query = new URLSearchParams({
    workerId: String(workerId),
    bookingDate
  })
  return request(`/api/orders/availability?${query.toString()}`)
}

export function confirmUserOrder(id) {
  return request(`/api/orders/${id}/confirm`, {
    method: 'POST'
  })
}

export function confirmUserOrderCompletion(id) {
  return request(`/api/orders/${id}/confirm-completion`, {
    method: 'POST'
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

export function uploadWorkerServiceRecord(id, payload) {
  const formData = new FormData()
  formData.append('stage', payload.stage)
  formData.append('description', payload.description)
  if (payload.file) {
    formData.append('file', payload.file)
  }

  return request(`/api/worker/orders/${id}/service-records`, {
    method: 'POST',
    body: formData
  })
}
