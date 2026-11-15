@echo off
chcp 65001 > nul
setlocal
cd /d "%~dp0\..\frontend"

echo ==================================================
echo   Comedor UV - Sincronizando app Android
echo ==================================================
echo.

echo [1/2] Compilando aplicacion web con Vite...
call npm.cmd run build
if errorlevel 1 (
    echo ERROR al compilar el frontend.
    pause
    exit /b 1
)

echo.
echo [2/2] Copiando recursos a frontend\android con Capacitor...
call npx.cmd cap sync android
if errorlevel 1 (
    echo ERROR al sincronizar Capacitor Android.
    pause
    exit /b 1
)

echo.
if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" reverse tcp:8080 tcp:8080 > nul 2>&1
    "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" reverse tcp:9092 tcp:9092 > nul 2>&1
)
echo Listo. Abrir frontend\android en Android Studio para compilar o ejecutar.
echo Con el telefono por USB usar http://127.0.0.1:8080 y en emulador http://10.0.2.2:8080.
echo.
pause
