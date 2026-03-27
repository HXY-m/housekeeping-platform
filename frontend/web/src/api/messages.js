import { request } from './http'

export function fetchOrderConversations() {
  return request('/api/communications/orders')
}

export function fetchOrderMessages(orderId) {
  return request(`/api/communications/orders/${orderId}`)
}

export function sendOrderMessage(orderId, payload) {
  return request(`/api/communications/orders/${orderId}`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}
