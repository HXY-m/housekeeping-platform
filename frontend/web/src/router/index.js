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
          meta: { requiresAuth: true, role: 'USER', permission: 'USER_ORDER_USE', surface: 'public' }
        },
        { path: 'orders', redirect: '/user/orders' },
        { path: 'worker/apply', redirect: '/register?roleCode=WORKER' }
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
        { path: 'dashboard', component: () => import('../views/user/UserDashboardView.vue'), meta: { permission: 'USER_DASHBOARD_VIEW' } },
        { path: 'profile', component: () => import('../views/user/UserProfileView.vue'), meta: { permission: 'USER_PROFILE_MANAGE' } },
        { path: 'favorites', component: () => import('../views/user/UserFavoritesView.vue'), meta: { permission: 'USER_FAVORITE_MANAGE' } },
        { path: 'messages', component: () => import('../views/user/UserMessagesView.vue'), meta: { permission: 'USER_MESSAGE_USE' } },
        { path: 'orders', component: () => import('../views/consumer/OrdersView.vue'), meta: { permission: 'USER_ORDER_USE' } }
      ]
    },
    {
      path: '/worker',
      component: WorkerLayout,
      meta: { requiresAuth: true, role: 'WORKER', surface: 'worker' },
      redirect: '/worker/dashboard',
      children: [
        { path: 'dashboard', component: () => import('../views/worker/WorkerDashboardView.vue'), meta: { permission: 'WORKER_DASHBOARD_VIEW' } },
        { path: 'orders', component: () => import('../views/worker/WorkerOrdersView.vue'), meta: { permission: 'WORKER_ORDER_HANDLE' } },
        { path: 'messages', component: () => import('../views/worker/WorkerMessagesView.vue'), meta: { permission: 'WORKER_MESSAGE_USE' } },
        { path: 'qualification', component: () => import('../views/worker/WorkerApplyView.vue'), meta: { permission: 'WORKER_QUALIFICATION_SUBMIT' } }
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, role: 'ADMIN', surface: 'admin' },
      redirect: '/admin/dashboard',
      children: [
        { path: 'dashboard', component: () => import('../views/admin/AdminDashboardView.vue'), meta: { permission: 'ADMIN_DASHBOARD_VIEW' } },
        { path: 'messages', component: () => import('../views/admin/AdminMessagesView.vue'), meta: { permission: 'ADMIN_MESSAGE_VIEW' } },
        { path: 'orders', component: () => import('../views/admin/AdminOrdersView.vue'), meta: { permission: 'ADMIN_ORDER_MANAGE' } },
        { path: 'users', component: () => import('../views/admin/AdminUsersView.vue'), meta: { permission: 'ADMIN_USER_MANAGE' } },
        { path: 'services', component: () => import('../views/admin/AdminServicesView.vue'), meta: { permission: 'ADMIN_SERVICE_MANAGE' } },
        { path: 'applications', component: () => import('../views/admin/AdminApplicationsView.vue'), meta: { permission: 'ADMIN_APPLICATION_REVIEW' } },
        { path: 'after-sales', component: () => import('../views/admin/AdminAfterSalesView.vue'), meta: { permission: 'ADMIN_AFTER_SALE_MANAGE' } },
        { path: 'operation-logs', component: () => import('../views/admin/AdminOperationLogsView.vue'), meta: { permission: 'ADMIN_OPERATION_LOG_VIEW' } },
        { path: 'reports', component: () => import('../views/admin/AdminReportsView.vue'), meta: { permission: 'ADMIN_REPORT_EXPORT' } },
        {
          path: 'permissions',
          component: () => import('../views/admin/AdminPermissionsView.vue'),
          meta: { permission: 'ADMIN_PERMISSION_MANAGE' }
        }
      ]
    }
  ]
})

function isAuthPage(path) {
  return path === '/login' || path === '/register'
}

function getRequiredPermissions(meta) {
  if (!meta) {
    return []
  }
  if (Array.isArray(meta.permissions)) {
    return meta.permissions.filter(Boolean)
  }
  if (meta.permission) {
    return [meta.permission]
  }
  return []
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

  const requiredPermissions = getRequiredPermissions(to.meta)
  if (requiredPermissions.length && authStore.hasPermissionData()) {
    const hasAccess = requiredPermissions.some((permissionCode) => authStore.hasPermission(permissionCode))
    if (!hasAccess) {
      return { path: '/login', query: { redirect: to.fullPath, error: 'no_permission' } }
    }
  }

  return true
})

export default router
