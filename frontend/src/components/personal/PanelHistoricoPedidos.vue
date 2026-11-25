<template>
  <div class="space-y-5">
    <section class="bg-white border border-slate-200 rounded-xl p-4 shadow-sm grid grid-cols-1 sm:grid-cols-2 lg:flex lg:flex-wrap items-end gap-3 text-sm">
      <div>
        <label class="block text-xs font-medium text-slate-600 mb-1">Fecha operativa</label>
        <div class="flex items-center gap-1.5">
          <input
            v-model="filtros.fecha"
            type="date"
            class="w-full lg:w-auto rounded-lg border border-slate-300 px-2.5 py-1.5 text-sm focus:outline-none focus:border-uv-azul"
          />
          <button
            type="button"
            @click="filtros.fecha = ''; $emit('cargar')"
            class="px-2.5 py-1.5 rounded-lg border border-slate-300 text-xs text-slate-600 hover:bg-slate-50 shrink-0"
          >
            Todas
          </button>
        </div>
      </div>

      <div>
        <label class="block text-xs font-medium text-slate-600 mb-1">Turno</label>
        <select
          v-model="filtros.turno"
          class="w-full lg:w-auto rounded-lg border border-slate-300 px-3 py-1.5 text-sm bg-white focus:outline-none focus:border-uv-azul"
        >
          <option value="">Todos</option>
          <option value="matutino">Matutino</option>
          <option value="vespertino">Vespertino</option>
        </select>
      </div>

      <div>
        <label class="block text-xs font-medium text-slate-600 mb-1">Modalidad de pago</label>
        <select
          v-model="filtros.modalidadPago"
          class="w-full lg:w-auto rounded-lg border border-slate-300 px-3 py-1.5 text-sm bg-white focus:outline-none focus:border-uv-azul"
        >
          <option value="">Todas</option>
          <option value="app_tarjeta">Tarjeta en app</option>
          <option value="mostrador">En mostrador</option>
        </select>
      </div>

      <div>
        <label class="block text-xs font-medium text-slate-600 mb-1">Estado</label>
        <select
          v-model="filtros.estado"
          class="w-full lg:w-auto rounded-lg border border-slate-300 px-3 py-1.5 text-sm bg-white focus:outline-none focus:border-uv-azul"
        >
          <option value="">Todos</option>
          <option value="entregado">Entregados</option>
          <option value="no_reclamado">No reclamados</option>
        </select>
      </div>

      <button
        type="button"
        @click="$emit('cargar')"
        class="sm:col-span-2 lg:col-span-1 w-full lg:w-auto px-4 py-1.5 rounded-lg bg-uv-azul hover:bg-uv-azul-oscuro text-white text-sm font-medium"
      >
        Aplicar filtros
      </button>
    </section>

    <div class="grid grid-cols-2 lg:grid-cols-4 gap-3">
      <div class="bg-white border border-slate-200 border-l-4 border-l-uv-verde rounded-xl p-4 shadow-sm">
        <p class="text-xs text-slate-500">Entregados</p>
        <p class="text-2xl font-bold text-uv-verde-oscuro tabular-nums mt-1">{{ historico.totalEntregados }}</p>
      </div>
      <div class="bg-white border border-slate-200 border-l-4 border-l-red-500 rounded-xl p-4 shadow-sm">
        <p class="text-xs text-slate-500">No reclamados</p>
        <p class="text-2xl font-bold text-red-700 tabular-nums mt-1">{{ historico.totalNoReclamados }}</p>
      </div>
      <div class="bg-white border border-slate-200 border-l-4 border-l-uv-azul rounded-xl p-4 shadow-sm">
        <p class="text-xs text-slate-500">Cobrado en App</p>
        <p class="text-xl font-bold text-slate-900 tabular-nums mt-1">{{ formatearMoneda(historico.montoCobradoApp) }}</p>
      </div>
      <div class="bg-white border border-slate-200 border-l-4 border-l-amber-500 rounded-xl p-4 shadow-sm">
        <p class="text-xs text-slate-500">Cobrado en Mostrador</p>
        <p class="text-xl font-bold text-slate-900 tabular-nums mt-1">{{ formatearMoneda(historico.montoCobradoMostrador) }}</p>
      </div>
    </div>

    <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
      <ListaPedidosHistorial
        :pedidos="historico.pedidos"
        mostrar-estudiante
        mensaje-vacio="No hay pedidos cerrados con los filtros seleccionados."
      />
    </section>
  </div>
</template>

<script setup>
import ListaPedidosHistorial from '../ListaPedidosHistorial.vue'
import { formatearMoneda } from '../../utils/formatos'

defineProps({
  historico: { type: Object, required: true },
  filtros: { type: Object, required: true }
})

defineEmits(['cargar'])
</script>
