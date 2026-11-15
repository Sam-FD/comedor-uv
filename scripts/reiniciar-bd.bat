@echo off
chcp 65001 > nul
setlocal
cd /d "%~dp0\.."

echo Esto borrara todos los pedidos y cambios en la base de datos local y restaurara los datos de prueba iniciales.
set /p RESP="Escribe SI para continuar: "
if /i not "%RESP%"=="SI" (
    echo Operacion cancelada.
    pause
    exit /b 0
)

call detener.bat
docker compose down -v
docker compose up -d

echo Base de datos reiniciada. Ahora puedes ejecutar iniciar.bat.
pause
