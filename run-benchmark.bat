@echo off
chcp 65001 >nul
cls

echo ⚡ Building Main Project...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Build failed. & pause & exit /b %ERRORLEVEL% )

echo 🛠 Compiling Benchmark...
cd examples\Benchmark
call mvn compile -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Compile failed. & cd ..\.. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Benchmark...
call mvn exec:java -q

cd ..\..
pause
