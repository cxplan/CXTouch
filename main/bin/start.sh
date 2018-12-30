#!/bin/sh
cd ..

CONFPATH=conf
RUNJAR=lib/${artifactId}-${version}.jar
## run!
java -Xbootclasspath/a:$CONFPATH -Xmx256m -Xms100m -Dsun.java2d.opengl=False -Dsun.java2d.xrender=True -Dconfig.path=$CONFPATH -jar $RUNJAR
