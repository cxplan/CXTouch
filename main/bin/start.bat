@echo off
set  CURRENT=%cd%
cd ..
SET CLIENT_HOME=%cd%

set CONFPATH=%CLIENT_HOME%\conf\
set LIB_PATH=%CLIENT_HOME%\lib
set RUNJAR=%CLIENT_HOME%/lib/${artifactId}-${version}.jar
rem some properties which need by app
set PROPERTIES_LOG4J_LOGPATH=log4j.logPath
rem run!
@echo on
@java -Xbootclasspath/a:%CONFPATH% -Xmx256m -Xms100m -Dsun.java2d.opengl=False -Dsun.java2d.xrender=True -Dconfig.path=%CONFPATH% -jar %RUNJAR%
