@echo off
cd /d "%~dp0"
where javaw >nul 2>nul
if errorlevel 1 (
  echo Instale Java 8 o posterior para ejecutar el sistema.
  pause
  exit /b 1
)
start "Punto de venta" javaw -jar PuntoDeVenta.jar
