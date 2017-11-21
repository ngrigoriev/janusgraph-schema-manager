#!/bin/sh


JAVA_CMD=java
APP_CLASS=org.apache.tinkerpop.gremlin.server.GremlinServer
BIN_DIR=`dirname $0`

GREMLIN_HOME=$BIN_DIR/../examples/sandbox/gremlin
GREMLIN_CONF=$PWD/$GREMLIN_HOME/gremlin-server.yaml

# need to be absolute paths because of cd done later
CLASSPATH=$PWD/$BIN_DIR:$PWD/$BIN_DIR/../lib/'*'

if [ -n "$JAVA_HOME" ] ; then
	JAVA_CMD=$JAVA_HOME/bin/java
fi

if [ -n "$1" ] ; then
	GREMLIN_CONF=$1
	GREMLIN_HOME=`dirname $GREMLIN_CONF`
fi

if [ ! -f "$GREMLIN_CONF" ] ; then
	echo "Configuration file $GREMLIN_CONF not found"
	exit 1
fi

GREMLIN_CONF_REL=`basename $GREMLIN_CONF`
cd $GREMLIN_HOME

echo "Running local Gremlin server using $GREMLIN_CONF"
exec $JAVA_CMD -Dgremlin.log4j.level=INFO -Dlog4j.configuration=log4j-console.properties -cp "$CLASSPATH" $APP_CLASS $GREMLIN_CONF_REL
