@echo off
set JAVA_HOME=D:\codeTools\Java\JDK17
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d D:\schoolTools\sandouAI\backend
mvn spring-boot:run
