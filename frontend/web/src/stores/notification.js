import { reactive } from 'vue'
import {
  fetchNotifications,
  fetchUnreadNotificationCount,
  markAllNotificationsAsRead,
  markNotificationAsRead
} from '../api/notifications'

const SUPPORTED_PORTALS = ['user', 'worker', 'admin']

const state = reactive({
  activePortal: 'public',
  itemsByPortal: {
    user: [],
    worker: [],
    admin: []
  },
  unreadCountByPortal: {
    user: 0,
    worker: 0,
    admin: 0
  },
  loadingByPortal: {
    user: false,
    worker: false,
    admin: false
  },
  loadedAtByPortal: {
    user: 0,
    worker: 0,
    admin: 0
  }
})

function isSupportedPortal(portal) {
  return SUPPORTED_PORTALS.includes(portal)
}

function sortNotifications(items = []) {
  return [...items].sort((left, right) => String(right.createdAt || '').localeCompare(String(left.createdAt || '')))
}

export const notificationStore = {
  state,
  setActivePortal(portal) {
    state.activePortal = portal || 'public'
  },
  getItems(portal = state.activePortal) {
    return state.itemsByPortal[portal] || []
  },
  getUnreadCount(portal = state.activePortal) {
    return Number(state.unreadCountByPortal[portal] || 0)
  },
  isLoading(portal = state.activePortal) {
    return Boolean(state.loadingByPortal[portal])
  },
  async ensureLoaded(portal = state.activePortal, { force = false } = {}) {
    if (!isSupportedPortal(portal)) {
      return []
    }

    const justLoaded = Date.now() - Number(state.loadedAtByPortal[portal] || 0) < 15000
    if (!force && justLoaded && state.itemsByPortal[portal].length) {
      return state.itemsByPortal[portal]
    }

    state.loadingByPortal[portal] = true
    try {
      const [items, unreadCount] = await Promise.all([
        fetchNotifications(),
        fetchUnreadNotificationCount()
      ])
      state.itemsByPortal[portal] = sortNotifications(items)
      state.unreadCountByPortal[portal] = unreadCount
      state.loadedAtByPortal[portal] = Date.now()
      return state.itemsByPortal[portal]
    } finally {
      state.loadingByPortal[portal] = false
    }
  },
  async refresh(portal = state.activePortal) {
    return this.ensureLoaded(portal, { force: true })
  },
  async markRead(portal, id) {
    if (!isSupportedPortal(portal)) {
      return
    }
    const items = state.itemsByPortal[portal] || []
    const current = items.find((item) => item.id === id)
    if (!current || current.read) {
      return
    }

    state.itemsByPortal[portal] = items.map((item) => (item.id === id ? { ...item, read: true } : item))
    state.unreadCountByPortal[portal] = Math.max(0, this.getUnreadCount(portal) - 1)
    try {
      await markNotificationAsRead(id)
    } catch (error) {
      state.itemsByPortal[portal] = items
      state.unreadCountByPortal[portal] = this.getUnreadCount(portal) + 1
      throw error
    }
  },
  async markAllRead(portal = state.activePortal) {
    if (!isSupportedPortal(portal)) {
      return
    }
    const items = state.itemsByPortal[portal] || []
    if (!items.length) {
      return
    }

    const previousUnread = this.getUnreadCount(portal)
    state.itemsByPortal[portal] = items.map((item) => ({ ...item, read: true }))
    state.unreadCountByPortal[portal] = 0
    try {
      await markAllNotificationsAsRead()
    } catch (error) {
      state.itemsByPortal[portal] = items
      state.unreadCountByPortal[portal] = previousUnread
      throw error
    }
  },
  reset() {
    SUPPORTED_PORTALS.forEach((portal) => {
      state.itemsByPortal[portal] = []
      state.unreadCountByPortal[portal] = 0
      state.loadingByPortal[portal] = false
      state.loadedAtByPortal[portal] = 0
    })
    state.activePortal = 'public'
  }
}
