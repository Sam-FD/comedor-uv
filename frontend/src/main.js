import { createApp } from 'vue'
import { App as CapacitorApp } from '@capacitor/app'
import App from './App.vue'
import router from './router'
import './style.css'
import { sesion } from './stores/sesion'
import { conectarSocket } from './services/socket'
import { esAppMovil } from './utils/entorno'

if (sesion.token) {
  conectarSocket()
}

if (esAppMovil()) {
  CapacitorApp.addListener('backButton', () => {
    const evento = new CustomEvent('app-back', { cancelable: true })
    if (!window.dispatchEvent(evento)) return
    const rutasRaiz = ['/', '/login', '/personal']
    if (!rutasRaiz.includes(router.currentRoute.value.path)) {
      router.back()
    } else {
      CapacitorApp.minimizeApp()
    }
  })
}

createApp(App).use(router).mount('#app')
