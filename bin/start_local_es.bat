echo "Running local instance of ElasticSearch 1.5.2"
set MVN_OPTS="-X"
mvn %MVN_OPTS% -Dlog4j.configuration=file:./examples/log4j-console.properties exec:java@local-es-server
pause