@echo off
set JAVA_HOME=E:\JDK17.0.13
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d E:\Code\sandouAI\sandouAI\backend
mvn spring-boot:run
