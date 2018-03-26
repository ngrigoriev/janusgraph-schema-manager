#!/bin/sh

JAVA_CMD=java

BIN_DIR=`dirname $0`

if [ -n "$JAVA_HOME" ] ; then
	JAVA_CMD=$JAVA_HOME/bin/java
fi

APP_CLASS=com.newforma.titan.utils.ScriptRunnerApp
CLASSPATH=$BIN_DIR:$BIN_DIR/../lib/'*'

$JAVA_CMD -cp "$CLASSPATH" -Dlog4j.configuration=log4j-console.properties $APP_CLASS $*
