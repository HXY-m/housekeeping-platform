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
