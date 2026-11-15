import { reactive } from 'vue'
import api, { registrarManejadorNoAutorizado } from '../services/api'
import { conectarSocket, desconectarSocket, unirseASalaActual } from '../services/socket'

function leerUsuarioGuardado() {
  try {
    const raw = localStorage.getItem('comedor_usuario')
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const sesion = reactive({
  token: localStorage.getItem('comedor_token') || '',
  usuario: leerUsuarioGuardado()
})

export function guardarSesion(token, usuario) {
  sesion.token = token
  sesion.usuario = usuario
  localStorage.setItem('comedor_token', token)
  localStorage.setItem('comedor_usuario', JSON.stringify(usuario))
  conectarSocket()
  unirseASalaActual()
}

export function actualizarUsuarioEnSesion(usuario) {
  sesion.usuario = usuario
  localStorage.setItem('comedor_usuario', JSON.stringify(usuario))
}

export function cerrarSesion() {
  sesion.token = ''
  sesion.usuario = null
  localStorage.removeItem('comedor_token')
  localStorage.removeItem('comedor_usuario')
  desconectarSocket()
}

registrarManejadorNoAutorizado(cerrarSesion)

export async function refrescarPerfil() {
  if (!sesion.token) {
    return null
  }
  try {
    const { data } = await api.get('/auth/me')
    actualizarUsuarioEnSesion(data)
    return data
  } catch (err) {
    if (err?.response?.status === 401) {
      cerrarSesion()
    }
    return null
  }
}
