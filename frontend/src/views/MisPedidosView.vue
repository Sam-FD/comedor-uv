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

      <ListaPedidosHistorial
        :pedidos="pedidosCerrados"
        mensaje-vacio="Aún no tienes pedidos concluidos en tu historial."
      />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import api from '../services/api'
import { suscribirEvento, socketConectado } from '../services/socket'
import { actualizarUsuarioEnSesion, refrescarPerfil, sesion } from '../stores/sesion'
import BannerRestriccion from '../components/BannerRestriccion.vue'
import ListaPedidosHistorial from '../components/ListaPedidosHistorial.vue'
import PaseRecoleccion from '../components/PaseRecoleccion.vue'

const pedidos = ref([])
const avisoEstado = ref('')
const desuscripciones = []

const estadosActivos = ['recibido', 'en_preparacion', 'listo_para_recoger']

const pedidoActivo = computed(() => pedidos.value.find((p) => estadosActivos.includes(p.estadoPedido)) || null)
const pedidosCerrados = computed(() => pedidos.value.filter((p) => !estadosActivos.includes(p.estadoPedido)))

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
