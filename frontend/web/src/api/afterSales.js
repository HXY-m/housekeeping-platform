import { request } from './http'

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

export function fetchAdminAfterSales() {
  return request('/api/admin/after-sales')
}

export function handleAdminAfterSale(id, payload) {
  return request(`/api/admin/after-sales/${id}/handle`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}
