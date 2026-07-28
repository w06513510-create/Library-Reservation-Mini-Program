@echo off
chcp 65001 >nul
title RuoYi-Vue-Plus 启动器 (fat jar / java -jar)

REM ============================================================
REM  RuoYi-Vue-Plus 一键启动脚本  (已实测可用)
REM  位置: 本脚本放在 Template\scripts\ 下, 运行时自动切到 RuoYi-Vue-Plus 项目根。
REM
REM  原理: 用 Maven reactor 打出自包含 fat jar (含全部依赖),
REM        再用 java -jar 启动 —— 启动阶段零 Maven、零下载,
REM        约 17 秒起来 (实测), 和 IDEA 直接运行主类效果一致。
REM
REM  为什么要先 package: 本项目各模块之间是 jar 依赖, 而兄弟模块
REM  的 jar 没装进本地仓库(IDEA 用自己的模块机制绕过了)。所以命令行
REM  必须先用 reactor(-pl ruoyi-admin -am) 把兄弟模块一起编译打包,
REM  fat jar 里就带齐了所有类, 之后 java -jar 直接跑。
REM
REM  用法 (双击, 或在 script 目录里执行):
REM    run.bat              有 jar 就直接启动; 没有就先构建再启动 (推荐)
REM    run.bat build        强制重新构建 fat jar 后再启动 (改了代码后用这个)
REM    run.bat run  prod    第二参数可选, 覆盖 spring profile (prod/local, 默认 dev)
REM ============================================================

REM ---- 固定环境 (本机实际路径) ----
set "JAVA_HOME=D:\jdk\jdk17"
set "MAVEN_HOME=D:\Maven\maven\apache-maven-3.9.12"
REM 注意: 真正的本地仓库是 .m2\repository 子目录 (IDEA 用的就是它, 1.2G 已装齐)
set "MAVEN_REPO=D:\Maven\.m2\repository"
set "MVN=%MAVEN_HOME%\bin\mvn.cmd"
set "JAVA=%JAVA_HOME%\bin\java.exe"
set "JAR=ruoyi-admin\target\ruoyi-admin.jar"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

REM ---- 切到 RuoYi-Vue-Plus 项目根 (本脚本在 Template\scripts\ 下) ----
cd /d "%~dp0..\RuoYi-Vue-Plus"

REM ---- 基本校验 ----
if not exist "%JAVA%" ( echo [错误] 未找到 JDK: "%JAVA%" & goto :end )
if not exist "%MVN%"  ( echo [错误] 未找到 Maven: "%MVN%" & goto :end )
if not exist "ruoyi-admin\pom.xml" ( echo [错误] 未定位到 RuoYi-Vue-Plus 项目根 (当前: %cd%), 请确认本脚本在 Template\scripts\ 下 & goto :end )

set "MODE=%~1"
if "%MODE%"=="" set "MODE=run"
set "PROFILE=%~2"

set "PROFARG="
if not "%PROFILE%"=="" set "PROFARG=--spring.profiles.active=%PROFILE%"

echo ============================================================
echo  项目根   = %cd%
echo  JAVA_HOME = %JAVA_HOME%
echo  MAVEN     = %MAVEN_HOME%
echo  REPO      = %MAVEN_REPO%
echo  模式 MODE = %MODE%    端口(默认) = 8199
echo  提示: 启动前请确保 MySQL / Redis 已就绪
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
