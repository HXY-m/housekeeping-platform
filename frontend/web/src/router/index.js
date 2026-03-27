import { createRouter, createWebHistory } from 'vue-router'
import { authStore } from '../stores/auth'
import HomeView from '../views/HomeView.vue'
import WorkersView from '../views/WorkersView.vue'
import WorkerDetailView from '../views/WorkerDetailView.vue'
import BookingView from '../views/BookingView.vue'
import OrdersView from '../views/OrdersView.vue'
import AdminView from '../views/AdminView.vue'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/login', component: LoginView },
    { path: '/workers', component: WorkersView },
    { path: '/workers/:id', component: WorkerDetailView },
    { path: '/booking/:workerId', component: BookingView },
    { path: '/orders', component: OrdersView },
    {
      path: '/admin',
      component: AdminView,
      meta: {
        requiresAuth: true,
        role: 'ADMIN'
      }
    }
  ]
})

router.beforeEach(async (to) => {
  await authStore.ensureLoaded()

  if (!to.meta.requiresAuth) {
    return true
  }

  if (!authStore.isLoggedIn()) {
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }

  if (to.meta.role && !authStore.hasRole(to.meta.role)) {
    return {
      path: '/login',
      query: { redirect: to.fullPath, error: 'no_permission' }
    }
  }

  return true
})

export default router
