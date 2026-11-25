<template>
  <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
    <div class="lg:col-span-2 space-y-6">
      <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
        <div class="px-5 py-4 border-b border-slate-200">
          <h2 class="text-base font-bold text-slate-900">Comida del día ({{ catalogo.fechaOperativa }})</h2>
          <p class="text-xs text-slate-500">Apaga la disponibilidad en cuanto se termine para bloquear nuevos pedidos.</p>
        </div>

        <div v-if="catalogo.comidasDelDia.length === 0" class="p-5 text-sm text-slate-500">
          Aún no hay comidas del día publicadas hoy.
        </div>

        <ul v-else class="divide-y divide-slate-100">
          <li
            v-for="platillo in catalogo.comidasDelDia"
            :key="platillo.id"
            class="p-4 flex flex-wrap items-center justify-between gap-3"
          >
            <div>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-slate-900">{{ platillo.nombre }}</span>
                <span class="px-2 py-0.5 rounded-full text-xs bg-slate-100 text-slate-700">
                  {{ etiquetaTurno(platillo.turno) }}
                </span>
              </div>
              <p v-if="platillo.descripcion" class="text-xs text-slate-500 mt-0.5">{{ platillo.descripcion }}</p>
              <p class="text-xs font-semibold text-uv-azul mt-1 tabular-nums">{{ formatearMoneda(platillo.precio) }}</p>
            </div>

            <button
              type="button"
              @click="$emit('alternar-disponibilidad', platillo)"
              class="px-3.5 py-1.5 rounded-md text-xs font-semibold border transition-colors"
              :class="
                platillo.disponible
                  ? 'bg-uv-verde-claro border-uv-verde/40 text-uv-verde-oscuro hover:bg-red-50 hover:border-red-200 hover:text-red-700'
                  : 'bg-red-50 border-red-200 text-red-700 hover:bg-uv-verde-claro hover:border-uv-verde/40 hover:text-uv-verde-oscuro'
              "
            >
              {{ platillo.disponible ? 'Disponible (marcar agotado)' : 'Agotado (habilitar)' }}
            </button>
          </li>
        </ul>
      </section>

      <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
        <div class="px-5 py-4 border-b border-slate-200">
          <h2 class="text-base font-bold text-slate-900">Menú</h2>
          <p class="text-xs text-slate-500">Los platillos no se eliminan para conservar el histórico de ventas.</p>
        </div>

        <ul class="divide-y divide-slate-100">
          <li
            v-for="platillo in catalogo.menuRegular"
            :key="platillo.id"
            class="p-4 flex flex-wrap items-center justify-between gap-3"
          >
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <span class="font-semibold text-slate-900">{{ platillo.nombre }}</span>
                <span class="px-2 py-0.5 rounded-full text-xs bg-slate-100 text-slate-700">
                  {{ etiquetaTurno(platillo.turno) }}
                </span>
              </div>
              <p v-if="platillo.descripcion" class="text-xs text-slate-500 mt-0.5">{{ platillo.descripcion }}</p>
              <p class="text-xs font-semibold text-uv-azul mt-1 tabular-nums">{{ formatearMoneda(platillo.precio) }}</p>
            </div>

            <div class="flex items-center gap-2 shrink-0">
              <button
                type="button"
                @click="abrirEdicionPlatillo(platillo)"
                class="px-3 py-1.5 rounded-md text-xs font-medium border border-slate-300 text-slate-700 hover:bg-slate-50"
              >
                Editar
              </button>
              <button
                type="button"
                @click="$emit('alternar-disponibilidad', platillo)"
                class="px-3.5 py-1.5 rounded-md text-xs font-semibold border transition-colors"
                :class="
                  platillo.disponible
                    ? 'bg-uv-verde-claro border-uv-verde/40 text-uv-verde-oscuro hover:bg-red-50 hover:border-red-200 hover:text-red-700'
                    : 'bg-red-50 border-red-200 text-red-700 hover:bg-uv-verde-claro hover:border-uv-verde/40 hover:text-uv-verde-oscuro'
                "
              >
                {{ platillo.disponible ? 'Disponible' : 'Agotado' }}
              </button>
            </div>
          </li>
        </ul>
      </section>
    </div>

    <div class="space-y-6">
      <section class="bg-white border border-slate-200 rounded-xl p-5 shadow-sm">
        <h3 class="text-sm font-bold text-slate-900 mb-3">Publicar comida del día</h3>
        <form @submit.prevent="enviarComidaDelDia" class="space-y-3 text-sm">
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Turno</label>
            <select
              v-model="formComidaDia.turno"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm bg-white focus:outline-none focus:border-uv-azul"
            >
              <option value="matutino">Matutino</option>
              <option value="vespertino">Vespertino</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Nombre del platillo</label>
            <input
              v-model="formComidaDia.nombre"
              type="text"
              required
              placeholder="Ej. Milanesa de pollo con ensalada"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
            />
          </div>
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Complementos y agua</label>
            <textarea
              v-model="formComidaDia.descripcion"
              rows="2"
              placeholder="Arroz, frijoles y agua del día"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
            ></textarea>
          </div>
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Precio fijo (MXN)</label>
            <input
              v-model="formComidaDia.precio"
              type="number"
              step="0.50"
              min="1"
              required
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
            />
          </div>
          <button
            type="submit"
            class="w-full py-2.5 rounded-lg bg-uv-verde hover:bg-uv-verde-oscuro text-white text-sm font-semibold"
          >
            Publicar para hoy
          </button>
        </form>
      </section>

      <section class="bg-white border border-slate-200 rounded-xl p-5 shadow-sm">
        <h3 class="text-sm font-bold text-slate-900 mb-3">Agregar al menú</h3>
        <form @submit.prevent="enviarPlatilloRegular" class="space-y-3 text-sm">
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Nombre</label>
            <input
              v-model="formRegular.nombre"
              type="text"
              required
              placeholder="Ej. Sincronizadas"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
            />
          </div>
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Descripción</label>
            <textarea
              v-model="formRegular.descripcion"
              rows="2"
              placeholder="Ingredientes o porción"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
            ></textarea>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Precio</label>
              <input
                v-model="formRegular.precio"
                type="number"
                step="0.50"
                min="1"
                required
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
              />
            </div>
            <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Turno</label>
              <select
                v-model="formRegular.turno"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm bg-white focus:outline-none focus:border-uv-azul"
              >
                <option value="ambos">Ambos</option>
                <option value="matutino">Matutino</option>
                <option value="vespertino">Vespertino</option>
              </select>
            </div>
          </div>
          <button
            type="submit"
            class="w-full py-2.5 rounded-lg bg-uv-azul hover:bg-uv-azul-oscuro text-white text-sm font-semibold"
          >
            Guardar platillo
          </button>
        </form>
      </section>
    </div>

    <div
      v-if="platilloEnEdicion"
      class="fixed inset-0 bg-slate-900/40 z-50 flex items-center justify-center p-4"
      @click.self="platilloEnEdicion = null"
    >
      <div class="bg-white border border-slate-200 rounded-xl shadow-lg max-w-md w-full p-5 space-y-4 max-h-[90vh] overflow-y-auto">
        <h3 class="text-base font-bold text-slate-900">Editar platillo del menú</h3>
        <form @submit.prevent="enviarEdicionPlatillo" class="space-y-3 text-sm">
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Nombre</label>
            <input
              v-model="platilloEnEdicion.nombre"
              type="text"
              required
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
            />
          </div>
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Descripción</label>
            <textarea
              v-model="platilloEnEdicion.descripcion"
              rows="2"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
            ></textarea>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Precio (MXN)</label>
              <input
                v-model="platilloEnEdicion.precio"
                type="number"
                step="0.50"
                min="1"
                required
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
              />
            </div>
            <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Turno</label>
              <select
                v-model="platilloEnEdicion.turno"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm bg-white focus:outline-none focus:border-uv-azul"
              >
                <option value="ambos">Ambos</option>
                <option value="matutino">Matutino</option>
                <option value="vespertino">Vespertino</option>
              </select>
            </div>
          </div>
          <div class="flex justify-end gap-2 pt-2">
            <button
              type="button"
              @click="platilloEnEdicion = null"
              class="px-4 py-2 rounded-lg border border-slate-300 text-sm text-slate-700 hover:bg-slate-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              class="px-4 py-2 rounded-lg bg-uv-azul hover:bg-uv-azul-oscuro text-white text-sm font-semibold"
            >
              Guardar cambios
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { etiquetaTurno, formatearMoneda } from '../../utils/formatos'

