<template>
  <header class="bg-uv-azul text-white border-b-[3px] border-uv-verde shadow-sm">
    <div class="max-w-6xl mx-auto px-4 py-2.5 flex flex-wrap items-center justify-between gap-x-3 gap-y-2">
      <div class="order-1 flex items-center gap-2.5 sm:gap-3 min-w-0">
        <div class="w-9 h-9 rounded-md bg-white text-uv-azul font-bold flex items-center justify-center text-sm tracking-tight border-b-2 border-uv-verde shrink-0">
          UV
        </div>
        <div class="min-w-0">
          <h1 class="text-sm sm:text-base font-semibold leading-tight tracking-tight truncate">Comedor Universitario</h1>
          <p class="text-[11px] text-blue-100/90 truncate">Universidad Veracruzana</p>
        </div>
      </div>

      <div class="order-2 md:order-3 flex items-center gap-2 sm:gap-2.5 shrink-0">
        <div
          class="flex items-center gap-1.5 text-xs bg-black/15 px-2.5 py-1 rounded-full text-blue-50"
          :title="socketConectado ? 'Tiempo real activo' : (socketDisponible ? 'Reconectando tiempo real con respaldo periódico' : 'Actualización automática periódica activa')"
        >
          <span
            class="w-2 h-2 rounded-full"
            :class="socketConectado ? 'bg-uv-verde' : (socketDisponible ? 'bg-slate-300' : 'bg-sky-300')"
          ></span>
          <span class="hidden sm:inline">{{ socketConectado ? 'En vivo' : (socketDisponible ? 'Reconectando' : 'En línea') }}</span>
        </div>

        <div class="flex items-center gap-2.5 pl-2.5 border-l border-white/20">
          <div class="text-right hidden md:block">
            <p class="text-xs font-medium leading-tight">{{ sesion.usuario?.nombre }}</p>
            <div class="flex items-center justify-end gap-1.5 mt-0.5">
              <span class="text-[11px] text-blue-100">{{ sesion.usuario?.credencialInstitucional }}</span>
              <span
                v-if="sesion.usuario?.rol === 'estudiante'"
                class="text-[10px] px-1.5 py-0.5 rounded font-medium"
                :class="claseFaltasHeader"
              >
                {{ textoFaltasHeader }}
              </span>
            </div>
          </div>
          <button
            type="button"
            @click="salir"
            class="px-3 py-1.5 text-xs font-medium rounded-md border border-white/30 hover:bg-white/10 transition-colors"
          >
            Salir
          </button>
        </div>
      </div>

      <nav class="order-3 md:order-2 w-full md:w-auto md:ml-auto flex items-center justify-between gap-2 pt-2 border-t border-white/15 md:pt-0 md:border-t-0 text-xs sm:text-sm">
        <div class="flex items-center gap-1.5">
          <template v-if="sesion.usuario?.rol === 'estudiante'">
            <RouterLink
              to="/"
              class="px-3 py-1.5 rounded-md transition-colors whitespace-nowrap"
              :class="route.name === 'catalogo' ? 'bg-white/15 font-medium text-white' : 'text-blue-100 hover:bg-white/10 hover:text-white'"
            >
              Menú
            </RouterLink>
            <RouterLink
              to="/mis-pedidos"
              class="px-3 py-1.5 rounded-md transition-colors whitespace-nowrap"
              :class="route.name === 'mis-pedidos' ? 'bg-white/15 font-medium text-white' : 'text-blue-100 hover:bg-white/10 hover:text-white'"
            >
              Mis pedidos
            </RouterLink>
          </template>

          <template v-if="sesion.usuario?.rol === 'personal'">
            <RouterLink
              to="/personal"
              class="px-3 py-1.5 rounded-md bg-white/15 font-medium text-white whitespace-nowrap"
            >
              Panel operativo
            </RouterLink>
          </template>
        </div>

        <div class="flex md:hidden items-center gap-1.5 shrink-0">
          <span class="text-[11px] text-blue-100 font-medium truncate max-w-[110px]">
            {{ sesion.usuario?.credencialInstitucional }}
          </span>
          <span
            v-if="sesion.usuario?.rol === 'estudiante'"
            class="text-[10px] px-1.5 py-0.5 rounded font-semibold"
            :class="claseFaltasHeader"
          >
            {{ textoFaltasHeader }}
          </span>
          <span
            v-else-if="sesion.usuario?.rol === 'personal'"
            class="text-[10px] px-1.5 py-0.5 rounded font-semibold bg-white/15 text-blue-50"
          >
            Personal
          </span>
        </div>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { sesion, cerrarSesion } from '../stores/sesion'
import { socketConectado, socketDisponible } from '../services/socket'

const route = useRoute()
const router = useRouter()

const textoFaltasHeader = computed(() => {
  const u = sesion.usuario
  if (!u) return ''
  if (u.nivelRestriccion === 'sin_pedidos_en_linea') return 'Suspendido'
  if (u.nivelRestriccion === 'sin_mostrador') return 'Solo tarjeta'
  return `${u.faltasAbandono || 0}/3 faltas`
})

const claseFaltasHeader = computed(() => {
  const u = sesion.usuario
  if (!u) return ''
  if (u.nivelRestriccion === 'sin_pedidos_en_linea') return 'bg-red-500/80 text-white'
  if (u.nivelRestriccion === 'sin_mostrador') return 'bg-amber-400/90 text-slate-900'
  return 'bg-white/15 text-blue-50'
})

function salir() {
  cerrarSesion()
  router.push('/login')
}
</script>
