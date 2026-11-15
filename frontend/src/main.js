import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'
import { sesion } from './stores/sesion'
import { conectarSocket } from './services/socket'

if (sesion.token) {
  conectarSocket()
}

createApp(App).use(router).mount('#app')
