@echo off
title ͼ���ԤԼϵͳ ��������� (RuoYi-Vue-Plus / fat jar)

REM ============================================================
REM  ͼ���ԤԼϵͳ ���һ������ (���ű��� scripts\ ��, ����ʱ�Զ��е� ..\RuoYi-Vue-Plus)
REM
REM  ԭ��: �� Maven reactor ���԰��� fat jar (��ȫ������) �� java -jar,
REM        �����׶��� Maven�������ء��ֵ�ģ��֮���� jar ����, �����б�������
REM        reactor(-pl ruoyi-admin -am) ���ֵ�ģ��һ�����, fat jar ��Ŵ��������ࡣ
REM
REM  �÷� (˫��, ���� scripts Ŀ¼ִ��):
REM    run-backend.bat            �� jar ֱ������; û�����ȹ���������
REM    run-backend.bat build      ǿ�����¹��� fat jar �������� (���˺�˴���������!)
REM    run-backend.bat run prod   �ڶ�������ѡ, ���� spring profile (prod/local, Ĭ�� dev)
REM
REM  [!] ǰ��: ���� MySQL(�� library_reservation) + Docker �� library-redis / library-minio �Ѿ���
REM  [!] �ϴ���"ϵͳ�ӿ�����ʱ" ��Ϊ MinIO û��� sys_oss_config δ����; ���˺�˴����� build �ع���
REM  [!] �˿� 8199 (application.yml server.port)
REM ============================================================

REM ---- �̶����� (������ʵ·��) ----
set "JAVA_HOME=D:\jdk\jdk17"
set "MAVEN_HOME=D:\Maven\maven\apache-maven-3.9.12"
set "MAVEN_REPO=D:\Maven\.m2\repository"
set "MVN=%MAVEN_HOME%\bin\mvn.cmd"
set "JAVA=%JAVA_HOME%\bin\java.exe"
set "JAR=ruoyi-admin\target\ruoyi-admin.jar"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

REM ---- �е� RuoYi-Vue-Plus ��Ŀ�� (���ű��� scripts\ ��, ��һ������Ŀ��) ----
cd /d "%~dp0..\RuoYi-Vue-Plus"

REM ---- ����У�� ----
if not exist "%JAVA%" ( echo [����] δ�ҵ� JDK: "%JAVA%" & goto :end )
if not exist "%MVN%"  ( echo [����] δ�ҵ� Maven: "%MVN%" & goto :end )
if not exist "ruoyi-admin\pom.xml" ( echo [����] δ��λ�� RuoYi-Vue-Plus ��Ŀ�� [��ǰ: %cd%], ��ȷ�ϱ��ű��� scripts\ �� & goto :end )

set "MODE=%~1"
if "%MODE%"=="" set "MODE=run"
set "PROFILE=%~2"

set "PROFARG="
if not "%PROFILE%"=="" set "PROFARG=--spring.profiles.active=%PROFILE%"

echo ============================================================
echo  ��Ŀ��    = %cd%
echo  JAVA_HOME = %JAVA_HOME%
echo  MAVEN     = %MAVEN_HOME%
echo  REPO      = %MAVEN_REPO%
echo  ģʽ MODE = %MODE%    �˿�(Ĭ��) = 8199
echo  ��ʾ: ����ǰ��ȷ�� MySQL / Redis / MinIO �Ѿ���
echo ============================================================
echo.

if /i "%MODE%"=="build"   goto :build
if /i "%MODE%"=="rebuild" goto :build

REM ---- Ĭ��: �� jar ֱ����, û�����ȹ��� ----
if exist "%JAR%" goto :runjar
echo δ���� fat jar, �ȹ���һ�� ...
goto :build

:build
echo [����] mvn -pl ruoyi-admin -am -Dmaven.test.skip=true package  (�״μ�����, ֮���л���Ͽ�) ...
call "%MVN%" -Dmaven.repo.local="%MAVEN_REPO%" -pl ruoyi-admin -am -Dmaven.test.skip=true package
if errorlevel 1 goto :builderr
if not exist "%JAR%" ( echo [����] ������ɵ�δ�ҵ� %JAR% & goto :end )
echo [�������]
echo.

:runjar
echo [����] java -jar %JAR%
echo ------------------------------------------------------------
chcp 65001 >nul
"%JAVA%" -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -jar "%JAR%" %PROFARG%
goto :end

:builderr
echo.
echo [ʧ��] Maven ����ʧ��, ��鿴�Ϸ���־�Ų�.
goto :end

:end
echo.
echo (�������˳�) ��������رմ��� ...
pause >nul
