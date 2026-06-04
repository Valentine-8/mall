@echo off
chcp 65001 >nul
cd /d "%~dp0.."
python scripts\ensure_utf8_workspace.py --check
exit /b %ERRORLEVEL%
