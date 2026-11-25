import { Capacitor } from '@capacitor/core'

export function esAppMovil() {
  return Capacitor.isNativePlatform()
}

export function obtenerApiBaseUrl() {
  const envUrl = import.meta.env.VITE_API_URL?.trim()
  if (envUrl) {
    const limpia = envUrl.replace(/\/+$/, '')
    return /\/api$/i.test(limpia) ? limpia : `${limpia}/api`
  }
  return '/api'
}

export function obtenerSocketUrl() {
  const envSocket = import.meta.env.VITE_SOCKET_URL?.trim()
  if (envSocket) {
    return envSocket.replace(/\/+$/, '')
  }
  const envApi = import.meta.env.VITE_API_URL?.trim()
  if (envApi) {
    return new URL(envApi).origin
  }
  return 'http://localhost:9092'
}
