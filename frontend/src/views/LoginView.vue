<template>
  <div class="min-h-[100dvh] bg-[#F4F6F9] flex flex-col justify-center py-8 px-4">
    <div class="max-w-md w-full mx-auto space-y-4">
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
            Iniciar sesion
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
                Matricula o correo institucional
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
              <label class="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1">Matricula UV</label>
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
                placeholder="Minimo 6 caracteres"
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
        </div>
      </div>

      <!-- Accesos rapidos para probar perfiles y restricciones (solo visible en modo demo) -->
      <div v-if="modoDemo" class="bg-white border border-slate-200 rounded-xl shadow-sm p-4 space-y-2.5">
        <div class="flex items-center justify-between">
          <h2 class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Cuentas de prueba
          </h2>
          <span class="text-xs text-slate-500">Contraseña: <strong class="font-semibold text-slate-700">uv2026</strong></span>
        </div>

        <div class="space-y-1.5">
          <button
            v-for="cuenta in cuentasDemo"
            :key="cuenta.credencial"
            type="button"
            @click="entrarConDemo(cuenta.credencial)"
            class="w-full text-left px-3 py-2 rounded-lg border border-slate-200 hover:border-uv-azul hover:bg-uv-azul-claro/40 transition-colors flex items-center justify-between gap-2 text-xs"
          >
            <div>
              <span class="font-semibold text-slate-800">{{ cuenta.nombre }}</span>
              <span class="text-slate-500 ml-1">{{ cuenta.credencial }}</span>
            </div>
            <span class="px-2 py-0.5 rounded-full font-medium shrink-0" :class="cuenta.etiquetaClase">
              {{ cuenta.etiqueta }}
            </span>
          </button>
        </div>
      </div>

      <!-- Configuracion de servidor visible unicamente dentro de la app movil Capacitor -->
      <div v-if="appMovil" class="bg-white border border-slate-200 rounded-xl shadow-sm p-4 space-y-2.5">
        <div class="flex items-center justify-between gap-2">
          <h2 class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Servidor backend (movil)
          </h2>
          <button
            type="button"
            @click="mostrarConfigServidor = !mostrarConfigServidor"
            class="text-xs font-medium text-uv-azul hover:underline"
          >
            {{ mostrarConfigServidor ? 'Ocultar' : 'Cambiar IP / URL' }}
          </button>
        </div>

        <p v-if="!mostrarConfigServidor" class="text-xs text-slate-500 truncate">
          Conectando a: <span class="font-medium text-slate-700">{{ servidorActual }}</span>
        </p>

        <div v-else class="space-y-2 pt-1">
          <input
            v-model="servidorInput"
            type="url"
            placeholder="https://tu-backend.onrender.com"
            class="w-full rounded-lg border border-slate-300 px-3 py-1.5 text-xs focus:outline-none focus:ring-2 focus:ring-uv-azul/20 focus:border-uv-azul"
          />
          <div class="flex flex-wrap items-center gap-2">
            <button
              type="button"
              @click="aplicarServidorMovil"
              class="px-3 py-1.5 rounded-md bg-uv-azul hover:bg-uv-azul-oscuro text-white text-xs font-medium"
            >
              Guardar URL
            </button>
            <button
              v-if="modoDemo"
              type="button"
              @click="usarServidorUsb"
              class="px-2.5 py-1.5 rounded-md border border-slate-300 text-xs text-slate-700 hover:bg-slate-50"
            >
              USB (127.0.0.1)
            </button>
            <button
              v-if="modoDemo"
              type="button"
              @click="usarServidorEmulador"
              class="px-2.5 py-1.5 rounded-md border border-slate-300 text-xs text-slate-600 hover:bg-slate-50"
            >
              Emulador (10.0.2.2)
            </button>
          </div>
          <p v-if="errorServidor" class="text-[11px] text-red-700 font-medium">
            {{ errorServidor }}
          </p>
          <p v-else-if="avisoServidor" class="text-[11px] text-uv-verde-oscuro font-medium">
            {{ avisoServidor }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api, { extraerMensajeError } from '../services/api'
import { guardarSesion } from '../stores/sesion'
import { desconectarSocket } from '../services/socket'
import {
  esAppMovil,
  esModoDemo,
  guardarServidorMovil,
  obtenerServidorMovilEfectivo,
  obtenerServidorMovilGuardado
} from '../utils/entorno'

const router = useRouter()
const modo = ref('login')
const cargando = ref(false)
const error = ref('')
const appMovil = esAppMovil()
const modoDemo = esModoDemo()
const mostrarConfigServidor = ref(false)
const servidorInput = ref(obtenerServidorMovilGuardado() || obtenerServidorMovilEfectivo())
const servidorActual = ref(obtenerServidorMovilEfectivo())
const avisoServidor = ref('')
const errorServidor = ref('')

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

const cuentasDemo = [
  {
    credencial: 'S22014510',
    nombre: 'Laura Mendoza',
    etiqueta: 'Sin faltas',
    etiquetaClase: 'bg-uv-verde-claro text-uv-verde-oscuro'
  },
  {
    credencial: 'S22018824',
    nombre: 'Carlos Hernandez',
    etiqueta: '2 faltas',
    etiquetaClase: 'bg-slate-100 text-slate-700'
  },
  {
    credencial: 'S21009315',
    nombre: 'Ana Sofia Ruiz',
    etiqueta: 'Nivel 1: sin mostrador',
    etiquetaClase: 'bg-amber-50 text-amber-800'
  },
  {
    credencial: 'S23004109',
    nombre: 'Diego Morales',
    etiqueta: 'Nivel 2: solo catalogo',
    etiquetaClase: 'bg-red-50 text-red-700'
  },
  {
    credencial: 'P10001',
    nombre: 'Rosa Ramirez',
    etiqueta: 'Personal comedor',
    etiquetaClase: 'bg-uv-azul-claro text-uv-azul'
  }
]

function cambiarModo(nuevoModo) {
  modo.value = nuevoModo
  error.value = ''
}

function redirigirPorRol(rol) {
  if (rol === 'personal') {
    router.push('/personal')
  } else {
    router.push('/')
  }
}

async function enviarLogin() {
  error.value = ''
  cargando.value = true
  try {
    const { data } = await api.post('/auth/login', formLogin)
    guardarSesion(data.token, data.usuario)
    redirigirPorRol(data.usuario.rol)
  } catch (err) {
    error.value = extraerMensajeError(err, 'No se pudo iniciar sesion')
  } finally {
    cargando.value = false
  }
}

async function enviarRegistro() {
  error.value = ''
  cargando.value = true
  try {
    const { data } = await api.post('/auth/registro', formRegistro)
    guardarSesion(data.token, data.usuario)
    redirigirPorRol(data.usuario.rol)
  } catch (err) {
    error.value = extraerMensajeError(err, 'No se pudo completar el registro')
  } finally {
    cargando.value = false
  }
}

async function entrarConDemo(credencial) {
  modo.value = 'login'
  formLogin.credencialOCorreo = credencial
  formLogin.password = 'uv2026'
  await enviarLogin()
}

function aplicarServidorMovil() {
  errorServidor.value = ''
  const res = guardarServidorMovil(servidorInput.value)
  if (!res.ok) {
    errorServidor.value = res.error
    avisoServidor.value = ''
    return
  }
  servidorActual.value = obtenerServidorMovilEfectivo()
  servidorInput.value = res.url || servidorActual.value
  desconectarSocket()
  avisoServidor.value = `Servidor actualizado a ${servidorActual.value}`
}

function usarServidorUsb() {
  errorServidor.value = ''
  guardarServidorMovil('http://127.0.0.1:8080')
  servidorInput.value = obtenerServidorMovilEfectivo()
  servidorActual.value = servidorInput.value
  desconectarSocket()
  avisoServidor.value = `Conectado por USB a ${servidorActual.value}`
}

function usarServidorEmulador() {
  errorServidor.value = ''
  guardarServidorMovil('http://10.0.2.2:8080')
  servidorInput.value = obtenerServidorMovilEfectivo()
  servidorActual.value = servidorInput.value
  desconectarSocket()
  avisoServidor.value = `Configurado para emulador en ${servidorActual.value}`
}
</script>
