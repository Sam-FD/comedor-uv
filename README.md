# Comedor Universitario UV

Sistema de pedidos anticipados para el comedor de la Universidad Veracruzana (UV), con separación entre orden en línea, retención/cobro en pasarela y entrega en mostrador.

## Tecnologías

- **Backend:** Java 21, Spring Boot 3.5.16 (Web, Data JPA, Security JWT, Validation), PostgreSQL 17, `netty-socketio` 2.0.14
- **Frontend:** Vue 3.5, Vue Router 5, Tailwind CSS 3.4, Axios, `socket.io-client` 4.8, Vite 8
- **Móvil:** Capacitor 8 (Android) sobre la misma interfaz Vue

## Arranque local (Windows)

Requisitos: **Java 21**, **Node.js + npm** y **Docker Desktop**.

1. Ejecutar `iniciar.bat` en la raíz del proyecto para levantar PostgreSQL, backend en `8080`, Socket.IO en `9092` y frontend en `http://localhost:5173`.
2. Ejecutar `detener.bat` para detener todos los servicios.

Por defecto (`DATOS_DEMO=false`) únicamente se inicializa la cuenta de personal (`P10001` / `uv2026`, configurable por variables de entorno) y el menú base; el registro de estudiantes y la publicación de la comida del día se realizan desde la aplicación. Para cargar usuarios y pedidos de ejemplo en entorno local, definir `DATOS_DEMO=true` y `VITE_MODO_DEMO=true`.

Scripts auxiliares en `scripts/`:

- `scripts\reiniciar-bd.bat`: limpia el volumen de PostgreSQL local para empezar con base nueva.
- `scripts\recompilar.bat`: recompila el `.jar` del backend y el build web.
- `scripts\preparar-android.bat`: compila el frontend y sincroniza Capacitor con `frontend/android`.

## Despliegue en la nube (Neon + Render + Vercel)

1. **Base de datos en Neon.tech:** crear un proyecto PostgreSQL y obtener la cadena de conexión (`postgresql://usuario:clave@host/neondb?sslmode=require`). El backend extrae credenciales y convierte la URL a JDBC automáticamente al iniciar.
2. **Backend en Render.com:** conectar el repositorio mediante Blueprint (`render.yaml`) o Web Service Docker (`Dockerfile` unificado de API + Socket.IO en un solo puerto, health check `/api/salud`). Variables principales:
   - `DB_URL`: cadena de conexión de Neon.tech
   - `JWT_SECRET`: autogenerada por `render.yaml` o definida manualmente
   - `ADMIN_PASSWORD` (opcional): clave para la cuenta de personal `P10001` (por defecto `uv2026`)
   - `STRIPE_SECRET_KEY` (opcional): clave `sk_test_...` de Stripe Sandbox (vacío usa pasarela local)
   - `CORS_ORIGINS`: orígenes permitidos (por defecto acepta `https://*.vercel.app` y `https://*.onrender.com`)
3. **Frontend web en Vercel:** importar el repositorio definiendo `frontend` como *Root Directory* (framework Vite, comando `npm run build`, salida `dist`) y configurar:
   - `VITE_API_URL=https://<servicio>.onrender.com/api`
   - `VITE_MODO_DEMO=false`
   - (`VITE_SOCKET_URL` puede quedar vacía para conectar WebSockets al mismo dominio de `VITE_API_URL`).

## Variables principales (`backend/src/main/resources/application.properties` y `frontend/.env.example`)

- **Backend:** `PORT`, `DB_URL` / `DATABASE_URL`, `DB_USER`, `DB_PASS`, `DB_POOL_SIZE`, `JPA_DDL_AUTO`, `JWT_SECRET`, `CORS_ORIGINS`, `DATOS_DEMO`, `ADMIN_CREDENCIAL`, `ADMIN_PASSWORD`, `STRIPE_SECRET_KEY`.
- **Frontend:** `VITE_API_URL`, `VITE_SOCKET_URL`, `VITE_SOCKET_ENABLED`, `VITE_MODO_DEMO`.

## App Android (Capacitor)

- Definir `VITE_API_URL=https://<servicio>.onrender.com/api` en `frontend/.env.production` antes de ejecutar `npm run cap:sync` dentro de `frontend/` para compilar un APK conectado a la nube.
- Abrir el proyecto en Android Studio: `npm run cap:open` (en `frontend/`).
- La vista `/login` de la app móvil conserva el ajuste manual de URL para apuntar a otro servidor sin recompilar el APK.
