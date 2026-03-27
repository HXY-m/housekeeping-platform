import { request } from './http'

export function fetchUserProfile() {
  return request('/api/user/profile')
}

export function updateUserProfile(payload) {
  return request('/api/user/profile', {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function fetchUserAddresses() {
  return request('/api/user/addresses')
}

export function createUserAddress(payload) {
  return request('/api/user/addresses', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function updateUserAddress(id, payload) {
  return request(`/api/user/addresses/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function deleteUserAddress(id) {
  return request(`/api/user/addresses/${id}`, {
    method: 'DELETE'
  })
}
