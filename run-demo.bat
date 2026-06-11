@echo off

echo [FastASCII] Running Demo (via JitPack)...
cd examples
call mvn compile exec:java -Dexec.mainClass=FastASCII.Demo
cd ..
pause
