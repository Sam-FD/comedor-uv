<template>
  <div class="max-w-6xl mx-auto px-4 py-6 space-y-6">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div class="bg-white border border-slate-200 rounded-xl p-1 grid grid-cols-3 sm:inline-flex gap-1 text-xs sm:text-sm shadow-sm w-full sm:w-auto">
        <button
          type="button"
          @click="pestana = 'cola'"
          class="px-2.5 sm:px-4 py-2 rounded-lg font-medium transition-colors text-center"
          :class="pestana === 'cola' ? 'bg-uv-azul text-white' : 'text-slate-600 hover:bg-slate-100'"
        >
          <span class="sm:hidden">Cola ({{ cola.length }})</span>
          <span class="hidden sm:inline">Cola de pedidos ({{ cola.length }})</span>
        </button>
        <button
          type="button"
          @click="pestana = 'menu'"
          class="px-2.5 sm:px-4 py-2 rounded-lg font-medium transition-colors text-center"
          :class="pestana === 'menu' ? 'bg-uv-azul text-white' : 'text-slate-600 hover:bg-slate-100'"
        >
          <span class="sm:hidden">Menú</span>
          <span class="hidden sm:inline">Menú y disponibilidad</span>
        </button>
        <button
          type="button"
          @click="abrirHistorico"
          class="px-2.5 sm:px-4 py-2 rounded-lg font-medium transition-colors text-center"
          :class="pestana === 'historico' ? 'bg-uv-azul text-white' : 'text-slate-600 hover:bg-slate-100'"
        >
          <span class="sm:hidden">Histórico</span>
          <span class="hidden sm:inline">Histórico de pedidos</span>
        </button>
      </div>

      <button
        type="button"
        @click="alternarRecepcion"
        class="w-full sm:w-auto px-3.5 py-2 rounded-xl border text-xs font-semibold shadow-sm transition-colors"
        :class="
          catalogo.recepcionAbierta
            ? 'bg-uv-verde-claro border-uv-verde/40 text-uv-verde-oscuro hover:bg-amber-50 hover:border-amber-300 hover:text-amber-900'
            : 'bg-amber-50 border-amber-300 text-amber-900 hover:bg-uv-verde-claro hover:border-uv-verde/40 hover:text-uv-verde-oscuro'
        "
      >
        Recepción en línea: {{ catalogo.recepcionAbierta ? 'Abierta' : 'Cerrada' }}
      </button>
    </div>

    <div v-if="mensajeOperacion" class="rounded-xl border border-uv-verde/40 bg-uv-verde-claro px-4 py-2.5 text-sm text-uv-verde-oscuro shadow-sm flex items-center justify-between">
      <span>{{ mensajeOperacion }}</span>
      <button type="button" @click="mensajeOperacion = ''" class="text-xs font-semibold underline">Cerrar</button>
    </div>

    <div v-if="errorOperacion" class="rounded-xl border border-red-200 bg-red-50 px-4 py-2.5 text-sm text-red-800 shadow-sm flex items-center justify-between">
      <span>{{ errorOperacion }}</span>
      <button type="button" @click="errorOperacion = ''" class="text-xs font-semibold underline">Cerrar</button>
    </div>

    <PanelColaPedidos
      v-if="pestana === 'cola'"
      :cola="cola"
      :ahora-ms="ahoraMs"
      :pedido-encontrado="pedidoEncontrado"
      @buscar-referencia="buscarReferencia"
      @limpiar-busqueda="pedidoEncontrado = null"
      @avanzar-estado="avanzarEstado"
      @vencer-pedido="confirmarVencimiento"
    />

    <PanelGestionMenu
      v-if="pestana === 'menu'"
      :catalogo="catalogo"
      @alternar-disponibilidad="alternarDisponibilidad"
      @publicar-comida-dia="publicarComidaDelDia"
      @agregar-regular="agregarPlatilloRegular"
      @guardar-edicion="guardarEdicionPlatillo"
    />

    <PanelHistoricoPedidos
      v-if="pestana === 'historico'"
      :historico="historico"
      :filtros="filtrosHistorico"
      @cargar="cargarHistorico"
    />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import api, { extraerMensajeError } from '../services/api'
import { suscribirEvento, socketConectado } from '../services/socket'
import { etiquetaEstadoPedido } from '../utils/formatos'
import PanelColaPedidos from '../components/personal/PanelColaPedidos.vue'
import PanelGestionMenu from '../components/personal/PanelGestionMenu.vue'
import PanelHistoricoPedidos from '../components/personal/PanelHistoricoPedidos.vue'

const pestana = ref('cola')
const cola = ref([])
const catalogo = ref({
  fechaOperativa: '',
  turnoSugerido: 'matutino',
  recepcionAbierta: true,
  comidasDelDia: [],
  menuRegular: []
})
const pedidoEncontrado = ref(null)
const mensajeOperacion = ref('')
const errorOperacion = ref('')
const ahoraMs = ref(Date.now())

let timer = null
let respaldoTimer = null
const desuscripciones = []

const filtrosHistorico = reactive({
  fecha: '',
  turno: '',
  modalidadPago: '',
  estado: ''
})

const historico = ref({
  pedidos: [],
  totalEntregados: 0,
  totalNoReclamados: 0,
  montoCobradoApp: 0,
  montoCobradoMostrador: 0,
  montoTotalCobrado: 0
})

function siguienteEstado(actual) {
  if (actual === 'recibido') return 'en_preparacion'
  if (actual === 'en_preparacion') return 'listo_para_recoger'
  if (actual === 'listo_para_recoger') return 'entregado'
  return null
}

