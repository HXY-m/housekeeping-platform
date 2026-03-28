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

export function fetchMyAfterSales() {
  return request('/api/after-sales/my')
}

export function createAfterSale(payload) {
  return request('/api/after-sales', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function uploadAfterSaleAttachment(id, file) {
  const formData = new FormData()
  formData.append('file', file)

  return request(`/api/after-sales/${id}/attachments`, {
    method: 'POST',
    body: formData
  })
}

export function fetchAdminAfterSales(params = {}) {
  return request(`/api/admin/after-sales${buildQuery(params)}`)
}

export function handleAdminAfterSale(id, payload) {
  return request(`/api/admin/after-sales/${id}/handle`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}
