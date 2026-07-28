@echo off
setlocal
REM ============================================================
REM  run-frontend.bat  --  start plus-ui (Vite dev server, npm)
REM  Sits in scripts\ next to run-backend.bat.
REM  cd's to the plus-ui project (..\plus-ui) and runs npm run dev.
REM
REM  This project uses npm (package-lock.json), NOT yarn.
REM  Dev server: http://localhost:8188  (VITE_APP_PORT in .env.development).
REM  Changing .env requires restarting this script (Vite reads env at boot).
REM ============================================================

REM ---- pin Node (this machine) ----
set "NODE_HOME=D:\nodejs"
set "PATH=%NODE_HOME%;%PATH%"

REM ---- go to plus-ui root: scripts\ -> project root -> plus-ui ----
cd /d "%~dp0..\plus-ui"
if not exist "package.json" ( echo [ERROR] plus-ui not found at %cd% & goto :end )

echo [run-frontend] project = %cd%
node -v
echo.

REM ---- install deps only if missing (taobao mirror) ----
if not exist "node_modules" (
  echo [deps] node_modules missing -^> npm install
  call npm install --registry=https://registry.npmmirror.com
  if errorlevel 1 ( echo [FAILED] npm install failed, see log above. & goto :end )
)

echo [start] npm run dev   ^(Vite dev server, port 8188^)
echo ------------------------------------------------------------
call npm run dev

:end
echo.
echo (dev server exited) press any key to close ...
pause >nul
