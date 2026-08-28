@echo off
setlocal
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%
set DEFAULT_JVM_OPTS=-Xmx64m -Xms64m
if defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXE=java.exe
)
if defined JAVA_HOME if not exist "%JAVA_EXE%" (
  echo ERROR: Java was not found. Install JDK 17 and set JAVA_HOME.
  exit /b 1
)
if not defined JAVA_HOME where java.exe >nul 2>&1
if not defined JAVA_HOME if errorlevel 1 (
  echo ERROR: Java was not found. Install JDK 17 and set JAVA_HOME.
  exit /b 1
)
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
