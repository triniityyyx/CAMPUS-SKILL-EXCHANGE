@echo off
echo ==================================================
echo   Launching Campus Skill Exchange Application...
echo ==================================================
if not exist "bin\Main.class" (
    echo Compiling project first...
    call build.bat
)
java -cp "bin;lib/mysql-connector-j.jar" Main
pause
