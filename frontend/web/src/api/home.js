import { request } from './http'

export function fetchHome() {
  return request('/api/home')
}

export function fetchServiceCategories() {
  return request('/api/categories')
}
