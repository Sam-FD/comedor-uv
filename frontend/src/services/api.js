import axios from 'axios'
import { esAppMovil, obtenerApiBaseUrl } from '../utils/entorno'

const api = axios.create({
  baseURL: obtenerApiBaseUrl(),
  headers: {
    'Content-Type': 'application/json'
  }
})

let manejadorNoAutorizado = null
let manejadorRedireccionLogin = null

export function registrarManejadorNoAutorizado(fn) {
  manejadorNoAutorizado = fn
}

export function registrarRedireccionLogin(fn) {
  manejadorRedireccionLogin = fn
}

api.interceptors.request.use((config) => {
  config.baseURL = obtenerApiBaseUrl()
  const token = localStorage.getItem('comedor_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const url = error?.config?.url || ''
    const esEndpointAuth = url.includes('/auth/login') || url.includes('/auth/registro')

    if (status === 401 && !esEndpointAuth) {
      localStorage.removeItem('comedor_token')
      localStorage.removeItem('comedor_usuario')
      if (typeof manejadorNoAutorizado === 'function') {
        manejadorNoAutorizado()
      }
      if (typeof manejadorRedireccionLogin === 'function') {
        manejadorRedireccionLogin()
      } else if (esAppMovil()) {
        if (!window.location.hash.includes('/login')) {
          window.location.hash = '#/login'
        }
      } else if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    return Promise.reject(error)
  }
)

export function extraerMensajeError(error, respaldo = 'Ocurrió un error inesperado') {
  return error?.response?.data?.mensaje || error?.message || respaldo
}

export default api
