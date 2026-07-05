@echo off
chcp 65001 >nul
setlocal
pushd "%~dp0"
cls

echo ⚡ Building Main Project...
call mvn -q -DskipTests -Dmaven.javadoc.skip=true clean install
if errorlevel 1 (
    echo ❌ Build failed.
    popd
    exit /b %errorlevel%
)

cd examples\Speed
echo 🛠 Compiling Speed benchmark...
call mvn -q -DskipTests -Dmaven.javadoc.skip=true compile
if errorlevel 1 (
    echo ❌ Compile failed.
    popd
    exit /b %errorlevel%
)

echo 🚀 Running Speed benchmark (fastascii.Speed)...
call mvn -q org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=fastascii.Speed
set EXITCODE=%ERRORLEVEL%
popd
exit /b %EXITCODE%
