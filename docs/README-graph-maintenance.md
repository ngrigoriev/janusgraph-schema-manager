# Graph maintenance tools

## Graph instance maintenance

This tool performs some basic maintenance of the registered graph instances.

Usage:

```
bin/instance_tool.sh <options>
```

This tool "pings" all registered graph instances with a meaningless cache eviction message for the internal vertex label type used by the schema manager to store the graph metadata. It waits for the response for the given amount of time. Instances that respond with the cache eviction acknowledgment are considered "live". The ones that fail to respond are considered "dead". The tool can force the dead ones to close if requested.

**WARNING**: this tool relies on the certain internals of JanusGraph implementation. It may not work as expected with newer versions of JanusGraph if something changes. It is recommended to use it first without "-k" option first. It is also recommended to verify the timeout that is needed to wait for all graph instances to respond. In a large or highly loaded cluster, the default value of 60s may be too low.

"-g" option (required) refers to the graph configuration property file.

If "-l" option is used, then the list of all instances will be printed at the end.

Using "-t <seconds>" option it is possible to control the timeout for the tool.

Finally, if "-k" option is specified, the instances that do not respond within the timeout, will be force-closed using ManagementSystem.forceCloseInstance() method.

Example:

```
$ bin/instance_tool.sh -g graph.properties -l
...
Instance summary (id, state):
+ 0a00026a17880-ip-10-0-2-1061		LIVE
+ 0a00026a18831-ip-10-0-2-1061		LIVE
+ 0a00161f31128-ip-10-0-22-311		LIVE
+ 0a00161f31247-ip-10-0-22-311		LIVE
```

