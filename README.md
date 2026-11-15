# Comedor Universitario UV

Sistema de pedidos anticipados para el comedor de la Universidad Veracruzana (UV), con separacion entre orden en linea, retencion/cobro en pasarela y entrega en mostrador.

## Tecnologias

- **Backend:** Java 21, Spring Boot 3.5.16 (Web, Data JPA, Security JWT, Validation), PostgreSQL 17, `netty-socketio` 2.0.14
- **Frontend:** Vue 3.5, Vue Router 5, Tailwind CSS 3.4, Axios, `socket.io-client` 4.8, Vite 8
- **Movil:** Capacitor 8 (Android) sobre la misma interfaz Vue

## Arranque rapido en local (Windows)

Requisitos: **Java 21**, **Node.js + npm** y **Docker Desktop**.

1. Ejecuta `iniciar.bat` en la raiz del proyecto (levanta PostgreSQL, backend en `8080`, Socket.IO en `9092` y frontend en `http://localhost:5173`).
2. Para detener todo, ejecuta `detener.bat`.

En produccion (`DATOS_DEMO=false` por defecto) solo se crea la cuenta de personal (`P10001` / `uv2026` o la configurada por variables de entorno) y el menu regular base; los estudiantes se registran desde la app y el personal publica la comida del dia. Si deseas cargar usuarios y pedidos de ejemplo localmente, define `DATOS_DEMO=true` y `VITE_MODO_DEMO=true`.

Scripts auxiliares en `scripts/`:

- `scripts\reiniciar-bd.bat`: restaura PostgreSQL a estado limpio.
- `scripts\recompilar.bat`: recompila el `.jar` del backend y el build web.
- `scripts\preparar-android.bat`: compila el frontend y sincroniza Capacitor con `frontend/android`.

## Despliegue en la nube (Neon + Render + Vercel)

1. **Base de datos en Neon.tech:** crea un proyecto PostgreSQL gratuito y copia la cadena de conexion (`postgresql://usuario:clave@host/neondb?sslmode=require`). El backend extrae credenciales y convierte la URL a JDBC automaticamente.
2. **Backend en Render.com:** conecta este repositorio como Blueprint (`render.yaml`) o Web Service Docker (`Dockerfile` unificado de API + Socket.IO en puerto unico, health check `/api/salud`). Configura:
   - `DB_URL`: cadena copiada de Neon.tech
   - `JWT_SECRET`: generado automaticamente o personalizado
   - `ADMIN_PASSWORD` (opcional): clave para la cuenta de personal `P10001` (por defecto `uv2026`)
   - `STRIPE_SECRET_KEY` (opcional): clave `sk_test_...` de Stripe Sandbox (vacio usa pasarela local)
   - `CORS_ORIGINS`: URL de tu sitio en Vercel (por defecto ya acepta `https://*.vercel.app`)
3. **Frontend web en Vercel:** importa el mismo repositorio seleccionando `frontend` como *Root Directory* (framework Vite, comando `npm run build`, salida `dist`) y define:
   - `VITE_API_URL=https://<tu-servicio>.onrender.com/api`
   - `VITE_MODO_DEMO=false`
   - (`VITE_SOCKET_URL` puede quedar vacia; el cliente conecta WebSockets automaticamente al mismo dominio Render).

## Variables principales (`backend/src/main/resources/application.properties` y `frontend/.env.example`)

- **Backend:** `PORT`, `DB_URL` / `DATABASE_URL`, `DB_USER`, `DB_PASS`, `DB_POOL_SIZE`, `JPA_DDL_AUTO`, `JWT_SECRET`, `CORS_ORIGINS`, `DATOS_DEMO`, `ADMIN_CREDENCIAL`, `ADMIN_PASSWORD`, `STRIPE_SECRET_KEY`.
- **Frontend:** `VITE_API_URL`, `VITE_SOCKET_URL`, `VITE_SOCKET_ENABLED`, `VITE_MODO_DEMO`.

## App Android (Capacitor)

- Para compilar un APK conectado al servidor en la nube, define `VITE_API_URL=https://<tu-servicio>.onrender.com/api` antes de correr `npm run cap:sync` dentro de `frontend/`.
- Abrir proyecto en Android Studio: `npm run cap:open` (en `frontend/`).
- En la app movil se mantiene el ajuste manual de URL desde `/login` para apuntar a otro servidor o entorno local sin recompilar el APK.
