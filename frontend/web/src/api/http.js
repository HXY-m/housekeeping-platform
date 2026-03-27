const TOKEN_KEY = 'housekeeping_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

function buildHeaders(body, extraHeaders = {}) {
  const headers = { ...extraHeaders }

  if (!(body instanceof FormData) && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json'
  }

  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  return headers
}

export async function request(url, options = {}) {
  const response = await fetch(url, {
    headers: buildHeaders(options.body, options.headers),
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
