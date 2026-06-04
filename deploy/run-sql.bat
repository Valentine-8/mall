@echo off
REM 本机 Windows 一键：SSH 到服务器执行 SQL（无需公网开放 3306）
REM 用法:
REM   deploy\run-sql.bat sql\mall_social.sql
REM   deploy\run-sql.bat --extra
REM   deploy\run-sql.bat --list
REM 首次使用前修改下面 PEM、IP、服务器项目路径

setlocal EnableExtensions

set "PEM=C:\Users\lemon\Downloads\ruoyimall.pem"
set "IP=82.156.68.87"
set "USER=ubuntu"
set "REMOTE_DIR=/home/ubuntu/mall"

if "%~1"=="" (
  echo.
  echo 用法:
  echo   deploy\run-sql.bat sql\mall_social.sql
  echo   deploy\run-sql.bat --extra
  echo   deploy\run-sql.bat --list
  echo.
  echo 请编辑本文件顶部的 PEM、IP、REMOTE_DIR
  exit /b 1
)

if not exist "%PEM%" (
  echo 错误: 找不到私钥 %PEM%
  exit /b 1
)

set "ARG1=%~1"
set "REMOTE_ARG=%ARG1:\=/%"

echo SSH 到 %USER%@%IP% 执行 SQL ...
ssh -i "%PEM%" -o StrictHostKeyChecking=accept-new %USER%@%IP% "cd %REMOTE_DIR% && chmod +x deploy/run-sql.sh && ./deploy/run-sql.sh %REMOTE_ARG%"
set "EC=%ERRORLEVEL%"
if not "%EC%"=="0" (
  echo 执行失败，退出码 %EC%
  exit /b %EC%
)
echo 完成。
exit /b 0
