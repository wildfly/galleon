@REM
@REM Copyright The WildFly Authors
@REM SPDX-License-Identifier: Apache-2.0
@REM

@echo off

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)

set LOGGING_CONFIG=
echo "%JAVA_OPTS%" | findstr /I "logging.configuration" > nul
if errorlevel == 1 (
  set LOGGING_CONFIG="-Dlogging.configuration=file:%DIRNAME%\galleon-cli-logging.properties"
) else (
  echo logging.configuration already set in JAVA_OPTS
)

"%JAVA%" --add-modules=java.se -version >nul 2>&1 && (set MODULAR_JDK=true) || (set MODULAR_JDK=false)
if "%MODULAR_JDK%" == "true" (
  echo "%~1" | findstr /I "\-\-add\-modules" > nul
  if errorlevel == 1 (
    rem Set default modular jdk options
    set "DEFAULT_MODULAR_JVM_OPTIONS=%DEFAULT_MODULAR_JVM_OPTIONS% --add-modules=java.se"
  ) else (
    set "DEFAULT_MODULAR_JVM_OPTIONS="
  )
)
set "JAVA_OPTS=%JAVA_OPTS% %DEFAULT_MODULAR_JVM_OPTIONS%"

"%JAVA%" %JAVA_OPTS% %LOGGING_CONFIG% -jar "%DIRNAME%\galleon-cli.jar" %*
:END
if "x%NOPAUSE%" == "x" pause

