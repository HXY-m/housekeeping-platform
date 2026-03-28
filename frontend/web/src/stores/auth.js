import { reactive } from 'vue'
import { clearToken, fetchCurrentUser, hasToken, setToken } from '../api'
import { resolveHomePath } from '../utils/auth'

const state = reactive({
  token: localStorage.getItem('housekeeping_token') || '',
  user: null,
  loaded: false
})

function extractPermissionCodes(value) {
  const source = Array.isArray(value) ? value : value ? [value] : []
  return source
    .map((item) => {
      if (typeof item === 'string') {
        return item.trim()
      }
      if (item && typeof item === 'object') {
        return String(
          item.permissionCode ||
            item.code ||
            item.key ||
            item.value ||
            item.id ||
            item.name ||
            ''
        ).trim()
      }
      return ''
    })
    .filter(Boolean)
}

function readPermissionSnapshot(user) {
  if (!user) {
    return { available: false, codes: [] }
  }

  const knownKeys = ['permissionCodes', 'permissions', 'permissionList', 'authorities']
  for (const key of knownKeys) {
    if (Object.prototype.hasOwnProperty.call(user, key)) {
      return {
        available: true,
        codes: extractPermissionCodes(user[key])
      }
    }
  }

  return { available: false, codes: [] }
}

async function loadCurrentUser() {
  if (!hasToken()) {
    state.token = ''
    state.user = null
    state.loaded = true
    return null
  }

  try {
    const user = await fetchCurrentUser()
    state.token = localStorage.getItem('housekeeping_token') || ''
    state.user = user
    state.loaded = true
    return user
  } catch {
    logout()
    state.loaded = true
    return null
  }
}

export const authStore = {
  state,
  async ensureLoaded() {
    if (state.loaded) {
      return state.user
    }
    return loadCurrentUser()
  },
  async refresh() {
    return loadCurrentUser()
  },
  setUserSession(payload) {
    setToken(payload.token)
    state.token = payload.token
    state.user = payload.user
    state.loaded = true
  },
  loginSuccess(payload) {
    this.setUserSession(payload)
  },
  logout() {
    logout()
  },
  isLoggedIn() {
    return Boolean(state.token && state.user)
  },
  hasRole(roleCode) {
    return state.user?.roleCode === roleCode
  },
  getPermissionCodes() {
    return readPermissionSnapshot(state.user).codes
  },
  hasPermission(permissionCode) {
    const snapshot = readPermissionSnapshot(state.user)
    if (!snapshot.available) {
      return true
    }
    return snapshot.codes.includes(permissionCode)
  },
  hasAnyPermission(permissionCodes = []) {
    const snapshot = readPermissionSnapshot(state.user)
    if (!snapshot.available) {
      return true
    }
    const required = Array.isArray(permissionCodes) ? permissionCodes : [permissionCodes]
    return required.some((permissionCode) => snapshot.codes.includes(permissionCode))
  },
  hasPermissionData() {
    return readPermissionSnapshot(state.user).available
  },
  resolveHomePath(roleCode = state.user?.roleCode) {
    return resolveHomePath(roleCode)
  }
}

function logout() {
  clearToken()
  state.token = ''
  state.user = null
}
