@echo off

REM Run this batch file with the following command:
REM cmd /V:ON /C run_really.bat

set THIS_DIR=.\
REM if "%OS%" == "Windows_NT" set THIS_DIR=%~dp0%

set INGRID_HOME=%THIS_DIR%
set LIBS=%INGRID_HOME%lib
set CONF=%INGRID_HOME%conf

set CLASSPATH=%CONF%;%THIS_DIR%
set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar
FOR %%c IN ("%LIBS%\*.jar") DO set CLASSPATH=!CLASSPATH!;%%c

REM echo %CLASSPATH% 
if not "%INGRID_JAVA_HOME%" == "" set JAVA_HOME=%INGRID_JAVA_HOME%
set JAVA=%JAVA_HOME%\bin\java
set JAVA_HEAP_MAX=-Xmx1000m 
if not "%INGRID_HEAPSIZE%" == "" set JAVA_HEAP_MAX=-Xmx%INGRID_HEAPSIZE%m

echo ------------------
echo jvm
echo %JAVA%
echo %JAVA_HEAP_MAX%
echo ------------------

%JAVA% %JAVA_HEAP_MAX% %INGRID_OPTS% -cp "%CLASSPATH%" %1 %2 %3 %4 %5 %6 %7 %8 %9 %10 %11 %12 %13 %14 %15
