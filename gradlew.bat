@echo off
:: ##########################################################################
:: Gradle start up script for Windows
:: ##########################################################################

:: Add default JVM options here if desired
set DEFAULT_JVM_OPTS=

set APP_NAME=Gradle
set APP_BASE_NAME=%~nx0

:: Resolve APP_HOME (the directory of this script)
set APP_HOME=%~dp0
set MAX_FD=maximum

call :getJava
call :runGradle %*

exit /b

:getJava
:: Determine the Java command to use to start the JVM.
if not "%JAVA_HOME%" == "" (
    if exist "%JAVA_HOME%\jre\bin\java.exe" (
        set JAVACMD=%JAVA_HOME%\jre\bin\java
    ) else (
        set JAVACMD=%JAVA_HOME%\bin\java
    )
) else (
    for /f "delims=" %%I in ('where java') do set JAVACMD=%%I
)

if not exist "%JAVACMD%" (
    echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
    exit /b 1
)

exit /b

:runGradle
:: Path to Gradle wrapper JAR
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper-8.4.jar

:: Run Gradle
"%JAVACMD%" %DEFAULT_JVM_OPTS% -cp "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