async function alternarRecepcion() {
  errorOperacion.value = ''
  try {
    const { data } = await api.patch('/personal/operacion/recepcion', {
      abierta: !catalogo.value.recepcionAbierta
    })
    catalogo.value.recepcionAbierta = data.recepcionAbierta
    mensajeOperacion.value = data.recepcionAbierta
      ? 'Recepción de pedidos en línea abierta.'
      : 'Recepción de pedidos en línea cerrada temporalmente.'
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo cambiar el estado de recepción')
  }
}

async function cargarCola() {
  const { data } = await api.get('/personal/pedidos/cola')
  cola.value = data
}

async function cargarCatalogo() {
  const { data } = await api.get('/platillos')
  catalogo.value = data
}

async function cargarHistorico() {
  const params = {}
  if (filtrosHistorico.fecha) params.fecha = filtrosHistorico.fecha
  if (filtrosHistorico.turno) params.turno = filtrosHistorico.turno
  if (filtrosHistorico.modalidadPago) params.modalidadPago = filtrosHistorico.modalidadPago
  if (filtrosHistorico.estado) params.estado = filtrosHistorico.estado

  const { data } = await api.get('/personal/pedidos/historico', { params })
  historico.value = data
}

function abrirHistorico() {
  pestana.value = 'historico'
  cargarHistorico()
}

async function buscarReferencia(referencia) {
  errorOperacion.value = ''
  try {
    const { data } = await api.get('/personal/pedidos/buscar', { params: { referencia } })
    pedidoEncontrado.value = data
  } catch (err) {
    pedidoEncontrado.value = null
    errorOperacion.value = extraerMensajeError(err, 'No se encontró la referencia')
  }
}

async function avanzarEstado(pedido) {
  errorOperacion.value = ''
  mensajeOperacion.value = ''
  const siguiente = siguienteEstado(pedido.estadoPedido)
  if (!siguiente) return

  try {
    const { data } = await api.patch(`/personal/pedidos/${pedido.id}/estado`, {
      estadoPedido: siguiente
    })
    if (pedidoEncontrado.value?.id === data.id) {
      pedidoEncontrado.value = data
    }
    await cargarCola()
    mensajeOperacion.value = `Pedido ${data.referencia} actualizado a: ${etiquetaEstadoPedido(data.estadoPedido)}.`
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo actualizar el pedido')
    await cargarCola()
  }
}

async function confirmarVencimiento(pedido) {
  errorOperacion.value = ''
  try {
    const { data } = await api.post(`/personal/pedidos/${pedido.id}/vencer`)
    if (pedidoEncontrado.value?.id === data.id) {
      pedidoEncontrado.value = data
    }
    await cargarCola()
    mensajeOperacion.value = `Pedido ${data.referencia} marcado como no reclamado. Se registró una falta al estudiante.`
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo marcar como no reclamado')
    await cargarCola()
  }
}

async function alternarDisponibilidad(platillo) {
  errorOperacion.value = ''
  try {
    const nuevoEstado = !platillo.disponible
    await api.patch(`/personal/platillos/${platillo.id}/disponibilidad`, {
      disponible: nuevoEstado
    })
    await cargarCatalogo()
    mensajeOperacion.value = `${platillo.nombre} marcado como ${nuevoEstado ? 'disponible' : 'agotado'}.`
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo cambiar la disponibilidad')
  }
}

async function guardarEdicionPlatillo(payload, onExito) {
  errorOperacion.value = ''
  try {
    await api.put(`/personal/platillos/regular/${payload.id}`, {
      nombre: payload.nombre,
      descripcion: payload.descripcion,
      precio: payload.precio,
      turno: payload.turno
    })
    onExito?.()
    await cargarCatalogo()
    mensajeOperacion.value = 'Platillo actualizado en el menú.'
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo actualizar el platillo')
  }
}

async function publicarComidaDelDia(payload, onExito) {
  errorOperacion.value = ''
  try {
    await api.post('/personal/platillos/comida-del-dia', payload)
    onExito?.()
    await cargarCatalogo()
    mensajeOperacion.value = `Comida del día publicada para el turno ${payload.turno}.`
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo publicar la comida del día')
  }
}

async function agregarPlatilloRegular(payload, onExito) {
  errorOperacion.value = ''
  try {
    await api.post('/personal/platillos/regular', payload)
    onExito?.()
    await cargarCatalogo()
    mensajeOperacion.value = 'Platillo agregado al menú.'
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo agregar el platillo')
  }
}

onMounted(async () => {
  try {
    await Promise.all([cargarCola(), cargarCatalogo()])
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo cargar la información inicial')
  }

  timer = setInterval(() => {
    ahoraMs.value = Date.now()
  }, 1000)

  respaldoTimer = setInterval(() => {
    if (!socketConectado.value) {
      const tareas = [cargarCola(), cargarCatalogo()]
      if (pestana.value === 'historico') {
        tareas.push(cargarHistorico())
      }
      Promise.all(tareas).catch(() => {})
    }
  }, 10000)

  desuscripciones.push(
    suscribirEvento('nuevo_pedido', () => {
      cargarCola()
    }),
    suscribirEvento('pedido_actualizado', (actualizado) => {
      cargarCola()
      if (pedidoEncontrado.value?.id === actualizado.id) {
        pedidoEncontrado.value = actualizado
      }
      if (pestana.value === 'historico') {
        cargarHistorico()
      }
    }),
    suscribirEvento('catalogo_actualizado', () => {
      cargarCatalogo()
    })
  )
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (respaldoTimer) clearInterval(respaldoTimer)
  desuscripciones.forEach((fn) => fn())
})
</script>
