@echo off
chcp 65001 > nul
setlocal
cd /d "%~dp0\.."

echo Esta accion borrara todos los pedidos y cambios de la base de datos local para iniciar con una base limpia.
set /p RESP="Confirmar escribiendo SI: "
if /i not "%RESP%"=="SI" (
    echo Operacion cancelada.
    pause
    exit /b 0
)

call detener.bat
docker compose down -v
docker compose up -d

echo Base de datos reiniciada. Ejecutar iniciar.bat para levantar el sistema.
pause
