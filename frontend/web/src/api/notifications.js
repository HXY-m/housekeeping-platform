import { request } from './http'

export function fetchNotifications() {
  return request('/api/notifications')
}

export function fetchUnreadNotificationCount() {
  return request('/api/notifications/unread-count')
}

export function markNotificationAsRead(id) {
  return request(`/api/notifications/${id}/read`, {
    method: 'POST'
  })
}

export function markAllNotificationsAsRead() {
  return request('/api/notifications/read-all', {
    method: 'POST'
  })
}
