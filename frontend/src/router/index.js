import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '../views/Login.vue'
import ShipperDashboard from '../views/shipper/ShipperDashboard.vue'
import CarrierDashboard from '../views/carrier/CarrierDashboard.vue'
import PlatformDashboard from '../views/platform/PlatformDashboard.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/shipper', component: ShipperDashboard, meta: { requiresAuth: true, roles: ['SHIPPER_ADMIN', 'SHIPPER_STAFF'] } },
  { path: '/carrier', component: CarrierDashboard, meta: { requiresAuth: true, roles: ['CARRIER_ADMIN', 'CARRIER_DRIVER'] } },
  { path: '/platform', component: PlatformDashboard, meta: { requiresAuth: true, roles: ['PLATFORM_ADMIN'] } }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const session = JSON.parse(localStorage.getItem('yss-session') || 'null')
  if (to.meta.requiresAuth) {
    if (!session?.token) {
      return next('/login')
    }
    if (to.meta.roles && !to.meta.roles.includes(session.role)) {
      // 角色不匹配，重定向到对应的工作台
      if (session.role.startsWith('SHIPPER')) return next('/shipper')
      if (session.role.startsWith('CARRIER')) return next('/carrier')
      if (session.role === 'PLATFORM_ADMIN') return next('/platform')
      return next('/login')
    }
  }
  next()
})

export default router
