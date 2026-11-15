import { Capacitor } from '@capacitor/core'

const CLAVE_SERVIDOR_MOVIL = 'comedor_servidor_url'
const RESPUESTO_MOVIL_POR_DEFECTO = 'http://127.0.0.1:8080'

export function esAppMovil() {
  return Capacitor.isNativePlatform()
}

export function esModoDemo() {
  return import.meta.env.VITE_MODO_DEMO === 'true'
}

export function obtenerServidorMovilGuardado() {
  return localStorage.getItem(CLAVE_SERVIDOR_MOVIL) || ''
}

export function normalizarServidorMovil(url) {
  const sinEspacios = (url || '').trim()
  if (!sinEspacios) {
    return { ok: true, url: '' }
  }
  const conProtocolo = /^https?:\/\//i.test(sinEspacios) ? sinEspacios : `http://${sinEspacios}`
  const limpia = conProtocolo.replace(/\/+$/, '').replace(/\/api$/i, '')
  try {
    const parsed = new URL(limpia)
    if (!['http:', 'https:'].includes(parsed.protocol) || !parsed.hostname) {
      return { ok: false, error: 'Ingresa una URL válida (ej. http://192.168.1.50:8080)' }
    }
    return { ok: true, url: parsed.origin }
  } catch {
    return { ok: false, error: 'Ingresa una URL válida (ej. http://192.168.1.50:8080)' }
  }
}

export function guardarServidorMovil(url) {
  const resultado = normalizarServidorMovil(url)
  if (!resultado.ok) {
    return resultado
  }
  if (resultado.url) {
    localStorage.setItem(CLAVE_SERVIDOR_MOVIL, resultado.url)
  } else {
    localStorage.removeItem(CLAVE_SERVIDOR_MOVIL)
  }
  return resultado
}

export function obtenerServidorMovilEfectivo() {
  const guardado = obtenerServidorMovilGuardado()
  if (guardado) {
    return guardado
  }
  if (import.meta.env.VITE_API_URL) {
    const normalizado = normalizarServidorMovil(import.meta.env.VITE_API_URL)
    if (normalizado.ok && normalizado.url) {
      return normalizado.url
    }
  }
  return RESPUESTO_MOVIL_POR_DEFECTO
}

export function obtenerApiBaseUrl() {
  if (esAppMovil()) {
    const guardado = obtenerServidorMovilGuardado()
    if (guardado) {
      return `${guardado}/api`
    }
  }
  if (import.meta.env.VITE_API_URL) {
    const limpio = import.meta.env.VITE_API_URL.trim().replace(/\/+$/, '')
    return /\/api$/i.test(limpio) ? limpio : `${limpio}/api`
  }
  if (esAppMovil()) {
    return `${obtenerServidorMovilEfectivo()}/api`
  }
  return '/api'
}

export function obtenerSocketUrl() {
  if (import.meta.env.VITE_SOCKET_ENABLED === 'false') {
    return ''
  }
  if (import.meta.env.VITE_SOCKET_URL) {
    return import.meta.env.VITE_SOCKET_URL.replace(/\/+$/, '')
  }
  if (esAppMovil() || import.meta.env.VITE_API_URL) {
    const base = esAppMovil() ? obtenerServidorMovilEfectivo() : obtenerApiBaseUrl()
    try {
      const url = new URL(base)
      if (url.protocol === 'https:' || (url.port && url.port !== '8080')) {
        return url.origin
      }
      return `${url.protocol}//${url.hostname}:9092`
    } catch {
      return esAppMovil() ? 'http://127.0.0.1:9092' : ''
    }
  }
  return 'http://localhost:9092'
}

export function haySocketConfigurado() {
  return Boolean(obtenerSocketUrl())
}
