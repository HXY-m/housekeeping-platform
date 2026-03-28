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

export function fetchOrders(params) {
  if (!params) {
    return request(`/api/orders${buildQuery({ current: 1, size: 100 })}`).then((result) => result?.records || [])
  }
  return request(`/api/orders${buildQuery(params)}`)
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

export function fetchWorkerOrders(params) {
  if (!params) {
    return request(`/api/worker/orders${buildQuery({ current: 1, size: 100 })}`).then((result) => result?.records || [])
  }
  return request(`/api/worker/orders${buildQuery(params)}`)
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
