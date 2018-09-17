@REM
@REM Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
@REM and other contributors as indicated by the @author tags.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM   http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
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

"%JAVA%" %JAVA_OPTS% %LOGGING_CONFIG% -jar "%DIRNAME%\galleon-cli.jar" %*
:END
if "x%NOPAUSE%" == "x" pause

