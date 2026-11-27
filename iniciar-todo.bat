@echo off
REM Compila y levanta los 4 servicios en ventanas separadas (Windows).
cd /d %~dp0
call mvnw.cmd -q -DskipTests install || exit /b 1
for %%m in (ms-usuarios ms-productos ms-pedidos gateway) do (
  start "%%m" cmd /k "cd %%m && java -jar target\%%m-1.0.0.jar"
  timeout /t 2 >nul
)
echo Gateway en http://localhost:8080
