#!/bin/sh

JAVA_CMD=java
APP_CLASS=org.apache.tinkerpop.gremlin.console.Console
BIN_DIR=`dirname $0`

GREMLIN_HOME=$BIN_DIR/../examples/sandbox/gremlin

# need to be absolute paths because of cd done later
CLASSPATH=$BIN_DIR:$BIN_DIR/../lib/'*'

if [ -n "$JAVA_HOME" ] ; then
	JAVA_CMD=$JAVA_HOME/bin/java
fi

cp -f $GREMLIN_HOME/plugins-sample.txt $GREMLIN_HOME/plugins.txt

echo "Starting Gremlin console..."

exec $JAVA_CMD -Dgremlin.log4j.level=INFO -Dlog4j.configuration=log4j-console.properties -Dtinkerpop.ext=$GREMLIN_HOME -cp "$CLASSPATH" $APP_CLASS 
