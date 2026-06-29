@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0backend"
mvn spring-boot:run
