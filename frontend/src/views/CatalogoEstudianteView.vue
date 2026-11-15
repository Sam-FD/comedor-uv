<template>
  <div class="max-w-6xl mx-auto px-4 pt-6 pb-24 lg:pb-8 space-y-6">
    <BannerRestriccion v-if="sesion.usuario" :usuario="sesion.usuario" />

    <div
      v-if="!catalogo.recepcionAbierta"
      class="rounded-xl border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-900 shadow-sm"
    >
      <p class="font-semibold">Recepcion de pedidos cerrada temporalmente</p>
      <p class="mt-0.5">El personal pauso los pedidos en linea. Puedes seguir consultando la disponibilidad del menu.</p>
    </div>

    <div
      v-if="avisoEnVivo"
      class="rounded-xl border border-uv-verde/40 bg-uv-verde-claro px-4 py-3 text-sm text-uv-verde-oscuro shadow-sm flex items-center justify-between gap-2"
    >
      <span class="font-medium">{{ avisoEnVivo }}</span>
      <button type="button" @click="avisoEnVivo = ''" class="text-xs font-semibold underline shrink-0">Ocultar</button>
    </div>

    <PaseRecoleccion
      v-if="pedidoActivo"
      :pedido="pedidoActivo"
      @cancelado="manejarPedidoCancelado"
      @vencido="manejarPedidoVencido"
    />

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
      <!-- Columna izquierda: selector de turno, comida del dia y menu regular -->
      <div class="lg:col-span-2 space-y-6">
        <div class="flex flex-wrap items-center justify-between gap-3 bg-white border border-slate-200 rounded-xl p-4 shadow-sm">
          <div>
            <h2 class="text-lg font-bold text-slate-900">Menu disponible hoy</h2>
            <p class="text-xs text-slate-500">
              Jornada {{ catalogo.fechaOperativa || 'vigente' }} · Disponibilidad actualizada en vivo
            </p>
          </div>

          <div class="inline-flex rounded-lg bg-slate-100 p-1 text-sm">
            <button
              type="button"
              @click="cambiarTurno('matutino')"
              class="px-3.5 py-1.5 rounded-md font-medium transition-colors"
              :class="turnoSeleccionado === 'matutino' ? 'bg-white text-uv-azul shadow-sm' : 'text-slate-600 hover:text-slate-900'"
            >
              Matutino
            </button>
            <button
              type="button"
              @click="cambiarTurno('vespertino')"
              class="px-3.5 py-1.5 rounded-md font-medium transition-colors"
              :class="turnoSeleccionado === 'vespertino' ? 'bg-white text-uv-azul shadow-sm' : 'text-slate-600 hover:text-slate-900'"
            >
              Vespertino
            </button>
          </div>
        </div>

        <!-- Comida del dia -->
        <section class="space-y-3">
          <div class="flex items-center justify-between">
            <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500">
              Comida del dia ({{ turnoSeleccionado }})
            </h3>
            <span class="text-xs text-uv-verde-oscuro font-medium">Menu completo del dia</span>
          </div>

          <div v-if="comidasDelTurno.length > 0" class="grid grid-cols-1 gap-3">
            <TarjetaPlatillo
              v-for="comida in comidasDelTurno"
              :key="comida.id"
              :platillo="comida"
              :destacado="true"
              :deshabilitado="bloqueoAgregar"
              :cantidad-en-carrito="cantidadEnCarrito(comida.id)"
              @agregar="agregarAlCarrito"
            />
          </div>
          <div v-else class="bg-white border border-slate-200 rounded-xl p-5 text-sm text-slate-500 shadow-sm">
            El personal aun no publica la comida del dia para el turno {{ turnoSeleccionado }}.
          </div>
        </section>

        <!-- Menu regular -->
        <section class="space-y-3">
          <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500">
            Menu regular
          </h3>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            <TarjetaPlatillo
              v-for="platillo in menuRegularDelTurno"
              :key="platillo.id"
              :platillo="platillo"
              :deshabilitado="bloqueoAgregar"
              :cantidad-en-carrito="cantidadEnCarrito(platillo.id)"
              @agregar="agregarAlCarrito"
            />
          </div>
        </section>

        <!-- Recordatorio visible en pantallas pequenas debajo del menu -->
        <div class="lg:hidden">
          <RecordatorioReglas />
        </div>
      </div>

      <!-- Columna derecha en escritorio: carrito y recuadro de reglas -->
      <aside class="hidden lg:block space-y-4 lg:sticky lg:top-6">
        <div class="bg-white border border-slate-200 rounded-xl p-5 shadow-sm">
          <CarritoPanel
            :carrito="carrito"
            :turno-seleccionado="turnoSeleccionado"
            v-model:modalidad-pago="modalidadPago"
            :total-carrito="totalCarrito"
            :recepcion-abierta="catalogo.recepcionAbierta"
            :sin-pedidos-en-linea="sinPedidosEnLinea"
            :solo-app-tarjeta="soloAppTarjeta"
            :pedido-activo="pedidoActivo"
            :bloqueo-agregar="bloqueoAgregar"
            :enviando="enviando"
            :error-pedido="errorPedido"
            :estado-pasarela="estadoPasarela"
            @aumentar="aumentarCantidad"
            @disminuir="disminuirCantidad"
            @enviar="enviarPedido"
          />
        </div>

        <RecordatorioReglas />
      </aside>
    </div>

    <!-- Barra fija inferior en movil cuando hay platillos en el carrito -->
    <div
      v-if="carrito.length > 0 && !bloqueoAgregar"
      class="lg:hidden fixed bottom-0 inset-x-0 bg-white border-t border-slate-200 px-4 pt-3 pb-[max(0.75rem,var(--safe-area-inset-bottom,0px))] flex items-center justify-between gap-3 z-30 shadow-md"
    >
      <div>
        <p class="text-xs text-slate-500">
          {{ totalArticulos }} {{ totalArticulos === 1 ? 'platillo' : 'platillos' }} · Turno {{ turnoSeleccionado }}
        </p>
        <p class="text-base font-bold text-slate-900 tabular-nums">
          {{ formatearMoneda(totalCarrito) }}
        </p>
      </div>
      <button
        type="button"
        @click="mostrarCarritoMovil = true"
        class="px-4 py-2.5 rounded-lg bg-uv-azul hover:bg-uv-azul-oscuro text-white text-sm font-semibold"
      >
        Ver tu pedido
      </button>
    </div>

    <!-- Panel deslizable movil para revisar carrito y pagar -->
    <div
      v-if="mostrarCarritoMovil"
      class="lg:hidden fixed inset-0 bg-slate-900/40 z-40 flex items-end"
      @click.self="mostrarCarritoMovil = false"
    >
      <div class="w-full bg-white rounded-t-2xl border-t border-slate-200 p-5 max-h-[85vh] overflow-y-auto space-y-3">
        <div class="flex justify-end">
          <button
            type="button"
            data-cerrar-modal-movil
            @click="mostrarCarritoMovil = false"
            class="text-xs font-medium text-slate-500 hover:text-slate-800"
          >
            Cerrar
          </button>
        </div>

        <CarritoPanel
          :carrito="carrito"
          :turno-seleccionado="turnoSeleccionado"
          v-model:modalidad-pago="modalidadPago"
          :total-carrito="totalCarrito"
          :recepcion-abierta="catalogo.recepcionAbierta"
          :sin-pedidos-en-linea="sinPedidosEnLinea"
          :solo-app-tarjeta="soloAppTarjeta"
          :pedido-activo="pedidoActivo"
          :bloqueo-agregar="bloqueoAgregar"
          :enviando="enviando"
          :error-pedido="errorPedido"
          :estado-pasarela="estadoPasarela"
          @aumentar="aumentarCantidad"
          @disminuir="disminuirCantidad"
          @enviar="enviarPedido"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import api, { extraerMensajeError } from '../services/api'
