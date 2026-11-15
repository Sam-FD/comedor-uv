<template>
  <section
    class="bg-white rounded-xl border shadow-sm overflow-hidden"
    :class="estaListo ? 'border-uv-verde ring-1 ring-uv-verde/30' : 'border-slate-200'"
  >
    <div class="p-4 sm:p-5 border-b border-dashed border-slate-200 flex flex-wrap items-center justify-between gap-4 bg-slate-50/50">
      <div class="flex items-center gap-4">
        <div class="bg-uv-azul-claro border border-uv-azul/20 rounded-lg px-4 py-2 text-center">
          <p class="text-[10px] font-semibold uppercase tracking-wider text-uv-azul/80">Codigo</p>
          <p class="text-3xl font-bold tracking-widest text-uv-azul tabular-nums leading-tight">
            {{ pedido.referencia }}
          </p>
        </div>

        <div>
          <div class="flex flex-wrap items-center gap-2">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">
              Pase de recoleccion
            </span>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold" :class="claseEstado">
              {{ etiquetaEstadoPedido(pedido.estadoPedido) }}
            </span>
          </div>
          <p class="text-sm text-slate-600 mt-1">
            Turno {{ etiquetaTurno(pedido.turno).toLowerCase() }} · Registrado a las {{ formatearHora(pedido.fechaCreacion) }}
          </p>
        </div>
      </div>

      <div class="text-left sm:text-right">
        <p class="text-xs text-slate-500">Total del pedido</p>
        <p class="text-2xl font-bold text-slate-900 tabular-nums">
          {{ formatearMoneda(pedido.total) }}
        </p>
        <p class="text-xs text-slate-600 mt-0.5">
          {{ etiquetaModalidadPago(pedido.modalidadPago) }} ({{ etiquetaEstadoPago(pedido.estadoPago) }})
        </p>
        <p v-if="transaccionPasarela" class="text-[11px] text-slate-500 mt-0.5 tabular-nums">
          Pasarela {{ transaccionPasarela.modo === 'stripe_test' ? 'Stripe Test' : 'Sandbox' }} · {{ transaccionPasarela.transaccionId }} (**** {{ transaccionPasarela.ultimosDigitos }})
        </p>
      </div>
    </div>

    <div class="p-4 sm:p-5 space-y-4">
      <!-- Indicador de progreso en 3 pasos -->
      <div class="grid grid-cols-3 gap-2 text-xs">
        <div
          v-for="(paso, idx) in pasos"
          :key="paso.clave"
          class="rounded-lg border px-2.5 py-2 flex items-center justify-center gap-1.5 font-medium"
          :class="clasePaso(idx)"
        >
          <span
            class="w-4 h-4 rounded-full text-[10px] font-bold flex items-center justify-center shrink-0"
            :class="idx <= indicePaso ? (estaListo && idx === 2 ? 'bg-uv-verde text-white' : 'bg-uv-azul text-white') : 'bg-slate-200 text-slate-500'"
          >
            {{ idx + 1 }}
          </span>
          <span class="truncate">{{ paso.texto }}</span>
        </div>
      </div>

      <div
        v-if="estaListo"
        class="rounded-lg bg-uv-verde-claro border border-uv-verde/40 px-4 py-3 flex flex-wrap items-center justify-between gap-3"
      >
        <div class="text-sm text-uv-verde-oscuro">
          <p class="font-semibold">Tu orden ya esta lista para recoger en mostrador.</p>
          <p v-if="pedido.modalidadPago === 'mostrador'" class="mt-0.5">
            Presenta la referencia {{ pedido.referencia }} en caja para pagar {{ formatearMoneda(pedido.total) }} y recibir tus alimentos.
          </p>
          <p v-else class="mt-0.5">
            Pasa directo al area de entrega con la referencia {{ pedido.referencia }} para confirmar tu cobro y recibir tus alimentos.
          </p>
        </div>
        <div class="bg-white border border-uv-verde/30 rounded-md px-3 py-1.5 text-sm font-bold text-uv-verde-oscuro tabular-nums shrink-0">
          Tiempo limite: {{ textoCuentaRegresiva }}
        </div>
      </div>

      <div class="pt-1">
        <p class="text-xs font-semibold uppercase tracking-wide text-slate-500 mb-2">Detalle de tu orden</p>
        <ul class="space-y-1.5 text-sm bg-slate-50 border border-slate-100 rounded-lg p-3">
          <li
            v-for="item in pedido.detalles"
            :key="item.id"
            class="flex items-center justify-between text-slate-700"
          >
            <span>{{ item.cantidad }} x {{ item.nombrePlatillo }}</span>
            <span class="tabular-nums font-semibold text-slate-900">{{ formatearMoneda(item.subtotal) }}</span>
          </li>
        </ul>

        <div v-if="errorCancelacion" class="mt-3 rounded border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-800">
          {{ errorCancelacion }}
        </div>

        <div class="mt-3 flex flex-wrap items-center justify-between gap-2">
          <span class="text-xs text-slate-500">
            Muestra esta referencia en el mostrador del comedor.
          </span>

          <template v-if="pedido.estadoPedido === 'recibido'">
            <button
              v-if="!confirmandoCancelacion"
              type="button"
              @click="confirmandoCancelacion = true"
              class="px-3 py-1.5 rounded-md border border-red-200 text-xs font-medium text-red-700 hover:bg-red-50 transition-colors"
            >
              Cancelar pedido
            </button>
            <div v-else class="flex items-center gap-2 text-xs">
              <span class="text-slate-600">¿Cancelar sin falta?</span>
              <button
                type="button"
                :disabled="cancelando"
                @click="confirmarCancelacion"
                class="px-2.5 py-1 rounded bg-red-600 hover:bg-red-700 text-white font-medium disabled:opacity-60"
              >
                {{ cancelando ? 'Cancelando...' : 'Si, cancelar' }}
              </button>
              <button
                type="button"
                @click="confirmandoCancelacion = false"
                class="px-2 py-1 rounded border border-slate-300 text-slate-600 hover:bg-slate-50"
              >
                No
              </button>
            </div>
          </template>

          <span v-else-if="pedido.estadoPedido === 'en_preparacion'" class="text-xs text-slate-500">
            En preparacion (ya no es posible cancelar)
          </span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import api, { extraerMensajeError } from '../services/api'
