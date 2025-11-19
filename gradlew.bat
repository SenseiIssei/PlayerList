@echo off
SETLOCAL
set DIRNAME=%~dp0
set APP_BASE_NAME=%~n0

"%DIRNAME%gradle\wrapper\gradle-wrapper.jar" %*
