export function formatearMoneda(monto) {
  const numero = Number(monto || 0)
  return new Intl.NumberFormat('es-MX', {
    style: 'currency',
    currency: 'MXN'
  }).format(numero)
}

export function formatearFechaHora(iso) {
  if (!iso) return ''
  const fecha = new Date(iso)
  return fecha.toLocaleString('es-MX', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export function formatearHora(iso) {
  if (!iso) return ''
  const fecha = new Date(iso)
  return fecha.toLocaleTimeString('es-MX', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

export function etiquetaEstadoPedido(estado) {
  const mapa = {
    recibido: 'Recibido',
    en_preparacion: 'En preparación',
    listo_para_recoger: 'Listo para recoger',
    entregado: 'Entregado',
    no_reclamado: 'No reclamado',
    cancelado: 'Cancelado'
  }
  return mapa[estado] || estado
}

export function etiquetaModalidadPago(modalidad) {
  return modalidad === 'app_tarjeta' ? 'Tarjeta en app' : 'En mostrador'
}

export function etiquetaEstadoPago(estado) {
  const mapa = {
    pendiente_mostrador: 'Pendiente en mostrador',
    retenido: 'Cobro retenido',
    cobrado: 'Cobrado',
    liberado: 'Retención liberada',
    cancelado: 'Cancelado'
  }
  return mapa[estado] || estado
}

export function etiquetaTurno(turno) {
  const mapa = {
    matutino: 'Matutino',
    vespertino: 'Vespertino',
    ambos: 'Ambos turnos'
  }
  return mapa[turno] || turno
}
