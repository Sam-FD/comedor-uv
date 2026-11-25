<template>
  <div class="space-y-6">
    <section class="bg-white border border-slate-200 rounded-xl p-4 shadow-sm space-y-3">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 class="text-sm font-bold text-slate-900">Validación rápida en mostrador</h2>
          <p class="text-xs text-slate-500">Ingresa el código de referencia del estudiante para validar su cobro y entrega</p>
        </div>
        <form @submit.prevent="enviarBusqueda" class="flex flex-wrap items-center gap-2 w-full sm:w-auto">
          <input
            v-model="referenciaBusqueda"
            type="text"
            maxlength="4"
            placeholder="Ej. A001"
            class="flex-1 sm:flex-none sm:w-32 min-w-[110px] uppercase rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-semibold tracking-wider focus:outline-none focus:ring-2 focus:ring-uv-azul/20 focus:border-uv-azul"
          />
          <button
            type="submit"
            class="px-3.5 py-1.5 rounded-lg bg-uv-azul hover:bg-uv-azul-oscuro text-white text-sm font-medium shrink-0"
          >
            Buscar
          </button>
          <button
            v-if="pedidoEncontrado"
            type="button"
            @click="limpiarBusqueda"
            class="px-2.5 py-1.5 rounded-lg border border-slate-300 text-xs text-slate-600 hover:bg-slate-50 shrink-0"
          >
            Limpiar
          </button>
        </form>
      </div>

      <div v-if="pedidoEncontrado" class="rounded-xl border border-uv-azul/30 bg-uv-azul-claro/40 p-4 flex flex-wrap items-center justify-between gap-4">
        <div class="space-y-1 text-sm">
          <div class="flex flex-wrap items-center gap-2">
            <span class="text-xl font-bold text-uv-azul tabular-nums">{{ pedidoEncontrado.referencia }}</span>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-white border border-slate-200 text-slate-800">
              {{ etiquetaEstadoPedido(pedidoEncontrado.estadoPedido) }}
            </span>
            <span
              class="px-2.5 py-0.5 rounded-full text-xs font-semibold"
              :class="pedidoEncontrado.modalidadPago === 'mostrador' ? 'bg-amber-100 text-amber-900' : 'bg-uv-verde-claro text-uv-verde-oscuro'"
            >
              {{ pedidoEncontrado.modalidadPago === 'mostrador' ? 'Cobrar en caja: ' + formatearMoneda(pedidoEncontrado.total) : 'App tarjeta (' + etiquetaEstadoPago(pedidoEncontrado.estadoPago) + ')' }}
            </span>
          </div>
          <p class="text-slate-700">
            Estudiante: <span class="font-medium">{{ pedidoEncontrado.nombreEstudiante }}</span> ({{ pedidoEncontrado.credencialEstudiante }})
          </p>
          <p class="text-xs text-slate-600">
            {{ pedidoEncontrado.detalles.map((d) => `${d.cantidad}x ${d.nombrePlatillo}`).join(' · ') }}
          </p>
        </div>

        <div v-if="tieneSiguienteEstado(pedidoEncontrado.estadoPedido)">
          <button
            type="button"
            @click="solicitarAvanceEstado(pedidoEncontrado)"
            class="px-4 py-2 rounded-lg text-sm font-semibold text-white"
            :class="pedidoEncontrado.estadoPedido === 'listo_para_recoger' ? 'bg-uv-verde hover:bg-uv-verde-oscuro' : 'bg-uv-azul hover:bg-uv-azul-oscuro'"
          >
            {{ textoAccionEstado(pedidoEncontrado.estadoPedido) }}
          </button>
        </div>
      </div>
    </section>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
      <div
        v-for="columna in columnasCola"
        :key="columna.estado"
        class="bg-white border border-slate-200 border-t-4 rounded-xl shadow-sm flex flex-col overflow-hidden"
        :class="columna.bordeSuperior"
      >
        <div class="px-4 py-3 border-b border-slate-200 flex items-center justify-between bg-slate-50/70">
          <h3 class="text-sm font-bold text-slate-800">{{ columna.titulo }}</h3>
          <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-white border border-slate-200 text-slate-700 tabular-nums">
            {{ columna.items.length }}
          </span>
        </div>

        <div class="p-3 space-y-3 flex-1">
          <p v-if="columna.items.length === 0" class="text-xs text-slate-400 text-center py-8">
            Sin pedidos en esta etapa
          </p>

          <article
            v-for="pedido in columna.items"
            :key="pedido.id"
            class="border border-slate-200 rounded-lg p-3.5 space-y-2.5 bg-white"
          >
            <div class="flex items-start justify-between gap-2">
              <div>
                <span class="text-xl font-bold text-uv-azul tabular-nums">{{ pedido.referencia }}</span>
                <span class="ml-2 text-xs text-slate-500">{{ etiquetaTurno(pedido.turno) }}</span>
              </div>
              <span class="text-sm font-bold text-slate-900 tabular-nums">{{ formatearMoneda(pedido.total) }}</span>
            </div>

            <div class="text-xs text-slate-600">
              <p class="font-medium text-slate-800">{{ pedido.nombreEstudiante }}</p>
              <p>{{ pedido.credencialEstudiante }} · {{ formatearHora(pedido.fechaCreacion) }}</p>
            </div>

            <ul class="text-xs text-slate-700 bg-slate-50 rounded-md p-2.5 space-y-1">
              <li v-for="d in pedido.detalles" :key="d.id" class="flex justify-between gap-2">
                <span>{{ d.cantidad }} x {{ d.nombrePlatillo }}</span>
                <span class="tabular-nums text-slate-500">{{ formatearMoneda(d.subtotal) }}</span>
              </li>
            </ul>

            <div class="flex items-center justify-between gap-2 text-xs">
              <span
                class="px-2 py-0.5 rounded font-medium"
                :class="pedido.modalidadPago === 'mostrador' ? 'bg-amber-50 text-amber-800 border border-amber-200' : 'bg-uv-azul-claro text-uv-azul'"
              >
                {{ pedido.modalidadPago === 'mostrador' ? 'Cobrar en mostrador' : 'Pagado en app (retenido)' }}
              </span>

              <span
                v-if="pedido.estadoPedido === 'listo_para_recoger'"
                class="font-semibold text-uv-verde-oscuro tabular-nums"
              >
                Vence {{ formatearRestante(pedido.fechaLimiteRecoleccion) }}
              </span>
            </div>

            <div class="space-y-1.5">
              <button
                type="button"
                @click="solicitarAvanceEstado(pedido)"
                class="w-full py-2 rounded-md text-xs font-semibold text-white transition-colors"
                :class="pedido.estadoPedido === 'listo_para_recoger' ? 'bg-uv-verde hover:bg-uv-verde-oscuro' : 'bg-uv-azul hover:bg-uv-azul-oscuro'"
              >
                {{ textoAccionEstado(pedido.estadoPedido) }}
              </button>

              <button
                v-if="pedido.estadoPedido === 'listo_para_recoger'"
                type="button"
                @click="pedidoAVencer = pedido"
                class="w-full py-1.5 rounded-md text-[11px] font-medium border border-slate-200 text-slate-600 hover:bg-red-50 hover:border-red-200 hover:text-red-700 transition-colors"
              >
                Marcar no reclamado
              </button>
            </div>
          </article>
        </div>
      </div>
    </div>

    <div
      v-if="pedidoAEntregar"
      class="fixed inset-0 bg-slate-900/40 z-50 flex items-center justify-center p-4"
      @click.self="pedidoAEntregar = null"
    >
      <div class="bg-white border border-slate-200 rounded-xl shadow-lg max-w-md w-full p-5 space-y-4 max-h-[90vh] overflow-y-auto">
        <div class="flex items-start justify-between gap-2">
          <div>
            <p class="text-xs uppercase tracking-wide text-slate-500 font-medium">Confirmar entrega</p>
            <h3 class="text-2xl font-bold text-uv-azul tabular-nums">{{ pedidoAEntregar.referencia }}</h3>
          </div>
          <span class="text-lg font-bold text-slate-900 tabular-nums">{{ formatearMoneda(pedidoAEntregar.total) }}</span>
        </div>

        <p class="text-sm text-slate-700">
          Estudiante: <span class="font-semibold">{{ pedidoAEntregar.nombreEstudiante }}</span> ({{ pedidoAEntregar.credencialEstudiante }})
        </p>

        <ul class="text-xs text-slate-700 bg-slate-50 border border-slate-200 rounded-lg p-3 space-y-1">
          <li v-for="d in pedidoAEntregar.detalles" :key="d.id" class="flex justify-between">
            <span>{{ d.cantidad }} x {{ d.nombrePlatillo }}</span>
            <span class="tabular-nums font-medium">{{ formatearMoneda(d.subtotal) }}</span>
          </li>
        </ul>

        <div
          class="rounded-lg border p-3 text-sm"
          :class="pedidoAEntregar.modalidadPago === 'mostrador' ? 'bg-amber-50 border-amber-300 text-amber-900' : 'bg-uv-verde-claro border-uv-verde/40 text-uv-verde-oscuro'"
        >
          <p v-if="pedidoAEntregar.modalidadPago === 'mostrador'" class="font-semibold">
            Cobrar {{ formatearMoneda(pedidoAEntregar.total) }} en caja antes de entregar (efectivo o tarjeta física).
          </p>
          <p v-else class="font-semibold">
            Pago retenido en app por {{ formatearMoneda(pedidoAEntregar.total) }}. Al confirmar se ejecutará el cobro definitivo.
          </p>
        </div>

        <div class="flex justify-end gap-2 pt-2">
          <button
            type="button"
            @click="pedidoAEntregar = null"
            class="px-4 py-2 rounded-lg border border-slate-300 text-sm text-slate-700 hover:bg-slate-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            @click="confirmarEntrega"
            class="px-4 py-2 rounded-lg bg-uv-verde hover:bg-uv-verde-oscuro text-white text-sm font-semibold"
          >
            Confirmar cobro y entrega
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="pedidoAVencer"
      class="fixed inset-0 bg-slate-900/40 z-50 flex items-center justify-center p-4"
      @click.self="pedidoAVencer = null"
    >
      <div class="bg-white border border-slate-200 rounded-xl shadow-lg max-w-md w-full p-5 space-y-4 max-h-[90vh] overflow-y-auto">
        <h3 class="text-base font-bold text-slate-900">
          Marcar pedido {{ pedidoAVencer.referencia }} como no reclamado
        </h3>
        <p class="text-sm text-slate-600 leading-relaxed">
          Esta acción cerrará el pedido de {{ pedidoAVencer.nombreEstudiante }}, liberará o cancelará el pago correspondiente y sumará 1 falta de abandono a su cuenta.
        </p>
        <div class="flex justify-end gap-2">
          <button
            type="button"
            @click="pedidoAVencer = null"
            class="px-4 py-2 rounded-lg border border-slate-300 text-sm text-slate-700 hover:bg-slate-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            @click="confirmarVencimiento"
            class="px-4 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-semibold"
          >
            Confirmar no reclamado
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import {
  etiquetaEstadoPago,
  etiquetaEstadoPedido,
  etiquetaTurno,
  formatearHora,
  formatearMoneda
} from '../../utils/formatos'

