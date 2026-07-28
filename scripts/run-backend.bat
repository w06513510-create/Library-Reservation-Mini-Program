@echo off
title 图书馆预约系统 后端启动器 (RuoYi-Vue-Plus / fat jar)

REM ============================================================
REM  图书馆预约系统 后端一键启动 (本脚本在 scripts\ 下, 启动时自动切到 ..\RuoYi-Vue-Plus)
REM
REM  原理: 用 Maven reactor 打自包含 fat jar (含全部依赖) 再 java -jar,
REM        启动阶段零 Maven、零下载。兄弟模块之间是 jar 依赖, 命令行必须先用
REM        reactor(-pl ruoyi-admin -am) 把兄弟模块一起编译, fat jar 里才带齐所有类。
REM
REM  用法 (双击, 或在 scripts 目录执行):
REM    run-backend.bat            有 jar 直接启动; 没有则先构建再启动
REM    run-backend.bat build      强制重新构建 fat jar 后再启动 (改了后端代码后用这个!)
REM    run-backend.bat run prod   第二参数可选, 覆盖 spring profile (prod/local, 默认 dev)
REM
REM  [!] 前置: 本机 MySQL(库 library_reservation) + Docker 的 library-redis / library-minio 已就绪
REM  [!] 上传报"系统接口请求超时" 多为 MinIO 没起或 sys_oss_config 未对齐; 改了后端代码用 build 重构建
REM  [!] 端口 8199 (application.yml server.port)
REM ============================================================

REM ---- 固定环境 (本机真实路径) ----
set "JAVA_HOME=D:\jdk\jdk17"
set "MAVEN_HOME=D:\Maven\maven\apache-maven-3.9.12"
set "MAVEN_REPO=D:\Maven\.m2\repository"
set "MVN=%MAVEN_HOME%\bin\mvn.cmd"
set "JAVA=%JAVA_HOME%\bin\java.exe"
set "JAR=ruoyi-admin\target\ruoyi-admin.jar"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

REM ---- 切到 RuoYi-Vue-Plus 项目根 (本脚本在 scripts\ 下, 上一级是项目根) ----
cd /d "%~dp0..\RuoYi-Vue-Plus"

REM ---- 基本校验 ----
if not exist "%JAVA%" ( echo [错误] 未找到 JDK: "%JAVA%" & goto :end )
if not exist "%MVN%"  ( echo [错误] 未找到 Maven: "%MVN%" & goto :end )
if not exist "ruoyi-admin\pom.xml" ( echo [错误] 未定位到 RuoYi-Vue-Plus 项目根 [当前: %cd%], 请确认本脚本在 scripts\ 下 & goto :end )

set "MODE=%~1"
if "%MODE%"=="" set "MODE=run"
set "PROFILE=%~2"

set "PROFARG="
if not "%PROFILE%"=="" set "PROFARG=--spring.profiles.active=%PROFILE%"

echo ============================================================
echo  项目根    = %cd%
echo  JAVA_HOME = %JAVA_HOME%
echo  MAVEN     = %MAVEN_HOME%
echo  REPO      = %MAVEN_REPO%
echo  模式 MODE = %MODE%    端口(默认) = 8199
echo  提示: 启动前请确保 MySQL / Redis / MinIO 已就绪
echo ============================================================
echo.

if /i "%MODE%"=="build"   goto :build
if /i "%MODE%"=="rebuild" goto :build

REM ---- 默认: 有 jar 直接跑, 没有则先构建 ----
if exist "%JAR%" goto :runjar
echo 未发现 fat jar, 先构建一次 ...
goto :build

:build
echo [构建] mvn -pl ruoyi-admin -am -Dmaven.test.skip=true package  (首次几分钟, 之后有缓存较快) ...
call "%MVN%" -Dmaven.repo.local="%MAVEN_REPO%" -pl ruoyi-admin -am -Dmaven.test.skip=true package
if errorlevel 1 goto :builderr
if not exist "%JAR%" ( echo [错误] 构建完成但未找到 %JAR% & goto :end )
echo [构建完成]
echo.

:runjar
echo [启动] java -jar %JAR%
echo ------------------------------------------------------------
chcp 65001 >nul
"%JAVA%" -Dfile.encoding=UTF-8 -jar "%JAR%" %PROFARG%
goto :end

:builderr
echo.
echo [失败] Maven 构建失败, 请查看上方日志排查.
goto :end

:end
echo.
echo (进程已退出) 按任意键关闭窗口 ...
pause >nul
