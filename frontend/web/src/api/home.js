import { request } from './http'

export function fetchHome() {
  return request('/api/home')
}
