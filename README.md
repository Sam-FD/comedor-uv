# Comedor Universitario UV

Sistema de pedidos anticipados para el comedor universitario de la UV, con control de disponibilidad en tiempo real, flujo de preparación y validación en mostrador.

## Stack

- **Backend:** Java 21, Spring Boot 3.5, Spring Security (JWT), PostgreSQL 17, `netty-socketio`
- **Frontend:** Vue 3, Vue Router, Tailwind CSS, Axios, `socket.io-client`, Vite
- **Móvil:** Capacitor 8 (Android)

## Uso local (Windows)

Requiere **Java 21**, **Node.js** y **Docker Desktop**.

- `iniciar.bat`: levanta PostgreSQL, backend (`8080`), Socket.IO (`9092`) y frontend (`http://localhost:5173`).
- `detener.bat`: detiene los servicios.
- Cuenta inicial de personal: `P10001` / `uv2026`. Los estudiantes se registran desde la app (para probar tarjeta rechazada en el pago simulado, usar una terminación `0002`).

Scripts en `scripts/`: `recompilar.bat`, `reiniciar-bd.bat` y `preparar-android.bat` (compila el APK en `docs/comedor-uv.apk`).

## Despliegue y APK

- **Backend (Render + Neon):** usa `render.yaml` / `Dockerfile` (Nginx unifica API y Socket.IO en un puerto). Variables requeridas: `DB_URL` (`jdbc:postgresql://...`), `DB_USER`, `DB_PASS` y `JWT_SECRET`.
- **Frontend (Vercel) y APK:** definir `VITE_API_URL=https://<servicio>.onrender.com/api` (en Vercel y en `frontend/.env.production` antes de compilar el APK).
