@echo off
chcp 65001 > nul
setlocal EnableDelayedExpansion
cd /d "%~dp0\.."

set "MVN_CMD=mvn"
where mvn > nul 2>&1
if errorlevel 1 (
    set "MVN_FOUND="
    for /d %%D in ("C:\Program Files\JetBrains\IntelliJ IDEA*") do (
        if exist "%%~D\plugins\maven\lib\maven3\bin\mvn.cmd" (
            set "MVN_FOUND=%%~D\plugins\maven\lib\maven3\bin\mvn.cmd"
        )
    )
    if defined MVN_FOUND (
        set "MVN_CMD=!MVN_FOUND!"
    ) else (
        echo ERROR: No se encontro Maven en el sistema.
        exit /b 1
    )
)

echo Recompilando backend...
call "%MVN_CMD%" -f backend\pom.xml package -DskipTests
if errorlevel 1 exit /b 1

if "%~1"=="backend-only" exit /b 0

echo Recompilando frontend...
call npm.cmd run build --prefix frontend
if errorlevel 1 exit /b 1

echo Compilacion completa exitosa.
pause
