<template>
  <article
    class="bg-white border rounded-xl overflow-hidden shadow-sm flex flex-col justify-between transition-colors"
    :class="[
      destacado ? 'border-uv-verde/50' : 'border-slate-200 hover:border-uv-azul/40',
      !platillo.disponible ? 'bg-slate-50/80' : ''
    ]"
  >
    <div v-if="destacado" class="bg-uv-verde-claro border-b border-uv-verde/20 px-4 py-2 flex items-center justify-between gap-2">
      <span class="text-xs font-semibold text-uv-verde-oscuro uppercase tracking-wide">
        Comida del dia · Turno {{ platillo.turno }}
      </span>
      <span
        class="px-2 py-0.5 rounded-full text-[11px] font-semibold"
        :class="platillo.disponible ? 'bg-white text-uv-verde-oscuro border border-uv-verde/30' : 'bg-red-100 text-red-700'"
      >
        {{ platillo.disponible ? 'Disponible' : 'Agotado' }}
      </span>
    </div>

    <div class="p-4 flex-1 flex flex-col justify-between gap-3">
      <div>
        <div class="flex items-start justify-between gap-3">
          <h3
            class="font-semibold leading-snug"
            :class="[
              destacado ? 'text-base text-slate-900' : 'text-[15px] text-slate-900',
              !platillo.disponible ? 'text-slate-500' : ''
            ]"
          >
            {{ platillo.nombre }}
          </h3>
          <span
            class="shrink-0 px-2.5 py-0.5 rounded-md text-sm font-bold tabular-nums"
            :class="platillo.disponible ? 'bg-uv-azul-claro text-uv-azul' : 'bg-slate-200 text-slate-500'"
          >
            {{ formatearMoneda(platillo.precio) }}
          </span>
        </div>

        <p v-if="platillo.descripcion" class="text-sm text-slate-600 mt-1.5 leading-relaxed">
          {{ platillo.descripcion }}
        </p>
      </div>

      <div class="flex flex-wrap items-center justify-between gap-2 pt-3 border-t border-slate-100">
        <div class="flex flex-wrap items-center gap-2 text-xs">
          <span
            v-if="!destacado"
            class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full font-medium"
            :class="platillo.disponible ? 'bg-uv-verde-claro text-uv-verde-oscuro' : 'bg-red-50 text-red-700'"
          >
            <span class="w-1.5 h-1.5 rounded-full" :class="platillo.disponible ? 'bg-uv-verde' : 'bg-red-500'"></span>
            {{ platillo.disponible ? 'Disponible' : 'Agotado' }}
          </span>
          <span class="text-slate-500">
            {{ etiquetaTurno(platillo.turno) }}
          </span>
          <span
            v-if="cantidadEnCarrito > 0"
            class="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-uv-azul-claro text-uv-azul"
          >
            En tu pedido: {{ cantidadEnCarrito }}
          </span>
        </div>

        <button
          type="button"
          :disabled="!platillo.disponible || deshabilitado"
          @click="$emit('agregar', platillo)"
          class="px-3.5 py-1.5 rounded-md text-xs sm:text-sm font-semibold transition-colors"
          :class="claseBoton"
        >
          {{ textoBoton }}
        </button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { etiquetaTurno, formatearMoneda } from '../utils/formatos'

const props = defineProps({
  platillo: {
    type: Object,
    required: true
  },
  destacado: {
    type: Boolean,
    default: false
  },
  deshabilitado: {
    type: Boolean,
    default: false
  },
  cantidadEnCarrito: {
    type: Number,
    default: 0
  }
})

defineEmits(['agregar'])

const textoBoton = computed(() => {
  if (!props.platillo.disponible) return 'Agotado'
  if (props.cantidadEnCarrito > 0) return 'Agregar otro (+1)'
  return props.destacado ? '+ Agregar comida del dia' : '+ Agregar'
})

const claseBoton = computed(() => {
  if (!props.platillo.disponible || props.deshabilitado) {
    return 'bg-slate-100 text-slate-400 cursor-not-allowed'
  }
  if (props.destacado) {
    return 'bg-uv-verde hover:bg-uv-verde-oscuro text-white'
  }
  return 'bg-uv-azul hover:bg-uv-azul-oscuro text-white'
})
</script>
