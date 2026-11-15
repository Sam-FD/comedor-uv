import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router'
import { registrarRedireccionLogin } from '../services/api'
import { sesion } from '../stores/sesion'
import { esAppMovil } from '../utils/entorno'
import LoginView from '../views/LoginView.vue'
import CatalogoEstudianteView from '../views/CatalogoEstudianteView.vue'
import MisPedidosView from '../views/MisPedidosView.vue'
import PanelPersonalView from '../views/PanelPersonalView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { publica: true }
  },
  {
    path: '/',
    name: 'catalogo',
    component: CatalogoEstudianteView,
    meta: { rol: 'estudiante' }
  },
  {
    path: '/mis-pedidos',
    name: 'mis-pedidos',
    component: MisPedidosView,
    meta: { rol: 'estudiante' }
  },
  {
    path: '/personal',
    name: 'personal',
    component: PanelPersonalView,
    meta: { rol: 'personal' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: esAppMovil() ? createWebHashHistory() : createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const autenticado = Boolean(sesion.token && sesion.usuario)

  if (!to.meta.publica && !autenticado) {
    return { name: 'login' }
  }

  if (to.name === 'login' && autenticado) {
    return sesion.usuario.rol === 'personal' ? { name: 'personal' } : { name: 'catalogo' }
  }

  if (to.meta.rol && sesion.usuario && to.meta.rol !== sesion.usuario.rol) {
    return sesion.usuario.rol === 'personal' ? { name: 'personal' } : { name: 'catalogo' }
  }

  return true
})

registrarRedireccionLogin(() => {
  if (router.currentRoute.value.name !== 'login') {
    router.replace({ name: 'login' })
  }
})

export default router
