@echo off
chcp 65001 >nul
cd /d "%~dp0.."
python scripts\ensure_utf8_workspace.py
python scripts\fix_shop_utf8_all.py
echo Done. Please reload Cursor window: Ctrl+Shift+P - Developer: Reload Window
pause
