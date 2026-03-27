import { createRouter, createWebHistory } from 'vue-router'
import { authStore } from '../stores/auth'

const PublicLayout = () => import('../layouts/PublicLayout.vue')
const AuthLayout = () => import('../layouts/AuthLayout.vue')
const UserLayout = () => import('../layouts/UserLayout.vue')
const WorkerLayout = () => import('../layouts/WorkerLayout.vue')
const AdminLayout = () => import('../layouts/AdminLayout.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: PublicLayout,
      meta: { surface: 'public' },
      children: [
        { path: '', component: () => import('../views/consumer/HomeView.vue') },
        { path: 'workers', component: () => import('../views/consumer/WorkersView.vue') },
        { path: 'workers/:id', component: () => import('../views/consumer/WorkerDetailView.vue') },
        {
          path: 'booking/:workerId',
          component: () => import('../views/consumer/BookingView.vue'),
          meta: { requiresAuth: true, role: 'USER', surface: 'public' }
        },
        { path: 'orders', redirect: '/user/orders' },
        { path: 'worker/apply', redirect: '/user/worker-application' }
      ]
    },
    {
      path: '/',
      component: AuthLayout,
      meta: { surface: 'auth' },
      children: [
        { path: 'login', component: () => import('../views/auth/LoginView.vue') },
        { path: 'register', component: () => import('../views/auth/RegisterView.vue') }
      ]
    },
    {
      path: '/user',
      component: UserLayout,
      meta: { requiresAuth: true, role: 'USER', surface: 'user' },
      redirect: '/user/dashboard',
      children: [
        { path: 'dashboard', component: () => import('../views/user/UserDashboardView.vue') },
        { path: 'profile', component: () => import('../views/user/UserProfileView.vue') },
        { path: 'favorites', component: () => import('../views/user/UserFavoritesView.vue') },
        { path: 'orders', component: () => import('../views/consumer/OrdersView.vue') },
        { path: 'worker-application', component: () => import('../views/worker/WorkerApplyView.vue') }
      ]
    },
    {
      path: '/worker',
      component: WorkerLayout,
      meta: { requiresAuth: true, role: 'WORKER', surface: 'worker' },
      redirect: '/worker/dashboard',
      children: [
        { path: 'dashboard', component: () => import('../views/worker/WorkerDashboardView.vue') },
        { path: 'orders', component: () => import('../views/worker/WorkerOrdersView.vue') }
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, role: 'ADMIN', surface: 'admin' },
      redirect: '/admin/dashboard',
      children: [
        { path: 'dashboard', component: () => import('../views/admin/AdminDashboardView.vue') },
        { path: 'orders', component: () => import('../views/admin/AdminOrdersView.vue') },
        { path: 'users', component: () => import('../views/admin/AdminUsersView.vue') },
        { path: 'services', component: () => import('../views/admin/AdminServicesView.vue') },
        { path: 'applications', component: () => import('../views/admin/AdminApplicationsView.vue') },
        { path: 'after-sales', component: () => import('../views/admin/AdminAfterSalesView.vue') },
        { path: 'operation-logs', component: () => import('../views/admin/AdminOperationLogsView.vue') }
      ]
    }
  ]
})

function isAuthPage(path) {
  return path === '/login' || path === '/register'
}

router.beforeEach(async (to) => {
  await authStore.ensureLoaded()

  if (isAuthPage(to.path) && authStore.isLoggedIn()) {
    return authStore.resolveHomePath()
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
