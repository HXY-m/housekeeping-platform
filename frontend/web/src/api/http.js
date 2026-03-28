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

function buildErrorMessage(status, result) {
  return result?.message || `请求失败：${status}`
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
    throw new Error(buildErrorMessage(response.status, result))
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

export async function downloadFile(url, options = {}) {
  const response = await fetch(url, {
    headers: buildHeaders(options.body, options.headers),
    ...options
  })

  if (!response.ok) {
    let result = null
    try {
      result = await response.json()
    } catch {
      result = null
    }
    throw new Error(buildErrorMessage(response.status, result))
  }

  const blob = await response.blob()
  const disposition = response.headers.get('content-disposition') || ''
  const matchedName = disposition.match(/filename\*=UTF-8''([^;]+)|filename=\"?([^\";]+)\"?/)
  const fileName = decodeURIComponent(matchedName?.[1] || matchedName?.[2] || options.fileName || 'report.csv')

  const objectUrl = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(objectUrl)

  return fileName
}
