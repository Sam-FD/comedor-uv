# Comedor Universitario UV

Sistema de pedidos anticipados para el comedor universitario de la UV, con control de disponibilidad en tiempo real, flujo de preparación y validación en mostrador.

## Stack

- **Backend:** Java 21, Spring Boot 3.5, Spring Security (JWT), PostgreSQL 17, `netty-socketio`
- **Frontend:** Vue 3, Vue Router, Tailwind CSS, Axios, `socket.io-client`, Vite
- **Móvil:** Capacitor 8 (Android)

## Estructura

- `backend/` — API REST, reglas de negocio y notificaciones en tiempo real
- `frontend/` — Aplicación web responsiva y proyecto Android (`frontend/android/`)
- `db/` — Esquema inicial de PostgreSQL
- `docker/` — Configuración para empaquetado en contenedor
