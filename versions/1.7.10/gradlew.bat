@echo off
setlocal
set "UAB_GRADLE_PROJECT_DIR=%~dp0."
set "UAB_GRADLE_JAVA_VERSION=25"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0..\..\scripts\run-gradle.ps1" %*
exit /b %ERRORLEVEL%
