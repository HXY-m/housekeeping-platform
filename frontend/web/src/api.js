export { clearToken, getToken, hasToken, request, setToken } from './api/http'
export { fetchHome } from './api/home'
export { fetchWorkers, fetchWorker } from './api/workers'
export {
  fetchOrders,
  createOrder,
  submitOrderReview,
  fetchWorkerOrders,
  acceptWorkerOrder,
  startWorkerOrder,
  completeWorkerOrder
} from './api/orders'
export { fetchAdminDashboard } from './api/admin'
export {
  fetchMyAfterSales,
  createAfterSale,
  uploadAfterSaleAttachment,
  fetchAdminAfterSales,
  handleAdminAfterSale
} from './api/afterSales'
export { uploadImage } from './api/upload'
export {
  fetchUserProfile,
  updateUserProfile,
  fetchUserAddresses,
  createUserAddress,
  updateUserAddress,
  deleteUserAddress
} from './api/user'
export {
  login,
  register,
  fetchCurrentUser,
  fetchDemoAccounts
} from './api/auth'
export {
  submitWorkerApplication,
  fetchMyWorkerApplications,
  fetchAdminWorkerApplications,
  reviewWorkerApplication
} from './api/workerApplications'
