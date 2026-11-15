import { Capacitor } from '@capacitor/core'

const CLAVE_SERVIDOR_MOVIL = 'comedor_servidor_url'
const RESPUESTO_MOVIL_POR_DEFECTO = 'http://127.0.0.1:8080'

export function esAppMovil() {
  return Capacitor.isNativePlatform()
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
      return { ok: false, error: 'Ingresa una URL valida (ej. http://192.168.1.50:8080)' }
    }
    return { ok: true, url: parsed.origin }
  } catch {
    return { ok: false, error: 'Ingresa una URL valida (ej. http://192.168.1.50:8080)' }
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
  return obtenerServidorMovilGuardado() || RESPUESTO_MOVIL_POR_DEFECTO
}

export function obtenerApiBaseUrl() {
  if (import.meta.env.VITE_API_URL) {
    return import.meta.env.VITE_API_URL.replace(/\/+$/, '')
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
  if (esAppMovil()) {
    try {
      const url = new URL(obtenerServidorMovilEfectivo())
      // En servidores cloud HTTPS gratuitos de un solo puerto no existe el puerto 9092 expuesto
      if (url.protocol === 'https:') {
        return ''
      }
      return `${url.protocol}//${url.hostname}:9092`
    } catch {
      return 'http://127.0.0.1:9092'
    }
  }
  // En produccion web con VITE_API_URL y sin VITE_SOCKET_URL, operar en modo HTTP de un solo puerto
  if (import.meta.env.VITE_API_URL) {
    return ''
  }
  return 'http://localhost:9092'
}

export function haySocketConfigurado() {
  return Boolean(obtenerSocketUrl())
}
