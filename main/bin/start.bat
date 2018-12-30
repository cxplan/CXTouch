@echo off
cd ..

set CONFPATH=conf/
set LIB_PATH=lib
set RUNJAR=lib/${artifactId}-${version}.jar
rem run!
@echo on
@java -Xbootclasspath/a:%CONFPATH% -Xmx256m -Xms100m -Dsun.java2d.opengl=False -Dsun.java2d.xrender=True -Dconfig.path=%CONFPATH% -jar %RUNJAR%
