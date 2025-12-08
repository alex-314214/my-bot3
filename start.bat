@echo off
make 1>&2
if %errorlevel% neq 0 exit /b %errorlevel%
java -cp ".;text-file-handler-2.0.1.jar;json.jar" Bot
