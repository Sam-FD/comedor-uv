import { io } from 'socket.io-client'
import { ref } from 'vue'
import { obtenerSocketUrl } from '../utils/entorno'

export const socketConectado = ref(false)
export const socketDisponible = ref(Boolean(obtenerSocketUrl()))

let socket = null

export function conectarSocket() {
  const urlSocket = obtenerSocketUrl()
  socketDisponible.value = Boolean(urlSocket)
  if (!urlSocket) {
    return null
  }

  if (socket) {
    unirseASalaActual()
    return socket
  }

  socket = io(urlSocket, {
    transports: ['websocket', 'polling'],
    reconnection: true,
    reconnectionDelay: 1500
  })

  socket.on('connect', () => {
    socketConectado.value = true
    unirseASalaActual()
  })

  socket.on('disconnect', () => {
    socketConectado.value = false
  })

  return socket
}

export function unirseASalaActual() {
  if (!socket || !socket.connected) {
    return
  }
  const token = localStorage.getItem('comedor_token')
  if (token) {
    socket.emit('unirse_sala', { token })
  }
}

export function suscribirEvento(evento, callback) {
  const instancia = conectarSocket()
  if (!instancia) {
    return () => {}
  }
  instancia.on(evento, callback)
  return () => {
    instancia.off(evento, callback)
  }
}

export function desconectarSocket() {
  if (socket) {
    socket.disconnect()
    socket = null
    socketConectado.value = false
  }
}
