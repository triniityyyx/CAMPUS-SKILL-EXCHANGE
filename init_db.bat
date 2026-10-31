

@echo off
echo ==================================================
echo   Initializing Database and Sample Data...
echo ==================================================
if not exist "bin\util\DatabaseInitializer.class" (
    echo Compiling project first...
    call build.bat
)
java -cp "bin;lib/mysql-connector-j.jar" util.DatabaseInitializer
pause
