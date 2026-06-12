@echo off
chcp 65001 >nul
cls

echo ⚡ Building Main Project...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Build failed. & pause & exit /b %ERRORLEVEL% )

cd examples\Speed
echo 🛠 Compiling Speed Demo...
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Compile failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Speed Demo...
call mvn exec:java -q"-Dexec.mainClass=fastascii.Demo" -q

cd ..\..
pause
