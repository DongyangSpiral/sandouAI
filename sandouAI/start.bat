@echo off
setlocal
chcp 65001 >nul
title Sandou Drive Launcher

cd /d "%~dp0"

where java >nul 2>&1 || (echo Java 17 is required. Please install JDK 17 and add it to PATH.& pause& exit /b 1)
where mvn >nul 2>&1 || (echo Maven 3.9+ is required. Please install Maven and add it to PATH.& pause& exit /b 1)
where node >nul 2>&1 || (echo Node.js 18+ is required. Please install Node.js and add it to PATH.& pause& exit /b 1)
where npm >nul 2>&1 || (echo npm is required. Please install Node.js and add npm to PATH.& pause& exit /b 1)

if not exist "frontend\node_modules" (
  echo Installing frontend dependencies...
  pushd frontend
  call npm install
  if errorlevel 1 exit /b 1
  popd
)

echo Starting backend on http://localhost:8080 ...
start "Sandou-Backend" cmd /k "cd /d %cd%\backend && mvn spring-boot:run"

echo Starting portal frontend on http://localhost:5173 ...
start "Sandou-Portal" cmd /k "cd /d %cd%\frontend && npm run dev:portal"

echo Starting drive frontend on http://localhost:5174 ...
start "Sandou-Drive" cmd /k "cd /d %cd%\frontend && npm run dev:drive"

echo.
echo Please make sure MySQL, Redis, RabbitMQ and MinIO are running.
echo You can start them with: docker compose up -d
echo.
pause
