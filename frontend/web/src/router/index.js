import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import WorkersView from '../views/WorkersView.vue'
import WorkerDetailView from '../views/WorkerDetailView.vue'
import BookingView from '../views/BookingView.vue'
import OrdersView from '../views/OrdersView.vue'
import AdminView from '../views/AdminView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/workers', component: WorkersView },
    { path: '/workers/:id', component: WorkerDetailView },
    { path: '/booking/:workerId', component: BookingView },
    { path: '/orders', component: OrdersView },
    { path: '/admin', component: AdminView }
  ]
})
