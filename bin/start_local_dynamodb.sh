#!/bin/sh

JAVA_CMD=java
APP_CLASS=com.amazonaws.services.dynamodbv2.local.main.ServerRunner
APP_DIR=`dirname $0`/..
DYNAMODB_PORT=4567

DATA_DIR=$1

CLASSPATH=$APP_DIR/lib/'*'

if [ -z "$DATA_DIR" ] ; then
	echo "Usage: $0 data-directory"
	exit
fi

if [ ! -d "$DATA_DIR" ] ; then
	echo "Creating data directory $DATA_DIR"
	mkdir -p "$DATA_DIR" || ( echo "Failed to create directory $DATA_DIR" ; exit 1 )
fi

if [ -n "$JAVA_HOME" ] ; then
	JAVA_CMD=$JAVA_HOME/bin/java
fi

echo "Starting local instance of DynamoDB with the data in $DATA_DIR directory"

exec $JAVA_CMD -cp "$CLASSPATH" $APP_CLASS -dbPath $DATA_DIR -port $DYNAMODB_PORT -sharedDb

