# Comedor Universitario UV

Sistema de pedidos anticipados para el comedor de la Universidad Veracruzana (UV), con separacion entre orden en linea, retencion/cobro en pasarela y entrega en mostrador con pase temporal.

## Tecnologias

- **Backend:** Java 21, Spring Boot 3.5.16 (Web, Data JPA, Security JWT, Validation), PostgreSQL 17, `netty-socketio` 2.0.14
- **Frontend:** Vue 3.5, Vue Router 5, Tailwind CSS 3.4, Axios, `socket.io-client` 4.8, Vite 8
- **Movil:** Capacitor 8 (Android) sobre la misma interfaz Vue

## Arranque rapido en local (Windows)

Requisitos: **Java 21**, **Node.js + npm** y **Docker Desktop**.

1. Ejecuta `iniciar.bat` en la raiz del proyecto (levanta PostgreSQL, backend en `8080`, Socket.IO en `9092` y frontend en `http://localhost:5173`).
2. Para detener todo, ejecuta `detener.bat`.

Scripts auxiliares en `scripts/`:

- `scripts\reiniciar-bd.bat`: restaura PostgreSQL al estado inicial con datos de prueba.
- `scripts\recompilar.bat`: recompila el `.jar` del backend y el build web.
- `scripts\preparar-android.bat`: compila el frontend y sincroniza Capacitor con `frontend/android`.

## Cuentas de prueba (`uv2026`)

| Matricula | Rol / Escenario |
| :--- | :--- |
| `S22014510` | Estudiante sin faltas (tarjeta en app o mostrador) |
| `S22018824` | Estudiante con 2 faltas |
| `S21009315` | Nivel 1 (`sin_mostrador`): solo tarjeta en app |
| `S23004109` | Nivel 2 (`sin_pedidos_en_linea`): cuenta suspendida |
| `P10001` | Personal del comedor (cola en vivo, menu e historico) |

## Configuracion para pruebas y despliegue

- **Salud del servicio:** `GET /api/salud`
- **Backend (`backend/src/main/resources/application.properties` / variables de entorno):**
  - `PORT`, `DB_URL`, `DB_USER`, `DB_PASS`, `JPA_DDL_AUTO` (por defecto `update`)
  - `JWT_SECRET`, `CORS_ORIGINS`, `SOCKET_ENABLED`, `SOCKET_HOST`, `SOCKET_PORT`
  - `STRIPE_SECRET_KEY`: opcional (`sk_test_...` para Stripe Sandbox real; vacio usa sandbox local con tarjeta aprobada `4242...4242` y rechazo `4000...0002`)
- **Frontend (`frontend/.env.example`):**
  - `VITE_API_URL`, `VITE_SOCKET_URL`, `VITE_SOCKET_ENABLED`

## App Android (Capacitor)

- Sincronizar cambios web hacia Android: `npm run cap:sync` (en `frontend/`)
- Abrir proyecto nativo en Android Studio: `npm run cap:open` (en `frontend/`)
- En movil apunta por defecto a `http://127.0.0.1:8080` (compatible con `adb reverse`) y permite configurar la IP local o URL HTTPS desde la pantalla de inicio de sesion.