import { suscribirEvento, socketConectado } from '../services/socket'
import { actualizarUsuarioEnSesion, refrescarPerfil, sesion } from '../stores/sesion'
import { formatearMoneda } from '../utils/formatos'
import BannerRestriccion from '../components/BannerRestriccion.vue'
import TarjetaPlatillo from '../components/TarjetaPlatillo.vue'
import PaseRecoleccion from '../components/PaseRecoleccion.vue'
import CarritoPanel from '../components/CarritoPanel.vue'
import RecordatorioReglas from '../components/RecordatorioReglas.vue'

const catalogo = ref({
  fechaOperativa: '',
  turnoSugerido: 'matutino',
  recepcionAbierta: true,
  comidasDelDia: [],
  menuRegular: []
})
const turnoSeleccionado = ref('matutino')
const carrito = ref([])
const modalidadPago = ref('app_tarjeta')
const pedidoActivo = ref(null)
const enviando = ref(false)
const errorPedido = ref('')
const avisoEnVivo = ref('')
const mostrarCarritoMovil = ref(false)
const estadoPasarela = ref(null)

const desuscripciones = []
let turnoInicialAplicado = false

const sinPedidosEnLinea = computed(() => sesion.usuario?.nivelRestriccion === 'sin_pedidos_en_linea')
const soloAppTarjeta = computed(() => sesion.usuario?.nivelRestriccion === 'sin_mostrador')
const bloqueoAgregar = computed(
  () => !catalogo.value.recepcionAbierta || sinPedidosEnLinea.value || Boolean(pedidoActivo.value)
)

