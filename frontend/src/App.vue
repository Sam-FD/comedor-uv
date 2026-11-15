<template>
  <div class="flex-1 flex flex-col bg-[#F4F6F9] text-slate-900">
    <BarraNavegacion v-if="sesion.usuario" />
    <main class="flex-1 flex flex-col">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import BarraNavegacion from './components/BarraNavegacion.vue'
import { sesion, refrescarPerfil } from './stores/sesion'

onMounted(() => {
  if (sesion.token) {
    refrescarPerfil()
  }
})
</script>