const props = defineProps({
  catalogo: { type: Object, required: true }
})

const emit = defineEmits(['alternar-disponibilidad', 'publicar-comida-dia', 'agregar-regular', 'guardar-edicion'])

const platilloEnEdicion = ref(null)
let turnoComidaInicializado = false

const formComidaDia = reactive({
  turno: 'matutino',
  nombre: '',
  descripcion: '',
  precio: '30.00'
})

const formRegular = reactive({
  nombre: '',
  descripcion: '',
  precio: '',
  turno: 'ambos'
})

watch(
  () => props.catalogo?.turnoSugerido,
  (sugerido) => {
    if (!turnoComidaInicializado && sugerido) {
      formComidaDia.turno = sugerido
      turnoComidaInicializado = true
    }
  },
  { immediate: true }
)

function abrirEdicionPlatillo(platillo) {
  platilloEnEdicion.value = {
    id: platillo.id,
    nombre: platillo.nombre,
    descripcion: platillo.descripcion || '',
    precio: platillo.precio,
    turno: platillo.turno
  }
}

function enviarComidaDelDia() {
  emit(
    'publicar-comida-dia',
    {
      nombre: formComidaDia.nombre,
      descripcion: formComidaDia.descripcion,
      precio: Number(formComidaDia.precio),
      turno: formComidaDia.turno
    },
    () => {
      formComidaDia.nombre = ''
      formComidaDia.descripcion = ''
    }
  )
}

function enviarPlatilloRegular() {
  emit(
    'agregar-regular',
    {
      nombre: formRegular.nombre,
      descripcion: formRegular.descripcion,
      precio: Number(formRegular.precio),
      turno: formRegular.turno
    },
    () => {
      formRegular.nombre = ''
      formRegular.descripcion = ''
      formRegular.precio = ''
    }
  )
}

function enviarEdicionPlatillo() {
  if (!platilloEnEdicion.value) return
  emit(
    'guardar-edicion',
    {
      id: platilloEnEdicion.value.id,
      nombre: platilloEnEdicion.value.nombre,
      descripcion: platilloEnEdicion.value.descripcion,
      precio: Number(platilloEnEdicion.value.precio),
      turno: platilloEnEdicion.value.turno
    },
    () => {
      platilloEnEdicion.value = null
    }
  )
}

function manejarBotonAtras(e) {
  if (platilloEnEdicion.value) {
    e.preventDefault()
    platilloEnEdicion.value = null
  }
}

onMounted(() => window.addEventListener('app-back', manejarBotonAtras))
onUnmounted(() => window.removeEventListener('app-back', manejarBotonAtras))
</script>
