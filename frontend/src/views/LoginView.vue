<template>
  <div class="flex-1 bg-[#F4F6F9] flex flex-col justify-center py-8 px-4">
    <div class="max-w-md w-full mx-auto">
      <div class="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden">
        <div class="bg-uv-azul text-white px-6 py-5 border-b-[3px] border-uv-verde">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-white text-uv-azul font-bold flex items-center justify-center text-sm border-b-2 border-uv-verde shrink-0">
              UV
            </div>
            <div>
              <p class="text-[11px] uppercase tracking-wider text-blue-100">Universidad Veracruzana</p>
              <h1 class="text-xl font-bold leading-tight">Comedor Universitario</h1>
            </div>
          </div>
          <p class="text-xs text-blue-100 mt-3">
            Consulta la disponibilidad en tiempo real y realiza tu pedido antes de pasar al mostrador.
          </p>
        </div>

        <div class="flex border-b border-slate-200 text-sm font-medium bg-slate-50">
          <button
            type="button"
            @click="cambiarModo('login')"
            class="flex-1 py-3 text-center border-b-2 transition-colors"
            :class="modo === 'login' ? 'border-uv-azul text-uv-azul bg-white' : 'border-transparent text-slate-500 hover:text-slate-700'"
          >
            Iniciar sesión
          </button>
          <button
            type="button"
            @click="cambiarModo('registro')"
            class="flex-1 py-3 text-center border-b-2 transition-colors"
            :class="modo === 'registro' ? 'border-uv-azul text-uv-azul bg-white' : 'border-transparent text-slate-500 hover:text-slate-700'"
          >
            Registrar estudiante
          </button>
        </div>

        <div class="p-6">
          <div v-if="error" class="mb-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2.5 text-sm text-red-800">
            {{ error }}
          </div>

          <form v-if="modo === 'login'" @submit.prevent="enviarLogin" class="space-y-4">
            <div>
              <label class="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                Matrícula o correo institucional
              </label>
              <input
                v-model="formLogin.credencialOCorreo"
                type="text"
                required
                placeholder="Ej. S22014510 o l.mendoza@estudiantes.uv.mx"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-uv-azul/20 focus:border-uv-azul"
              />
            </div>

            <div>
              <label class="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                Contraseña
              </label>
              <input
                v-model="formLogin.password"
                type="password"
                required
                placeholder="Ingresa tu contraseña"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-uv-azul/20 focus:border-uv-azul"
              />
            </div>

            <button
              type="submit"
              :disabled="cargando"
              class="w-full py-2.5 rounded-lg bg-uv-azul hover:bg-uv-azul-oscuro text-white text-sm font-semibold transition-colors disabled:opacity-60"
            >
              {{ cargando ? 'Ingresando...' : 'Entrar al comedor' }}
            </button>
          </form>

          <form v-else @submit.prevent="enviarRegistro" class="space-y-3.5">
            <div>
              <label class="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1">Matrícula UV</label>
              <input
                v-model="formRegistro.credencialInstitucional"
                type="text"
                required
                placeholder="Ej. S24011223"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-uv-azul/20 focus:border-uv-azul"
              />
            </div>

            <div>
              <label class="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1">Nombre completo</label>
              <input
                v-model="formRegistro.nombre"
                type="text"
                required
                placeholder="Nombre y apellidos"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-uv-azul/20 focus:border-uv-azul"
              />
            </div>

            <div>
              <label class="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1">Correo institucional</label>
              <input
                v-model="formRegistro.correo"
                type="email"
                required
                placeholder="usuario@estudiantes.uv.mx"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-uv-azul/20 focus:border-uv-azul"
              />
            </div>

            <div>
              <label class="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1">Contraseña</label>
              <input
                v-model="formRegistro.password"
                type="password"
                required
                minlength="6"
                placeholder="Mínimo 6 caracteres"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-uv-azul/20 focus:border-uv-azul"
              />
            </div>

            <button
              type="submit"
              :disabled="cargando"
              class="w-full py-2.5 rounded-lg bg-uv-verde hover:bg-uv-verde-oscuro text-white text-sm font-semibold transition-colors disabled:opacity-60"
            >
              {{ cargando ? 'Creando cuenta...' : 'Crear cuenta de estudiante' }}
            </button>
          </form>

          <p v-if="cargando && esperaExtendida" class="mt-3 text-xs text-center text-slate-500">
            Conectando con el servidor, el primer acceso tras inactividad puede tardar unos segundos...
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api, { extraerMensajeError } from '../services/api'
import { guardarSesion } from '../stores/sesion'

const router = useRouter()
const modo = ref('login')
const cargando = ref(false)
const esperaExtendida = ref(false)
const error = ref('')
let temporizadorEspera = null

const formLogin = reactive({
  credencialOCorreo: '',
  password: ''
})

const formRegistro = reactive({
  credencialInstitucional: '',
  nombre: '',
  correo: '',
  password: ''
})

function cambiarModo(nuevoModo) {
  modo.value = nuevoModo
  error.value = ''
}

function redirigirPorRol(rol) {
  router.push(rol === 'personal' ? '/personal' : '/')
}

function iniciarTemporizadorEspera() {
  detenerTemporizadorEspera()
  temporizadorEspera = setTimeout(() => {
    if (cargando.value) {
      esperaExtendida.value = true
    }
  }, 3500)
}

function detenerTemporizadorEspera() {
  if (temporizadorEspera) {
    clearTimeout(temporizadorEspera)
    temporizadorEspera = null
  }
  esperaExtendida.value = false
}

async function enviarLogin() {
  error.value = ''
  cargando.value = true
  iniciarTemporizadorEspera()
  try {
    const { data } = await api.post('/auth/login', formLogin)
    guardarSesion(data.token, data.usuario)
    redirigirPorRol(data.usuario.rol)
  } catch (err) {
    error.value = extraerMensajeError(err, 'No se pudo iniciar sesión')
  } finally {
    detenerTemporizadorEspera()
    cargando.value = false
  }
}

async function enviarRegistro() {
  error.value = ''
  cargando.value = true
  iniciarTemporizadorEspera()
  try {
    const { data } = await api.post('/auth/registro', formRegistro)
    guardarSesion(data.token, data.usuario)
    redirigirPorRol(data.usuario.rol)
  } catch (err) {
    error.value = extraerMensajeError(err, 'No se pudo completar el registro')
  } finally {
    detenerTemporizadorEspera()
    cargando.value = false
  }
}

onMounted(() => {
  api.get('/salud').catch(() => {})
})

onUnmounted(() => {
  detenerTemporizadorEspera()
})
</script>
