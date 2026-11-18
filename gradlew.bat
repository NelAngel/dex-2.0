@echo off
set VERSION=8.11.1
where gradle >nul 2>nul
if %ERRORLEVEL%==0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
echo Gradle %VERSION% no esta instalado. Ejecuta gradlew desde Git Bash o instala Gradle y vuelve a intentarlo.
exit /b 1