const props = defineProps({
  cola: { type: Array, required: true },
  ahoraMs: { type: Number, required: true },
  pedidoEncontrado: { type: Object, default: null }
})

const emit = defineEmits(['buscar-referencia', 'limpiar-busqueda', 'avanzar-estado', 'vencer-pedido'])

const referenciaBusqueda = ref('')
const pedidoAEntregar = ref(null)
const pedidoAVencer = ref(null)

const columnasCola = computed(() => [
  {
    estado: 'recibido',
    titulo: 'Recibidos',
    bordeSuperior: 'border-t-slate-400',
    items: props.cola.filter((p) => p.estadoPedido === 'recibido')
  },
  {
    estado: 'en_preparacion',
    titulo: 'En preparación',
    bordeSuperior: 'border-t-uv-azul',
    items: props.cola.filter((p) => p.estadoPedido === 'en_preparacion')
  },
  {
    estado: 'listo_para_recoger',
    titulo: 'Listos para recoger',
    bordeSuperior: 'border-t-uv-verde',
    items: props.cola.filter((p) => p.estadoPedido === 'listo_para_recoger')
  }
])

function tieneSiguienteEstado(actual) {
  return ['recibido', 'en_preparacion', 'listo_para_recoger'].includes(actual)
}

function textoAccionEstado(actual) {
  if (actual === 'recibido') return 'Iniciar preparación'
  if (actual === 'en_preparacion') return 'Marcar listo para recoger'
  if (actual === 'listo_para_recoger') return 'Validar y entregar'
  return ''
}

