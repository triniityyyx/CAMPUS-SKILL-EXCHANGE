@echo off
echo ==================================================
echo   Building Campus Skill Exchange Project...
echo ==================================================
if not exist "bin" mkdir bin
javac -cp "lib/mysql-connector-j.jar" src/*.java src/model/*.java src/dao/*.java src/service/*.java src/ui/*.java src/util/*.java -d bin
if %ERRORLEVEL% EQU 0 (
    echo [BUILD SUCCESS] Project compiled cleanly to bin/
) else (
    echo [BUILD FAILED] Please check compilation errors.
)
pause
