@echo off
chcp 65001 >nul
title UAMS 启动器

set JAVA_HOME=E:\JDK17.0.13
set NODE_HOME=E:\node-v20.12.2-win-x64
set PATH=%NODE_HOME%;%JAVA_HOME%\bin;%PATH%
set MYSQL_HOME=E:\MySQL\MySQL Server 8.0
set MYSQL_DATA=E:\Code\sandouAI\sandouAI\mysql-data
set MYSQL_PORT=3307

echo [1/3] 检查环境...
"%JAVA_HOME%\bin\java" -version >nul 2>&1 || ( echo 未找到 Java & pause & exit /b )
where mvn >nul 2>&1 || ( echo 未找到 Maven & pause & exit /b )
where node >nul 2>&1 || ( echo 未找到 Node.js & pause & exit /b )
where npm >nul 2>&1 || ( echo 未找到 npm & pause & exit /b )
echo 环境检查通过

echo.
echo [2/3] 启动 MySQL...
netstat -ano | findstr :%MYSQL_PORT% >nul
if %errorlevel% equ 0 (
    echo MySQL 已在端口 %MYSQL_PORT% 运行
) else (
    if not exist "%MYSQL_DATA%\mysql" (
        echo 初始化 MySQL 数据目录...
        "%MYSQL_HOME%\bin\mysqld" --initialize-insecure --datadir="%MYSQL_DATA%" >nul 2>&1
    )
    echo 启动 MySQL...
    start "UAMS-MySQL" /MIN "%MYSQL_HOME%\bin\mysqld" --datadir="%MYSQL_DATA%" --port=%MYSQL_PORT%
    timeout /t 5 /nobreak >nul
)

echo.
echo [3/3] 启动服务...
start "UAMS-Backend" cmd /c "cd /d backend && mvn spring-boot:run"
start "UAMS-Frontend" cmd /c "cd /d frontend && npm run dev"

echo.
echo MySQL: http://localhost:%MYSQL_PORT%
echo 后端: http://localhost:8080
echo 前端: http://localhost:5173
echo.
pause
