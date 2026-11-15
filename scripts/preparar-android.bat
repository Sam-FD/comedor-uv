@echo off
chcp 65001 > nul
setlocal
cd /d "%~dp0\..\frontend"

echo ==================================================
echo   Comedor UV - Compilando y empaquetando APK
echo ==================================================
echo.

echo [1/3] Compilando aplicacion web con Vite...
call npm.cmd run build
if errorlevel 1 (
    echo ERROR al compilar el frontend.
    if /I not "%~1"=="nopause" pause
    exit /b 1
)

echo.
echo [2/3] Sincronizando recursos con Capacitor Android...
call npx.cmd cap sync android
if errorlevel 1 (
    echo ERROR al sincronizar Capacitor Android.
    if /I not "%~1"=="nopause" pause
    exit /b 1
)

echo.
echo [3/3] Compilando APK con Gradle y copiando a docs\comedor-uv.apk...
pushd android
call gradlew.bat assembleDebug
set "GRADLE_ERR=%ERRORLEVEL%"
popd
if not "%GRADLE_ERR%"=="0" (
    echo ERROR al compilar el APK con Gradle.
    if /I not "%~1"=="nopause" pause
    exit /b %GRADLE_ERR%
)

if not exist "..\docs" mkdir "..\docs"
copy /Y "android\app\build\outputs\apk\debug\app-debug.apk" "..\docs\comedor-uv.apk" > nul
if errorlevel 1 (
    echo ERROR al copiar el APK a docs\comedor-uv.apk.
    if /I not "%~1"=="nopause" pause
    exit /b 1
)

echo.
echo Listo. APK generado en docs\comedor-uv.apk conectado al servidor en la nube.
echo Tambien queda disponible en frontend\android\app\build\outputs\apk\debug\app-debug.apk.
echo.
if /I not "%~1"=="nopause" pause
