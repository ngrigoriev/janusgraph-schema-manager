#!/bin/sh

APP_DIR=`dirname $0`/..

ES_HOME_DIR=$1
ES_DATA_DIR=$ES_HOME_DIR/data
ES_LOG_DIR=$ES_HOME_DIR/logs
ES_PLUGINS_DIR=$ES_HOME_DIR/plugins

ES_CONF_DIR=$APP_DIR/examples/sandbox/es-config

ES_CMD=$APP_DIR/extras/elasticsearch-5.6.3/bin/elasticsearch

if [ -z "$ES_HOME_DIR" ] ; then
	echo "Usage: $0 es-home-directory"
	echo "    ES home directory will be used for data, logs and plugins"
	exit
fi

if [ ! -d "$ES_HOME_DIR" ] ; then
	for d in "$ES_HOME_DIR" "$ES_DATA_DIR" "$ES_LOG_DIR" "$ES_PLUGINS_DIR" ; do
		echo "Creating ElasticSearch directory $d"
		mkdir -p "$d" || ( echo "Failed to create directory $d" ; exit 1 )
	done
fi

echo "Starting local instance of ElasticSearch as $ES_CMD with home in $ES_HOME_DIR directory"

	#-Des.config=$ES_CONF_DIR/elasticsearch.yml \
exec $ES_CMD
	-Dlog4j2.disable.jmx=true \
	-Des.path.home=$ES_HOME_DIR \
	-Des.path.conf=$ES_CONF_DIR \
	-Des.path.data=$ES_DATA_DIR \
	-Des.path.logs=$ES_LOG_DIR \
	-Des.path.plugins=$ES_PLUGINS_DIR \

