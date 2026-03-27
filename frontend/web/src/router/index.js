import { createRouter, createWebHistory } from 'vue-router'
import { authStore } from '../stores/auth'
import PublicLayout from '../layouts/PublicLayout.vue'
import AuthLayout from '../layouts/AuthLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import WorkerLayout from '../layouts/WorkerLayout.vue'
import HomeView from '../views/consumer/HomeView.vue'
import WorkersView from '../views/consumer/WorkersView.vue'
import WorkerDetailView from '../views/consumer/WorkerDetailView.vue'
import BookingView from '../views/consumer/BookingView.vue'
import OrdersView from '../views/consumer/OrdersView.vue'
import LoginView from '../views/auth/LoginView.vue'
import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
import AdminApplicationsView from '../views/admin/AdminApplicationsView.vue'
import WorkerApplyView from '../views/worker/WorkerApplyView.vue'
import WorkerOrdersView from '../views/worker/WorkerOrdersView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: PublicLayout,
      children: [
        { path: '', component: HomeView },
        { path: 'workers', component: WorkersView },
        { path: 'workers/:id', component: WorkerDetailView },
        {
          path: 'booking/:workerId',
          component: BookingView,
          meta: { requiresAuth: true, role: 'USER' }
        },
        {
          path: 'orders',
          component: OrdersView,
          meta: { requiresAuth: true, role: 'USER' }
        },
        {
          path: 'worker/apply',
          component: WorkerApplyView,
          meta: { requiresAuth: true, role: 'USER' }
        }
      ]
    },
    {
      path: '/login',
      component: AuthLayout,
      children: [{ path: '', component: LoginView }]
    },
    {
      path: '/worker',
      component: WorkerLayout,
      meta: { requiresAuth: true, role: 'WORKER' },
      redirect: '/worker/orders',
      children: [{ path: 'orders', component: WorkerOrdersView }]
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, role: 'ADMIN' },
      redirect: '/admin/dashboard',
      children: [
        { path: 'dashboard', component: AdminDashboardView },
        { path: 'applications', component: AdminApplicationsView }
      ]
    }
  ]
})

function defaultHomePath() {
  if (authStore.hasRole('ADMIN')) return '/admin/dashboard'
  if (authStore.hasRole('WORKER')) return '/worker/orders'
  return '/'
}

router.beforeEach(async (to) => {
  await authStore.ensureLoaded()

  if (to.path === '/login' && authStore.isLoggedIn()) {
    return defaultHomePath()
  }

  if (!to.meta.requiresAuth) {
    return true
  }

  if (!authStore.isLoggedIn()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.role && !authStore.hasRole(to.meta.role)) {
    return { path: '/login', query: { redirect: to.fullPath, error: 'no_permission' } }
  }

  return true
})

export default router
