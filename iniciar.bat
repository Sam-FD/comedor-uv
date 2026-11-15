@echo off
chcp 65001 > nul
setlocal EnableDelayedExpansion
cd /d "%~dp0"

echo ==================================================
echo   Comedor Universitario UV - Inicio del sistema
echo ==================================================
echo.

echo [1/4] Verificando PostgreSQL en Docker...
docker info > nul 2>&1
if not errorlevel 1 goto docker_listo

echo Docker Desktop no esta corriendo. Intentando iniciarlo...
docker desktop start > nul 2>&1
if errorlevel 1 (
    if exist "%LOCALAPPDATA%\Programs\DockerDesktop\Docker Desktop.exe" (
        start "" "%LOCALAPPDATA%\Programs\DockerDesktop\Docker Desktop.exe"
    )
)

set /a intentos=0
:esperar_docker
docker info > nul 2>&1
if not errorlevel 1 goto docker_listo
set /a intentos+=1
if !intentos! GEQ 45 (
    echo ERROR: Docker Desktop tardo demasiado en iniciar. Abrelo manualmente y vuelve a intentar.
    pause
    exit /b 1
)
timeout /t 2 /nobreak > nul
goto esperar_docker

:docker_listo
docker compose up -d
if errorlevel 1 (
    echo ERROR: No se pudo iniciar el contenedor de PostgreSQL.
    pause
    exit /b 1
)

echo Esperando a que PostgreSQL acepte conexiones...
set /a pg_intentos=0
:esperar_pg
docker exec comedor_uv_postgres pg_isready -U comedor_user -d comedor_uv > nul 2>&1
if not errorlevel 1 goto pg_listo
set /a pg_intentos+=1
if !pg_intentos! GEQ 25 (
    echo ERROR: PostgreSQL no respondio a tiempo.
    pause
    exit /b 1
)
timeout /t 2 /nobreak > nul
goto esperar_pg

:pg_listo
echo PostgreSQL listo en localhost:5432.
echo.

echo [2/4] Preparando backend (Spring Boot + Socket.IO)...
set "JAR_PATH=backend\target\comedor-uv-backend-0.0.1-SNAPSHOT.jar"
if not exist "%JAR_PATH%" (
    echo No se encontro el paquete compilado. Compilando backend...
    call "%~dp0scripts\recompilar.bat" backend-only
    if errorlevel 1 (
        echo ERROR: Fallo la compilacion del backend.
        pause
        exit /b 1
    )
)

netstat -ano | findstr ":8080 " | findstr "LISTENING" > nul 2>&1
if not errorlevel 1 (
    echo El puerto 8080 ya esta en uso. Omitiendo arranque duplicado del backend.
) else (
    start "Comedor UV - Backend" /min cmd /c "title Comedor UV - Backend & java -jar backend\target\comedor-uv-backend-0.0.1-SNAPSHOT.jar"
)
echo.

echo [3/4] Preparando frontend (Vue + TailwindCSS)...
if not exist "frontend\node_modules" (
    echo Instalando dependencias de Node.js...
    call npm.cmd install --prefix frontend
    if errorlevel 1 (
        echo ERROR: No se pudieron instalar las dependencias del frontend.
        pause
        exit /b 1
    )
)

netstat -ano | findstr ":5173 " | findstr "LISTENING" > nul 2>&1
if not errorlevel 1 (
    echo El puerto 5173 ya esta en uso. Omitiendo arranque duplicado del frontend.
) else (
    start "Comedor UV - Frontend" /min cmd /c "title Comedor UV - Frontend & npm.cmd run dev --prefix frontend"
)
echo.

echo [4/4] Esperando a que el sistema termine de cargar...
powershell -NoProfile -Command "for ($i = 0; $i -lt 30; $i++) { try { $b = Invoke-WebRequest -Uri 'http://localhost:8080/api/platillos' -UseBasicParsing -TimeoutSec 2; $f = Invoke-WebRequest -Uri 'http://localhost:5173' -UseBasicParsing -TimeoutSec 2; if ($b.StatusCode -eq 200 -and $f.StatusCode -eq 200) { exit 0 } } catch {}; Start-Sleep -Seconds 2 }; exit 1"
if errorlevel 1 (
    echo Aviso: Los servicios siguen arrancando en segundo plano. Revisa las ventanas minimizadas si tarda un poco mas.
) else (
    echo Sistema en linea. Abriendo navegador...
    start "" http://localhost:5173
)

if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" reverse tcp:8080 tcp:8080 > nul 2>&1
    "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" reverse tcp:9092 tcp:9092 > nul 2>&1
)

echo.
echo ==================================================
echo   Servicios activos:
echo   - Aplicacion Web: http://localhost:5173
echo   - API Backend:    http://localhost:8080
echo   - Socket.IO:      http://localhost:9092
echo   - PostgreSQL:     localhost:5432 (comedor_uv)
echo   - App Movil USB:  http://127.0.0.1:8080 (via adb reverse)
echo.
echo   Cuando termines de usar el sistema, ejecuta detener.bat
echo ==================================================
echo.
pause
