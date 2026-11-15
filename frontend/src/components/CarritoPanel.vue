<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between border-b border-slate-100 pb-3">
      <h2 class="text-base font-bold text-slate-900">Tu pedido</h2>
      <span class="text-xs px-2 py-0.5 rounded bg-slate-100 text-slate-700 font-medium">
        Turno {{ turnoSeleccionado }}
      </span>
    </div>

    <div v-if="!recepcionAbierta" class="rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-amber-900">
      El comedor cerro temporalmente la recepcion de pedidos en linea.
    </div>

    <div v-else-if="sinPedidosEnLinea" class="rounded-lg border border-red-200 bg-red-50 p-3 text-xs text-red-800">
      Tienes suspendidos los pedidos en linea. Solo puedes consultar el catalogo vigente.
    </div>

    <div v-else-if="pedidoActivo" class="rounded-lg border border-uv-azul/30 bg-uv-azul-claro p-3 text-xs text-uv-azul">
      Ya tienes el pedido {{ pedidoActivo.referencia }} en curso. Recogelo o cancelalo si aun esta en recibido antes de hacer uno nuevo.
    </div>

    <div v-if="carrito.length === 0" class="py-6 text-center text-sm text-slate-500">
      Agrega la comida del dia o platillos del menu para iniciar tu orden.
    </div>

    <ul v-else class="divide-y divide-slate-100 text-sm">
      <li v-for="item in carrito" :key="item.platillo.id" class="py-2.5 flex items-start justify-between gap-2">
        <div class="min-w-0">
          <p class="font-medium text-slate-800 leading-snug">{{ item.platillo.nombre }}</p>
          <p class="text-xs text-slate-500 tabular-nums">
            {{ formatearMoneda(item.platillo.precio) }} c/u
          </p>
        </div>

        <div class="flex items-center gap-1.5 shrink-0">
          <button
            type="button"
            @click="$emit('disminuir', item.platillo.id)"
            class="w-7 h-7 rounded border border-slate-300 text-slate-700 hover:bg-slate-100 flex items-center justify-center"
          >
            -
          </button>
          <span class="w-5 text-center font-medium tabular-nums">{{ item.cantidad }}</span>
          <button
            type="button"
            @click="$emit('aumentar', item.platillo.id)"
            class="w-7 h-7 rounded border border-slate-300 text-slate-700 hover:bg-slate-100 flex items-center justify-center"
          >
            +
          </button>
        </div>
      </li>
    </ul>

    <div v-if="carrito.length > 0 && !bloqueoAgregar" class="space-y-4 pt-3 border-t border-slate-200">
      <div class="flex items-center justify-between text-base font-bold text-slate-900">
        <span>Total</span>
        <span class="tabular-nums text-uv-azul">{{ formatearMoneda(totalCarrito) }}</span>
      </div>

      <div class="space-y-2">
        <p class="text-xs font-semibold uppercase tracking-wide text-slate-600">
          Modalidad de pago
        </p>

        <label
          class="flex items-start gap-2.5 p-2.5 rounded border cursor-pointer text-sm"
          :class="modalidadPago === 'app_tarjeta' ? 'border-uv-azul bg-uv-azul-claro/40' : 'border-slate-200'"
        >
          <input
            :checked="modalidadPago === 'app_tarjeta'"
            @change="$emit('update:modalidadPago', 'app_tarjeta')"
            type="radio"
            value="app_tarjeta"
            class="mt-0.5 accent-uv-azul"
          />
          <div>
            <p class="font-medium text-slate-900">Tarjeta desde la aplicacion</p>
            <p class="text-xs text-slate-500">Se retiene el monto y se cobra hasta que recojas tu pedido.</p>
          </div>
        </label>

        <label
          class="flex items-start gap-2.5 p-2.5 rounded border text-sm"
          :class="[
            soloAppTarjeta ? 'border-slate-200 bg-slate-100 opacity-60 cursor-not-allowed' : 'cursor-pointer',
            modalidadPago === 'mostrador' && !soloAppTarjeta ? 'border-uv-azul bg-uv-azul-claro/40' : 'border-slate-200'
          ]"
        >
          <input
            :checked="modalidadPago === 'mostrador'"
            @change="$emit('update:modalidadPago', 'mostrador')"
            type="radio"
            value="mostrador"
            :disabled="soloAppTarjeta"
            class="mt-0.5 accent-uv-azul"
          />
          <div>
            <p class="font-medium text-slate-900">Pago en mostrador</p>
            <p v-if="soloAppTarjeta" class="text-xs text-amber-800">
              Deshabilitado temporalmente por acumulacion de faltas.
            </p>
            <p v-else class="text-xs text-slate-500">
              Efectivo o tarjeta fisica en caja presentando tu codigo de pedido.
            </p>
          </div>
        </label>
      </div>

      <div v-if="modalidadPago === 'app_tarjeta'" class="rounded-lg border border-slate-200 bg-slate-50 p-3 space-y-2.5 text-xs">
        <div class="flex flex-wrap items-center justify-between gap-2">
          <div class="flex items-center gap-1.5">
            <p class="font-semibold text-slate-800">Pasarela de prueba</p>
            <span
              class="px-1.5 py-0.5 rounded text-[10px] font-semibold"
              :class="esStripeReal ? 'bg-uv-verde-claro text-uv-verde-oscuro' : 'bg-uv-azul-claro text-uv-azul'"
            >
              {{ esStripeReal ? 'Stripe Test' : 'Sandbox local' }}
            </span>
          </div>
          <div class="flex items-center gap-2">
            <button
              type="button"
              @click="rellenarTarjetaPrueba('aprobada')"
              class="text-uv-azul hover:underline font-medium"
            >
              Aprobada (4242)
            </button>
            <span class="text-slate-300">|</span>
            <button
              type="button"
              @click="rellenarTarjetaPrueba('declinada')"
              class="text-slate-600 hover:text-red-700 hover:underline font-medium"
            >
              Rechazo (0002)
            </button>
          </div>
        </div>

        <div>
          <label class="block text-slate-600 mb-1">Titular de la tarjeta</label>
          <input
            v-model="tarjeta.titular"
            type="text"
            placeholder="Nombre como aparece en la tarjeta"
            class="w-full rounded border border-slate-300 bg-white px-2.5 py-1.5 text-xs focus:outline-none focus:border-uv-azul"
          />
        </div>

        <div>
          <label class="block text-slate-600 mb-1">Numero de tarjeta (16 digitos)</label>
          <input
            :value="tarjeta.numero"
            @input="formatearNumeroTarjeta"
            type="text"
            maxlength="19"
            inputmode="numeric"
            placeholder="4532 1000 2000 3000"
            class="w-full rounded border border-slate-300 bg-white px-2.5 py-1.5 text-xs tabular-nums focus:outline-none focus:border-uv-azul"
          />
        </div>

        <div class="grid grid-cols-2 gap-2">
          <div>
            <label class="block text-slate-600 mb-1">Vigencia (MM/AA)</label>
            <input
              :value="tarjeta.vigencia"
              @input="formatearVigencia"
              type="text"
              maxlength="5"
              inputmode="numeric"
              placeholder="08/29"
              class="w-full rounded border border-slate-300 bg-white px-2.5 py-1.5 text-xs tabular-nums focus:outline-none focus:border-uv-azul"
            />
          </div>
          <div>
            <label class="block text-slate-600 mb-1">CVV</label>
            <input
              :value="tarjeta.cvv"
              @input="formatearCvv"
              type="password"
              maxlength="4"
              inputmode="numeric"
              placeholder="123"
              class="w-full rounded border border-slate-300 bg-white px-2.5 py-1.5 text-xs tabular-nums focus:outline-none focus:border-uv-azul"
            />
          </div>
        </div>

        <p class="text-[11px] text-slate-500 leading-snug">
          Solo se retiene {{ formatearMoneda(totalCarrito) }} hasta la entrega. No almacenamos tus datos bancarios.
        </p>
      </div>

      <div v-if="errorLocal || errorPedido" class="rounded border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-800">
        {{ errorLocal || errorPedido }}
      </div>

      <button
        type="button"
        :disabled="enviando"
        @click="intentarEnviar"
        class="w-full py-2.5 rounded-lg bg-uv-azul hover:bg-uv-azul-oscuro text-white text-sm font-semibold transition-colors disabled:opacity-60"
      >
        {{ enviando ? 'Enviando pedido...' : 'Confirmar y enviar a cocina' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { formatearMoneda } from '../utils/formatos'

const props = defineProps({
  carrito: { type: Array, required: true },
  turnoSeleccionado: { type: String, required: true },
  modalidadPago: { type: String, required: true },
  totalCarrito: { type: Number, required: true },
  recepcionAbierta: { type: Boolean, default: true },
  sinPedidosEnLinea: { type: Boolean, default: false },
  soloAppTarjeta: { type: Boolean, default: false },
  pedidoActivo: { type: Object, default: null },
  bloqueoAgregar: { type: Boolean, default: false },
  enviando: { type: Boolean, default: false },
  errorPedido: { type: String, default: '' },
  estadoPasarela: { type: Object, default: null }
})

const emit = defineEmits(['aumentar', 'disminuir', 'update:modalidadPago', 'enviar'])

const esStripeReal = computed(() => props.estadoPasarela?.modo === 'stripe_test')

const tarjeta = reactive({
  titular: '',
  numero: '',
  vigencia: '',
  cvv: ''
})

const errorLocal = ref('')

function formatearNumeroTarjeta(evento) {
  const digitos = evento.target.value.replace(/\D/g, '').slice(0, 16)
  tarjeta.numero = digitos.replace(/(\d{4})(?=\d)/g, '$1 ')
}

function formatearVigencia(evento) {
  const digitos = evento.target.value.replace(/\D/g, '').slice(0, 4)
  tarjeta.vigencia = digitos.length > 2 ? `${digitos.slice(0, 2)}/${digitos.slice(2)}` : digitos
}

function formatearCvv(evento) {
  tarjeta.cvv = evento.target.value.replace(/\D/g, '').slice(0, 4)
}

function rellenarTarjetaPrueba(tipo = 'aprobada') {
  tarjeta.titular = 'Estudiante UV'
  tarjeta.numero = tipo === 'declinada' ? '4000 0000 0000 0002' : '4242 4242 4242 4242'
  tarjeta.vigencia = '09/29'
  tarjeta.cvv = '412'
  errorLocal.value = ''
}

function intentarEnviar() {
  errorLocal.value = ''
  let digitos = ''
  if (props.modalidadPago === 'app_tarjeta') {
    digitos = tarjeta.numero.replace(/\s+/g, '')
    if (!tarjeta.titular.trim()) {
      errorLocal.value = 'Ingresa el nombre del titular de la tarjeta'
      return
    }
    if (!/^\d{16}$/.test(digitos)) {
      errorLocal.value = 'El numero de tarjeta debe tener 16 digitos'
      return
    }
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(tarjeta.vigencia.trim())) {
      errorLocal.value = 'Ingresa la vigencia en formato MM/AA'
      return
    }
    const [mesStr, anioStr] = tarjeta.vigencia.trim().split('/')
    const ahora = new Date()
    const anioActual = ahora.getFullYear() % 100
    const mesActual = ahora.getMonth() + 1
    const anioTarjeta = Number(anioStr)
    const mesTarjeta = Number(mesStr)
    if (anioTarjeta < anioActual || (anioTarjeta === anioActual && mesTarjeta < mesActual)) {
      errorLocal.value = 'La tarjeta ingresada se encuentra vencida'
      return
    }
    if (!/^\d{3,4}$/.test(tarjeta.cvv.trim())) {
      errorLocal.value = 'Ingresa un CVV valido de 3 o 4 digitos'
      return
    }
  }
  emit('enviar', { tarjetaPrueba: digitos || null })
}
</script>
