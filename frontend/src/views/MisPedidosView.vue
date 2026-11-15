<template>
  <div class="max-w-5xl mx-auto px-4 py-6 space-y-6">
    <BannerRestriccion v-if="sesion.usuario" :usuario="sesion.usuario" />

    <div
      v-if="avisoEstado"
      class="rounded-xl border border-uv-verde/40 bg-uv-verde-claro px-4 py-3 text-sm text-uv-verde-oscuro shadow-sm flex items-center justify-between gap-2"
    >
      <span class="font-medium">{{ avisoEstado }}</span>
      <button type="button" @click="avisoEstado = ''" class="text-xs font-semibold underline shrink-0">Ocultar</button>
    </div>

    <div v-if="pedidoActivo" class="space-y-2">
      <h2 class="text-sm font-semibold uppercase tracking-wide text-slate-600">Pedido en curso</h2>
      <PaseRecoleccion
        :pedido="pedidoActivo"
        @cancelado="cargarPedidos"
        @vencido="alActualizarPorVencimiento"
      />
    </div>

    <section class="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden">
      <div class="px-5 py-4 border-b border-slate-200 flex items-center justify-between">
        <div>
          <h2 class="text-base font-bold text-slate-900">Historial de mis pedidos</h2>
          <p class="text-xs text-slate-500">Registro de órdenes realizadas con tu cuenta institucional</p>
        </div>
        <RouterLink to="/" class="text-sm font-medium text-uv-azul hover:underline">
          Volver al menú
        </RouterLink>
      </div>

      <div v-if="pedidosCerrados.length === 0" class="p-6 text-sm text-slate-500 text-center">
        Aún no tienes pedidos concluidos en tu historial.
      </div>

      <template v-else>
        <div class="md:hidden divide-y divide-slate-100">
          <article v-for="pedido in pedidosCerrados" :key="`m-${pedido.id}`" class="p-4 space-y-2.5">
            <div class="flex items-start justify-between gap-2">
              <div>
                <p class="text-base font-bold text-uv-azul tabular-nums">{{ pedido.referencia }}</p>
                <p class="text-xs text-slate-500">{{ pedido.fechaOperativa }} · {{ etiquetaTurno(pedido.turno) }}</p>
              </div>
              <span class="px-2.5 py-0.5 rounded-full text-xs font-medium shrink-0" :class="claseEstadoCerrado(pedido.estadoPedido)">
                {{ etiquetaEstadoPedido(pedido.estadoPedido) }}
              </span>
            </div>

            <ul class="text-xs text-slate-700 bg-slate-50 rounded-md p-2.5 space-y-1">
              <li v-for="d in pedido.detalles" :key="d.id" class="flex justify-between gap-2">
                <span>{{ d.cantidad }} x {{ d.nombrePlatillo }}</span>
                <span class="tabular-nums text-slate-500">{{ formatearMoneda(d.subtotal) }}</span>
              </li>
            </ul>

            <div class="flex items-center justify-between gap-2 text-xs">
              <span class="text-slate-600">
                {{ etiquetaModalidadPago(pedido.modalidadPago) }} · {{ etiquetaEstadoPago(pedido.estadoPago) }}
              </span>
              <span class="text-sm font-bold text-slate-900 tabular-nums">
                {{ formatearMoneda(pedido.total) }}
              </span>
            </div>
          </article>
        </div>

        <div class="hidden md:block overflow-x-auto">
          <table class="w-full text-sm text-left">
            <thead class="bg-slate-50 text-xs uppercase text-slate-500 border-b border-slate-200">
              <tr>
                <th class="px-4 py-3">Referencia</th>
                <th class="px-4 py-3">Fecha y turno</th>
                <th class="px-4 py-3">Platillos</th>
                <th class="px-4 py-3">Pago</th>
                <th class="px-4 py-3">Estado</th>
                <th class="px-4 py-3 text-right">Total</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="pedido in pedidosCerrados" :key="pedido.id" class="hover:bg-slate-50">
                <td class="px-4 py-3 font-bold text-uv-azul tabular-nums">{{ pedido.referencia }}</td>
                <td class="px-4 py-3 text-slate-600">
                  <div>{{ pedido.fechaOperativa }}</div>
                  <div class="text-xs text-slate-400">{{ etiquetaTurno(pedido.turno) }}</div>
                </td>
                <td class="px-4 py-3 text-slate-700">
                  <ul class="space-y-0.5 text-xs">
                    <li v-for="d in pedido.detalles" :key="d.id">
                      {{ d.cantidad }} x {{ d.nombrePlatillo }}
                    </li>
                  </ul>
                </td>
                <td class="px-4 py-3 text-xs text-slate-600">
                  <div>{{ etiquetaModalidadPago(pedido.modalidadPago) }}</div>
                  <div class="text-slate-400">{{ etiquetaEstadoPago(pedido.estadoPago) }}</div>
                </td>
                <td class="px-4 py-3">
                  <span class="px-2.5 py-0.5 rounded-full text-xs font-medium" :class="claseEstadoCerrado(pedido.estadoPedido)">
                    {{ etiquetaEstadoPedido(pedido.estadoPedido) }}
                  </span>
                </td>
                <td class="px-4 py-3 text-right font-semibold tabular-nums">
                  {{ formatearMoneda(pedido.total) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import api from '../services/api'
import { suscribirEvento, socketConectado } from '../services/socket'
import { actualizarUsuarioEnSesion, refrescarPerfil, sesion } from '../stores/sesion'
import {
  etiquetaEstadoPago,
  etiquetaEstadoPedido,
  etiquetaModalidadPago,
  etiquetaTurno,
  formatearMoneda
} from '../utils/formatos'
import BannerRestriccion from '../components/BannerRestriccion.vue'
import PaseRecoleccion from '../components/PaseRecoleccion.vue'

const pedidos = ref([])
const avisoEstado = ref('')
const desuscripciones = []

const estadosActivos = ['recibido', 'en_preparacion', 'listo_para_recoger']

const pedidoActivo = computed(() => pedidos.value.find((p) => estadosActivos.includes(p.estadoPedido)) || null)
const pedidosCerrados = computed(() => pedidos.value.filter((p) => !estadosActivos.includes(p.estadoPedido)))

function claseEstadoCerrado(estado) {
  if (estado === 'entregado') return 'bg-uv-verde-claro text-uv-verde-oscuro'
  if (estado === 'no_reclamado') return 'bg-red-50 text-red-700'
  return 'bg-slate-100 text-slate-600'
}

async function cargarPedidos() {
  const { data } = await api.get('/pedidos/mis-pedidos')
  pedidos.value = data
}

async function alActualizarPorVencimiento() {
  const estabaActivo = Boolean(pedidoActivo.value)
  await Promise.all([cargarPedidos(), refrescarPerfil()])
  if (estabaActivo && !pedidoActivo.value) {
    avisoEstado.value = 'El tiempo de recolección venció y tu pedido pasó al historial.'
  }
}

let respaldoTimer = null

onMounted(async () => {
  try {
    await Promise.all([refrescarPerfil(), cargarPedidos()])
  } catch {
    avisoEstado.value = 'No se pudo actualizar el historial de pedidos en este momento.'
  }

  respaldoTimer = setInterval(() => {
    if (!socketConectado.value) {
      Promise.all([refrescarPerfil(), cargarPedidos()]).catch(() => {})
    }
  }, 10000)

  desuscripciones.push(
    suscribirEvento('pedido_actualizado', (pedido) => {
      cargarPedidos()
      if (pedido?.estadoPedido === 'listo_para_recoger') {
        avisoEstado.value = `Tu pedido ${pedido.referencia} ya está listo para recoger.`
      } else if (pedido?.estadoPedido === 'entregado') {
        avisoEstado.value = `Tu pedido ${pedido.referencia} fue entregado.`
      } else if (pedido?.estadoPedido === 'no_reclamado') {
        avisoEstado.value = `Tu pedido ${pedido.referencia} venció y fue marcado como no reclamado.`
      }
    }),
    suscribirEvento('usuario_actualizado', (usuario) => {
      actualizarUsuarioEnSesion(usuario)
    })
  )
})

onUnmounted(() => {
  if (respaldoTimer) clearInterval(respaldoTimer)
  desuscripciones.forEach((fn) => fn())
})
</script>
