import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/payment'
    },
    {
      path: '/payment',
      name: 'payment',
      component: () => import('@/views/Payment/index.vue')
    }
  ],
})

export default router