const comidasDelTurno = computed(() => {
  return catalogo.value.comidasDelDia.filter((c) => c.turno === turnoSeleccionado.value)
})

const menuRegularDelTurno = computed(() => {
  return catalogo.value.menuRegular.filter(
    (p) => p.turno === 'ambos' || p.turno === turnoSeleccionado.value
  )
})

const totalCarrito = computed(() => {
  return carrito.value.reduce((acc, item) => acc + Number(item.platillo.precio) * item.cantidad, 0)
})

const totalArticulos = computed(() => {
  return carrito.value.reduce((acc, item) => acc + item.cantidad, 0)
})

async function cargarCatalogo() {
  const { data } = await api.get('/platillos')
  catalogo.value = data

  if (!turnoInicialAplicado) {
    turnoSeleccionado.value = data.turnoSugerido || 'matutino'
    turnoInicialAplicado = true
  }

  const todos = [...data.comidasDelDia, ...data.menuRegular]
  const mapa = new Map(todos.map((p) => [p.id, p]))
  const retirados = []
  carrito.value = carrito.value.filter((item) => {
    const vigente = mapa.get(item.platillo.id)
    const turnoCompatible =
      vigente &&
      (vigente.tipoMenu === 'comida_del_dia'
        ? vigente.turno === turnoSeleccionado.value
        : vigente.turno === 'ambos' || vigente.turno === turnoSeleccionado.value)
    if (!vigente || !vigente.disponible || !turnoCompatible) {
      retirados.push(item.platillo.nombre)
      return false
    }
    item.platillo = vigente
    return true
  })

  if (retirados.length > 0) {
    avisoEnVivo.value = `Se retiro de tu carrito por actualizacion de disponibilidad o turno: ${retirados.join(', ')}.`
  }
}

async function cargarPedidoActivo() {
  const { data } = await api.get('/pedidos/mis-pedidos')
  const activos = ['recibido', 'en_preparacion', 'listo_para_recoger']
  pedidoActivo.value = data.find((p) => activos.includes(p.estadoPedido)) || null
}

function cambiarTurno(nuevoTurno) {
  turnoSeleccionado.value = nuevoTurno
  errorPedido.value = ''
  carrito.value = carrito.value.filter((item) => {
    if (item.platillo.tipoMenu === 'comida_del_dia') {
      return item.platillo.turno === nuevoTurno
    }
    return item.platillo.turno === 'ambos' || item.platillo.turno === nuevoTurno
  })
}

function agregarAlCarrito(platillo) {
  if (bloqueoAgregar.value || !platillo.disponible) return
  errorPedido.value = ''
  const existente = carrito.value.find((item) => item.platillo.id === platillo.id)
  if (existente) {
    existente.cantidad += 1
  } else {
    carrito.value.push({ platillo, cantidad: 1 })
  }
}

function aumentarCantidad(platilloId) {
  const item = carrito.value.find((i) => i.platillo.id === platilloId)
  if (item) item.cantidad += 1
}

