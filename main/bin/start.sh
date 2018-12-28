#!/bin/sh
CURRENT=`pwd`
cd ..
CLIENT_HOME=`pwd`

CONFPATH=$CLIENT_HOME/conf
RUNJAR=$CLIENT_HOME/lib/${artifactId}-${version}.jar
##some properties needed by app
PROPERTIES_LOG4J_LOGPATH=log4j.logPath
## run!
java -Xbootclasspath/a:$CONFPATH -Xmx256m -Xms100m -Dsun.java2d.opengl=False -Dsun.java2d.xrender=True -Dconfig.path=$CONFPATH -jar $RUNJAR