function formatearRestante(isoLimite) {
  if (!isoLimite) return '--:--'
  const diffSeg = Math.max(0, Math.floor((new Date(isoLimite).getTime() - props.ahoraMs) / 1000))
  const min = String(Math.floor(diffSeg / 60)).padStart(2, '0')
  const seg = String(diffSeg % 60).padStart(2, '0')
  return `${min}:${seg}`
}

function enviarBusqueda() {
  const refLimpia = referenciaBusqueda.value.trim()
  if (refLimpia) {
    emit('buscar-referencia', refLimpia)
  }
}

function limpiarBusqueda() {
  referenciaBusqueda.value = ''
  emit('limpiar-busqueda')
}

function solicitarAvanceEstado(pedido) {
  if (pedido.estadoPedido === 'listo_para_recoger') {
    pedidoAEntregar.value = pedido
    return
  }
  emit('avanzar-estado', pedido)
}

function confirmarEntrega() {
  if (!pedidoAEntregar.value) return
  const pedido = pedidoAEntregar.value
  pedidoAEntregar.value = null
  emit('avanzar-estado', pedido)
}

function confirmarVencimiento() {
  if (!pedidoAVencer.value) return
  const pedido = pedidoAVencer.value
  pedidoAVencer.value = null
  emit('vencer-pedido', pedido)
}

function manejarBotonAtras(e) {
  if (pedidoAEntregar.value) {
    e.preventDefault()
    pedidoAEntregar.value = null
  } else if (pedidoAVencer.value) {
    e.preventDefault()
    pedidoAVencer.value = null
  }
}

onMounted(() => window.addEventListener('app-back', manejarBotonAtras))
onUnmounted(() => window.removeEventListener('app-back', manejarBotonAtras))
</script>