function disminuirCantidad(platilloId) {
  const idx = carrito.value.findIndex((i) => i.platillo.id === platilloId)
  if (idx === -1) return
  if (carrito.value[idx].cantidad > 1) {
    carrito.value[idx].cantidad -= 1
  } else {
    carrito.value.splice(idx, 1)
  }
  if (carrito.value.length === 0) {
    mostrarCarritoMovil.value = false
  }
}

function cantidadEnCarrito(platilloId) {
  return carrito.value.find((i) => i.platillo.id === platilloId)?.cantidad || 0
}

function manejarPedidoCancelado(pedidoCancelado) {
  pedidoActivo.value = null
  avisoEnVivo.value = `Pedido ${pedidoCancelado.referencia} cancelado correctamente.`
}

async function manejarPedidoVencido() {
  await Promise.all([cargarPedidoActivo(), refrescarPerfil()])
  if (!pedidoActivo.value) {
    avisoEnVivo.value = 'El tiempo de recoleccion vencio. Consulta el detalle en Mis pedidos.'
  }
}

async function enviarPedido(datosPago = {}) {
  errorPedido.value = ''
  enviando.value = true
  try {
    const modalidadFinal = soloAppTarjeta.value ? 'app_tarjeta' : modalidadPago.value
    const payload = {
      turno: turnoSeleccionado.value,
      modalidadPago: modalidadFinal,
      items: carrito.value.map((i) => ({
        platilloId: i.platillo.id,
        cantidad: i.cantidad
      })),
      tarjetaPrueba: modalidadFinal === 'app_tarjeta' ? (datosPago?.tarjetaPrueba || null) : null
    }
    const { data } = await api.post('/pedidos', payload)
    pedidoActivo.value = data
    carrito.value = []
    mostrarCarritoMovil.value = false
    avisoEnVivo.value = `Pedido ${data.referencia} enviado a cocina.`
  } catch (err) {
    errorPedido.value = extraerMensajeError(err, 'No se pudo enviar el pedido')
    await cargarCatalogo()
    await refrescarPerfil()
  } finally {
    enviando.value = false
  }
}

async function cargarEstadoPasarela() {
  try {
    const { data } = await api.get('/pagos/estado')
    estadoPasarela.value = data
  } catch {
    estadoPasarela.value = null
  }
}

let respaldoTimer = null

onMounted(async () => {
  await refrescarPerfil()
  if (soloAppTarjeta.value) {
    modalidadPago.value = 'app_tarjeta'
  }
  await Promise.all([cargarCatalogo(), cargarPedidoActivo(), cargarEstadoPasarela()])

  respaldoTimer = setInterval(() => {
    if (!socketConectado.value) {
      Promise.all([cargarCatalogo(), cargarPedidoActivo(), refrescarPerfil()]).catch(() => {})
    }
  }, 10000)

  desuscripciones.push(
    suscribirEvento('catalogo_actualizado', () => {
      cargarCatalogo()
    }),
    suscribirEvento('pedido_actualizado', async (pedido) => {
      if (['entregado', 'no_reclamado', 'cancelado'].includes(pedido.estadoPedido)) {
        if (pedidoActivo.value && pedidoActivo.value.id === pedido.id) {
          pedidoActivo.value = null
        }
        if (pedido.estadoPedido === 'entregado') {
          avisoEnVivo.value = `Tu pedido ${pedido.referencia} fue entregado.`
        } else if (pedido.estadoPedido === 'no_reclamado') {
          avisoEnVivo.value = `Tu pedido ${pedido.referencia} vencio y fue marcado como no reclamado.`
        } else {
          avisoEnVivo.value = `Tu pedido ${pedido.referencia} fue cancelado.`
        }
        await refrescarPerfil()
      } else {
        pedidoActivo.value = pedido
        if (pedido.estadoPedido === 'listo_para_recoger') {
          avisoEnVivo.value = `Tu pedido ${pedido.referencia} ya esta listo para recoger en mostrador.`
        }
      }
    }),
    suscribirEvento('usuario_actualizado', (usuario) => {
      actualizarUsuarioEnSesion(usuario)
      if (usuario.nivelRestriccion === 'sin_mostrador') {
        modalidadPago.value = 'app_tarjeta'
      }
    })
  )
})

onUnmounted(() => {
  if (respaldoTimer) clearInterval(respaldoTimer)
  desuscripciones.forEach((fn) => fn())
})
</script>
