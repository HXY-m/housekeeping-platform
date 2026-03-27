const TOKEN_KEY = 'housekeeping_token'

function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

function buildHeaders(extraHeaders = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...extraHeaders
  }

  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  return headers
}

async function request(url, options = {}) {
  const response = await fetch(url, {
    headers: buildHeaders(options.headers),
    ...options
  })

  let result = null
  try {
    result = await response.json()
  } catch {
    result = null
  }

  if (!response.ok) {
    throw new Error(result?.message || `Request failed: ${response.status}`)
  }

  if (result && result.success === false) {
    throw new Error(result.message || '请求失败')
  }

  return result?.data
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function hasToken() {
  return Boolean(getToken())
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

export function fetchAdminDashboard() {
  return request('/api/admin/dashboard')
}

export function login(payload) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function fetchCurrentUser() {
  return request('/api/auth/me')
}

export function fetchDemoAccounts() {
  return request('/api/auth/demo-accounts')
}

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
