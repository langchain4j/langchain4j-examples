@echo off
setlocal
set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"
:: Without enforcer.skip, the quartus example cannot compile successfully
mvn install -DskipTests -Denforcer.skip=true -U
endlocal
