@echo off

set DIR=./
if "%OS%" == "Windows_NT" set DIR=%~dp0%
set CLASS=net.weta.components.test.TcpClient
echo call cmd /V:ON /C %DIR%starter.bat

set HOST=217.160.134.204
set PORT=443
set PROXY_HOST=127.0.0.1
set PROXY_PORT=8080
set MAX_MESSAGES=10
set USER_NAME=
set PASSWORD=


cmd /V:ON /C "%DIR%starter.bat" %CLASS% --host %HOST% --port %PORT% --maxMessages %MAX_MESSAGES%

