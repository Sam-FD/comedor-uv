import axios from 'axios'
import { obtenerApiBaseUrl } from '../utils/entorno'

const api = axios.create({
  baseURL: obtenerApiBaseUrl(),
  timeout: 45000,
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
      } else if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    return Promise.reject(error)
  }
)

export function extraerMensajeError(error, respaldo = 'Ocurrió un error inesperado') {
  if (error?.response?.data?.mensaje) {
    return error.response.data.mensaje
  }
  if (error?.code === 'ECONNABORTED' || /timeout/i.test(error?.message || '')) {
    return 'El servidor está tardando en responder. Intenta nuevamente en unos segundos.'
  }
  if (!error?.response) {
    return 'No se pudo conectar con el servidor. Verifica tu conexión a internet e intenta de nuevo.'
  }
  return error?.message || respaldo
}

export default api
