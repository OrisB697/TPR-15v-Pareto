@echo off
chcp 65001 >nul 2>&1
title Pareto Set Finder v1.0.0
color 0A

echo ===================================================
echo        ПРОГРАММА ВЫДЕЛЕНИЯ МНОЖЕСТВА ПАРЕТО
echo                  Версия 1.0.0
echo ===================================================
echo.

REM Ищем Java в разных местах
set JAVA_EXE=

REM Проверяем JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set JAVA_EXE=%JAVA_HOME%\bin\java.exe
    )
)

REM Проверяем Program Files
if not defined JAVA_EXE (
    for /d %%i in ("C:\Program Files\Java\jdk*") do (
        if exist "%%i\bin\java.exe" (
            set JAVA_EXE=%%i\bin\java.exe
            goto :found
        )
    )
)

REM Проверяем Eclipse Adoptium
if not defined JAVA_EXE (
    for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk*") do (
        if exist "%%i\bin\java.exe" (
            set JAVA_EXE=%%i\bin\java.exe
            goto :found
        )
    )
)

:found
REM Если Java не найдена
if not defined JAVA_EXE (
    color 0C
    echo ============================================
    echo  ОШИБКА: Java не найдена!
    echo ============================================
    echo.
    echo Установите Java 11 или выше:
    echo https://adoptium.net/
    echo.
    pause
    exit /b 1
)

echo Java найдена: %JAVA_EXE%
echo.

REM Запускаем программу
"%JAVA_EXE%" -jar "%~dp0pareto-optimizer-1.0.0.jar"

if errorlevel 1 (
    echo.
    echo Ошибка при выполнении программы!
)

pause