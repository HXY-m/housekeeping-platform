async function request(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json'
    },
    ...options
  })

  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`)
  }

  const result = await response.json()
  return result.data
}

export function fetchHome() {
  return request('/api/home')
}

export function fetchWorkers(serviceName) {
  const query = serviceName ? `?serviceName=${encodeURIComponent(serviceName)}` : ''
  return request(`/api/workers${query}`)
}

export function fetchWorker(id) {
  return request(`/api/workers/${id}`)
}

export function fetchOrders() {
  return request('/api/orders')
}

export function createOrder(payload) {
  return request('/api/orders', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function fetchAdminDashboard() {
  return request('/api/admin/dashboard')
}
