import { reactive } from 'vue'
import { clearToken, fetchCurrentUser, hasToken, setToken } from '../api'
import { resolveHomePath } from '../utils/auth'

const state = reactive({
  token: localStorage.getItem('housekeeping_token') || '',
  user: null,
  loaded: false
})

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
  resolveHomePath(roleCode = state.user?.roleCode) {
    return resolveHomePath(roleCode)
  }
}

function logout() {
  clearToken()
  state.token = ''
  state.user = null
}
