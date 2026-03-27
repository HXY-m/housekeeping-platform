import { request } from './http'

export function login(payload) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function register(payload) {
  return request('/api/auth/register', {
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
