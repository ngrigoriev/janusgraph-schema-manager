echo "Running Gremlin console against local Gremlin server"

cd examples
copy "plugins-sample.txt" "plugins.txt"
cd ..

set MVN_OPTS="-X"
mvn %MVN_OPTS% -Dgremlin.log4j.level=INFO -Dlog4j.configuration=file:./examples/log4j-console.properties -Dgremlin.server.config=%1 exec:java@local-gremlin-console