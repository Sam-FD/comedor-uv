<template>
  <div v-if="mostrar" class="rounded-xl border px-4 py-3.5 text-sm shadow-sm flex items-start gap-3" :class="claseContenedor">
    <svg class="w-5 h-5 shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
      <path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd" />
    </svg>
    <div>
      <p class="font-semibold">{{ titulo }}</p>
      <p class="mt-0.5 leading-relaxed">{{ mensaje }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatearFechaHora } from '../utils/formatos'

const props = defineProps({
  usuario: {
    type: Object,
    required: true
  }
})

const mostrar = computed(() => {
  if (!props.usuario || props.usuario.rol !== 'estudiante') {
    return false
  }
  return props.usuario.nivelRestriccion !== 'ninguna' || props.usuario.faltasAbandono > 0
})

const claseContenedor = computed(() => {
  const nivel = props.usuario?.nivelRestriccion
  if (nivel === 'sin_pedidos_en_linea') {
    return 'bg-red-50 border-red-200 text-red-900'
  }
  if (nivel === 'sin_mostrador') {
    return 'bg-amber-50 border-amber-300 text-amber-900'
  }
  return 'bg-white border-slate-300 text-slate-800'
})

const titulo = computed(() => {
  const nivel = props.usuario?.nivelRestriccion
  if (nivel === 'sin_pedidos_en_linea') {
    return 'Pedidos en línea suspendidos'
  }
  if (nivel === 'sin_mostrador') {
    return 'Restricción activa: solo pago con tarjeta en aplicación'
  }
  return `Aviso de recolección (${props.usuario?.faltasAbandono || 0} falta${props.usuario?.faltasAbandono === 1 ? '' : 's'} acumulada${props.usuario?.faltasAbandono === 1 ? '' : 's'})`
})

const mensaje = computed(() => {
  const nivel = props.usuario?.nivelRestriccion
  const fecha = formatearFechaHora(props.usuario?.restriccionHasta)
  if (nivel === 'sin_pedidos_en_linea') {
    return `Acumulaste 4 faltas por pedidos no reclamados. Puedes consultar el menú en tiempo real, pero deberás comprar directamente en mostrador hasta el ${fecha}.`
  }
  if (nivel === 'sin_mostrador') {
    return `Acumulaste 3 faltas por pedidos no reclamados. El pago en mostrador estará deshabilitado hasta el ${fecha}. Solo puedes ordenar con tarjeta en línea; si acumulas una 4ta falta se suspenderán los pedidos por 15 días adicionales.`
  }
  return 'Recuerda recoger tus pedidos a tiempo. Al acumular 3 faltas se deshabilita el pago en mostrador durante un mes. Si pasan 60 días sin reincidir, tu contador vuelve a cero.'
})
</script>
