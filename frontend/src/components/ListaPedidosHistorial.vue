<template>
  <div v-if="pedidos.length === 0" class="p-6 text-sm text-slate-500 text-center">
    {{ mensajeVacio }}
  </div>

  <template v-else>
    <div class="md:hidden divide-y divide-slate-100">
      <article v-for="p in pedidos" :key="`m-${p.id}`" class="p-4 space-y-2">
        <div class="flex items-start justify-between gap-2">
          <div>
            <span class="text-base font-bold text-uv-azul tabular-nums">{{ p.referencia }}</span>
            <span class="ml-2 text-xs text-slate-500">{{ p.fechaOperativa }} · {{ etiquetaTurno(p.turno) }}</span>
          </div>
          <span class="px-2.5 py-0.5 rounded-full text-xs font-medium shrink-0" :class="claseEstado(p.estadoPedido)">
            {{ etiquetaEstadoPedido(p.estadoPedido) }}
          </span>
        </div>

        <p v-if="mostrarEstudiante" class="text-xs text-slate-700">
          <span class="font-medium">{{ p.nombreEstudiante }}</span> ({{ p.credencialEstudiante }})
        </p>

        <ul class="text-xs text-slate-700 bg-slate-50 rounded-md p-2.5 space-y-1">
          <li v-for="d in p.detalles" :key="d.id" class="flex justify-between gap-2">
            <span>{{ d.cantidad }} x {{ d.nombrePlatillo }}</span>
            <span class="tabular-nums text-slate-500">{{ formatearMoneda(d.subtotal) }}</span>
          </li>
        </ul>

        <div class="flex items-center justify-between gap-2 text-xs">
          <span class="text-slate-500">
            {{ etiquetaModalidadPago(p.modalidadPago) }} · {{ etiquetaEstadoPago(p.estadoPago) }}
          </span>
          <span class="text-sm font-bold text-slate-900 tabular-nums">{{ formatearMoneda(p.total) }}</span>
        </div>
      </article>
    </div>

    <div class="hidden md:block overflow-x-auto">
      <table class="w-full text-sm text-left">
        <thead class="bg-slate-50 text-xs uppercase text-slate-500 border-b border-slate-200">
          <tr>
            <th class="px-4 py-3">Referencia</th>
            <th class="px-4 py-3">Fecha y turno</th>
            <th v-if="mostrarEstudiante" class="px-4 py-3">Estudiante</th>
            <th class="px-4 py-3">Platillos</th>
            <th class="px-4 py-3">Pago</th>
            <th class="px-4 py-3">Estado</th>
            <th class="px-4 py-3 text-right">Total</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="p in pedidos" :key="p.id" class="hover:bg-slate-50">
            <td class="px-4 py-3 font-bold text-uv-azul tabular-nums">{{ p.referencia }}</td>
            <td class="px-4 py-3 text-xs text-slate-600">
              <div>{{ p.fechaOperativa }}</div>
              <div class="text-slate-400">{{ etiquetaTurno(p.turno) }}</div>
            </td>
            <td v-if="mostrarEstudiante" class="px-4 py-3 text-xs">
              <div class="font-medium text-slate-800">{{ p.nombreEstudiante }}</div>
              <div class="text-slate-400">{{ p.credencialEstudiante }}</div>
            </td>
            <td class="px-4 py-3 text-xs text-slate-700">
              <ul class="space-y-0.5">
                <li v-for="d in p.detalles" :key="d.id">
                  {{ d.cantidad }} x {{ d.nombrePlatillo }}
                </li>
              </ul>
            </td>
            <td class="px-4 py-3 text-xs text-slate-600">
              <div>{{ etiquetaModalidadPago(p.modalidadPago) }}</div>
              <div class="text-slate-400">{{ etiquetaEstadoPago(p.estadoPago) }}</div>
            </td>
            <td class="px-4 py-3">
              <span class="px-2.5 py-0.5 rounded-full text-xs font-medium" :class="claseEstado(p.estadoPedido)">
                {{ etiquetaEstadoPedido(p.estadoPedido) }}
              </span>
            </td>
            <td class="px-4 py-3 text-right font-semibold tabular-nums">{{ formatearMoneda(p.total) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>
</template>

<script setup>
import {
  etiquetaEstadoPago,
  etiquetaEstadoPedido,
  etiquetaModalidadPago,
  etiquetaTurno,
  formatearMoneda
} from '../utils/formatos'

defineProps({
  pedidos: {
    type: Array,
    required: true
  },
  mostrarEstudiante: {
    type: Boolean,
    default: false
  },
  mensajeVacio: {
    type: String,
    default: 'No hay pedidos registrados.'
  }
})

function claseEstado(estado) {
  if (estado === 'entregado') return 'bg-uv-verde-claro text-uv-verde-oscuro'
  if (estado === 'no_reclamado') return 'bg-red-50 text-red-700'
  return 'bg-slate-100 text-slate-600'
}
</script>
