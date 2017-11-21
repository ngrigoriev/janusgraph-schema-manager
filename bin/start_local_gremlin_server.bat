set GREMLIN_CONF=examples/gremlin-server.yaml

if "%1" NEQ "" (
    set GREMLIN_CONF=%1
)

if not exist "%GREMLIN_CONF%" echo "Configuration file $GREMLIN_CONF not found"

echo "Running local Gremlin server using %GREMLIN_CONF%"
set MVN_OPTS="-X"
mvn %MVN_OPTS% -Dgremlin.log4j.level=INFO -Dlog4j.configuration=file:./examples/log4j-console.properties -Dgremlin.server.config=$1 -Dexec.args="%GREMLIN_CONF%" exec:java@local-gremlin-server