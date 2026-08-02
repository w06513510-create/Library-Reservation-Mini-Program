@echo off
REM == 图书馆预约系统 · 毕设文档站 —— 一键启动（起本地服务 + 自动打开浏览器预览）==
REM 双击本文件即可启动；浏览器自动打开 http://127.0.0.1:18099/
setlocal
set "PYEXE=C:\Users\lwf\.conda\envs\admindocs\python.exe"
set "SITEDIR=%~dp0"
set "URL=http://127.0.0.1:18099/"

if not exist "%PYEXE%" (
  echo [错误] 找不到 Python: %PYEXE%
  echo         请确认对应 conda 环境是否存在。
  pause
  exit /b 1
)

cd /d "%SITEDIR%"
echo 正在启动文档站，网址 %URL%
echo 关闭本窗口即终止服务。

REM 延迟 3 秒后自动打开浏览器（给 mkdocs 起服务时间）
start "" cmd /c "timeout /t 3 >nul & start %URL%"

set "PYTHONUTF8=1"
set "PYTHONIOENCODING=utf-8"
"%PYEXE%" -m mkdocs serve

pause
