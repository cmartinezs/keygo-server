@echo off
REM Test runner for H2 validation
REM This runs the data-local.sql validation test to ensure it loads properly

setlocal enabledelayedexpansion

cd /d C:\Users\cmartinezs\IdeaProjects\keygo-server

echo Testing data-local.sql with H2 profile...
echo.

REM Run the specific test
call mvnw.cmd -pl keygo-run clean test -Dtest=DataLocalSqlValidationTest -q -DskipOtherTests 2>&1

REM Capture exit code
set exitcode=%ERRORLEVEL%

if %exitcode% equ 0 (
    echo.
    echo SUCCESS: H2 startup with data-local.sql validation passed
    exit /b 0
) else (
    echo.
    echo FAILURE: H2 startup with data-local.sql validation failed with exit code %exitcode%
    exit /b %exitcode%
)
