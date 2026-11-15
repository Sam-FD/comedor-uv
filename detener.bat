@echo off
chcp 65001 > nul
setlocal EnableDelayedExpansion
cd /d "%~dp0"

echo ==================================================
echo   Comedor Universitario UV - Deteniendo servicios
echo ==================================================
echo.

echo [1/3] Cerrando frontend y backend...
taskkill /FI "WINDOWTITLE eq Comedor UV - Frontend*" /T /F > nul 2>&1
taskkill /FI "WINDOWTITLE eq Comedor UV - Backend*" /T /F > nul 2>&1

for %%P in (5173 8080 9092) do (
    for /f "tokens=5" %%A in ('netstat -ano 2^>nul ^| findstr ":%%P " ^| findstr "LISTENING"') do (
        if not "%%A"=="0" (
            taskkill /PID %%A /T /F > nul 2>&1
        )
    )
)

echo [2/3] Deteniendo contenedor de PostgreSQL...
docker info > nul 2>&1
if not errorlevel 1 (
    docker compose stop
)

echo [3/3] Limpieza completada.
echo.
echo Todos los servicios del Comedor Universitario UV han sido detenidos.
echo Los datos en PostgreSQL se conservan para la siguiente sesion.
echo.
pause