import {
  etiquetaEstadoPago,
  etiquetaEstadoPedido,
  etiquetaModalidadPago,
  etiquetaTurno,
  formatearHora,
  formatearMoneda
} from '../utils/formatos'

const props = defineProps({
  pedido: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['cancelado', 'vencido'])

const ahoraMs = ref(Date.now())
const confirmandoCancelacion = ref(false)
const cancelando = ref(false)
const errorCancelacion = ref('')
const transaccionPasarela = ref(null)
let timer = null
let vencimientoTimer = null
let vencimientoNotificado = false
let reintentosVencimiento = 0

async function cargarTransaccionPasarela() {
  if (!props.pedido?.id || props.pedido.modalidadPago !== 'app_tarjeta') {
    transaccionPasarela.value = null
    return
  }
  try {
    const res = await api.get(`/pagos/pedido/${props.pedido.id}`)
    transaccionPasarela.value = res.status === 200 ? res.data : null
  } catch {
    transaccionPasarela.value = null
  }
}

watch(
  () => [props.pedido?.id, props.pedido?.estadoPedido, props.pedido?.estadoPago],
  () => {
    confirmandoCancelacion.value = false
    errorCancelacion.value = ''
    vencimientoNotificado = false
    reintentosVencimiento = 0
    if (vencimientoTimer) {
      clearTimeout(vencimientoTimer)
      vencimientoTimer = null
    }
    cargarTransaccionPasarela()
  },
  { immediate: true }
)

function programarNotificacionVencimiento(retrasoMs = 1200) {
  if (vencimientoTimer) {
    clearTimeout(vencimientoTimer)
  }
  vencimientoTimer = setTimeout(() => {
    emit('vencido')
    if (props.pedido?.estadoPedido === 'listo_para_recoger' && reintentosVencimiento < 4) {
      reintentosVencimiento += 1
      programarNotificacionVencimiento(3000)
    }
  }, retrasoMs)
}

onMounted(() => {
  timer = setInterval(() => {
    ahoraMs.value = Date.now()
    if (
      props.pedido?.estadoPedido === 'listo_para_recoger' &&
      props.pedido?.fechaLimiteRecoleccion &&
      !vencimientoNotificado
    ) {
      const limite = new Date(props.pedido.fechaLimiteRecoleccion).getTime()
      if (limite <= ahoraMs.value) {
        vencimientoNotificado = true
        programarNotificacionVencimiento(1200)
      }
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (vencimientoTimer) clearTimeout(vencimientoTimer)
})

async function confirmarCancelacion() {
  errorCancelacion.value = ''
  cancelando.value = true
  try {
    const { data } = await api.post(`/pedidos/${props.pedido.id}/cancelar`)
    confirmandoCancelacion.value = false
    emit('cancelado', data)
  } catch (err) {
    errorCancelacion.value = extraerMensajeError(err, 'No se pudo cancelar el pedido')
    confirmandoCancelacion.value = false
  } finally {
    cancelando.value = false
  }
}

const pasos = [
  { clave: 'recibido', texto: 'Recibido' },
  { clave: 'en_preparacion', texto: 'En preparacion' },
  { clave: 'listo_para_recoger', texto: 'Listo' }
]

const indicePaso = computed(() => {
  return pasos.findIndex((p) => p.clave === props.pedido.estadoPedido)
})

const estaListo = computed(() => props.pedido.estadoPedido === 'listo_para_recoger')

function clasePaso(idx) {
  if (idx <= indicePaso.value) {
    if (estaListo.value && idx === 2) {
      return 'bg-uv-verde-claro border-uv-verde/40 text-uv-verde-oscuro'
    }
    return 'bg-uv-azul-claro border-uv-azul/30 text-uv-azul'
  }
  return 'bg-slate-50 border-slate-200 text-slate-400'
}

const claseEstado = computed(() => {
  if (props.pedido.estadoPedido === 'listo_para_recoger') {
    return 'bg-uv-verde-claro text-uv-verde-oscuro'
  }
  if (props.pedido.estadoPedido === 'en_preparacion') {
    return 'bg-uv-azul-claro text-uv-azul'
  }
  return 'bg-slate-100 text-slate-700'
})

const textoCuentaRegresiva = computed(() => {
  if (!props.pedido.fechaLimiteRecoleccion) {
    return '--:--'
  }
  const limite = new Date(props.pedido.fechaLimiteRecoleccion).getTime()
  const diffSeg = Math.max(0, Math.floor((limite - ahoraMs.value) / 1000))
  const min = String(Math.floor(diffSeg / 60)).padStart(2, '0')
  const seg = String(diffSeg % 60).padStart(2, '0')
  return `${min}:${seg}`
})
</script>
