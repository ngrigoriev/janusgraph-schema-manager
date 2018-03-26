[![Build Status](https://travis-ci.org/graph-lab/janusgraph-schema-manager.svg?branch=master)](https://travis-ci.org/graph-lab/janusgraph-schema-manager)

# Titan/JanusGraph Schema Manager


This is a simple schema manager that allows to maintain and apply JanusGraph (formely Titan) database schema.

The tool is written in Java and has not been carefully tested yet. Still, given the alternatives, it should be better than trying to run Java-based management calls via Gremlin or manually.

# Prerequisites

## Windows Users

- Download Maven http://archive.apache.org/dist/maven/maven-3/3.5.0/binaries/apache-maven-3.5.0-bin.zip
- Extract to C:\Program Files\apache-maven-3.5.0
- Add C:\Program Files\apache-maven-3.5.0\bin to your system %path% environment variable

# Building

By default the tool is built without the sandbox components (see below).

Run "mvn clean package" to build the schema manager or "mvn -Psandbox clean package" if you want to build the schema manager with sandbox components.
The distribution package will be available as target/janusgraph-schema-manager-<Version>-SNAPSHOT-dist\[-sandbox].zip


# Running the schema manager

To create a test graph using local DynamoDB backend you can use the following command:

```
bin/schema_manager.sh  -g graph.properties -w schema.json
```

To generate the documentation from the schema (without loading the schema into the graph) run this command:

```
bin/schema_manager.sh  -g graph.properties -d generated-docs schema.json
```

To load some sample data in the graph run this command:

```
bin/schema_manager.sh  -g graph.properties -l data-graphml.xml schema.json

```

# Schema format

Please refer to src/main/resources/schema/graph-schema-def-1.0.json for details. examples/sandbox/graph-of-gods/graph-of-the-gods-v1.0.json contains a working example of the famous Graph Of The Gods.

## Schema tagging

"doctags" property may be used for properties, edges, vertexes and indexes, as well as on the nested property and relationship descriptions. This property contains list of comma-separated tags to be applied to the element. The tags can be automatically cascaded in different ways to the nested elements (e.g. from the vertex to the declared properties and relationships). The cascading is controlled by "doctag_cascading" property of the schema. Note that this setting and cascading itself is done only in the current schema file, e.g. it is not extended to other files if the include mechanism is used.

Tagging can be used to partition the graph schema into logical fragments that may be analyzed separately.

## Filtering the generated documentation using tags

"-t" option can be used to limit the documentation scope to the specific tag(s) or to remove the elements with particular tag(s) from the documentation. Refer to the usage information for the value format.

Note that the documentation generated with the filter may be inconsistent - it may be missing some elements that do not pass the filter.

Tag filter does not affect the schema generation and validation.


### Schema coloring using tags

Doctags can optionally have ":color" suffix. If specified, the edges and vertices with corresponding tags will be drawn in the DOT file using that color. For supported color names please refer to http://www.graphviz.org/content/color-names.

Note that the color matching logic is as follows: tags for each vertex/edge are evaluated from the first to the last. First tag that has the color specified in the filter determines the color of this element. To support this logic you may want to place more specific tags first.


# Sandbox environment

The schema manager comes with a feature allowing the creation of a local graph environment. The environment includes:

- local DynamoDB instance
- local Elasticsearch instance
- local Gremlin server
- Gremlin console

The schema manager can be used against this environment just like against the production one.

It is worth mentioning here that we are not running this schema manager via Gremlin connection. While it is technically possible, I am not sure it is the way to go. For now the tool runs with embedded JanusGraph instance that connects to both storage backend (DynamoDB) and ES instance, applies the changes and shuts down.

## Starting the test environment

The test environment stores the graph data (DynamoDB) ES data in the directories you provide as arguments to the startup scripts. Thus, by erasing these directories and restarting everything you return to the empty database.

Build & get packages:

```
mvn -Psandbox clean package
```

Unpack target/janusgraph-schema-manager-<Version>-SNAPSHOT-dist\[-sandbox].zip somewhere.

Start local DynamoDB with the data stored in /tmp/db by running:

```
bin/start_local_dynamodb.sh /tmp/db
```

Start local ES instance with the data stored in /tmp/es-db by running:

```
bin/start_local_es.sh /tmp/es-db
```

Start local Gremlin server instance using default configuration from examples/sandbox/gremlin/gremlin-server.yaml by running:

```
bin/start_local_gremlin_server.sh
```

Finally, you can start Gremlin console using:

```
bin/start_local_gremlin_console.sh
```

All above-mentioned applications use the configuration files from "examples/sandbox" directory.


Note that the console does not connect anywhere by default. You need to connect to the local Gremlin server first using the following command:

```
:remote connect tinkerpop.server ./examples/sandbox/gremlin/gremlin-localhost.yaml
```

To test that everything is working you can run this command in Gremlin console:

```
gremlin> :> g
gremlin:graphtraversalsource[standardtitangraph[com.amazon.titan.diskstorage.dynamodb.DynamoDBStoreManager:[127.0.0.1]], standard]
```

This means that the graph called "g" is ready to be queried.




# Notes on the generated documentation

The documentation is generated in the form of XML files with the XSL+CSS styles used for rendering them. Due to various security limits, many browsers refuse to process such a combination locally. One of the simple ways to work around the problem is to access your documentation with an HTTP server:

```
python -m SimpleHTTPServer
```

## Graph visualization

A primitive DOT file is generated (graph.dot) along with the documentation. You can visualize it using GraphViz or another tool. On Mac you can install GraphViz with HomeBrew, for example. The resulting file can be displayed with the following command:

```
dot -Tpdf generated-docs/graph.dot  | open -f -a /Applications/Preview.app
```

# Graph maintenance

## Rebuilding the indexes

Normally when new index is created it gets enabled and starts working from this moment. However, sometimes the existing data must be re-indexed - for example, if the new index is built on the data that already existed in the graph or after some sort of recovery.

The schema manager can re-index either one named index, all defined indexes, new ones created during last run or all indexes that are not currently in usable state.


### Reindex one specific index

```
bin/schema_manager.sh  -g graph.properties -i index-name -w schema.json
```

### Reindex everything

```
bin/schema_manager.sh  -g graph.properties -r ALL -w schema.json
```

### Reindex only newly created indexes

```
bin/schema_manager.sh  -g graph.properties -r NEW -w schema.json
```

### Reindex only unavailable indexes

```
bin/schema_manager.sh  -g graph.properties -r UNAVAILABLE -w schema.json
```

In this case the state of each index defined in the schema will be verified. If an index or any of its properties is in the state REGISTERED or INSTALLED, the tool will re-index the data and enable the index. If the index or any of its components is in DISABLED state, it will be ignored.

## Running a Gremlin/Groovy script without the console

You can run one or more Gremlin/Groovy scripts directly using a simple script runner. 

```
bin/run_script -g graph.properties script1.groovy script2.groovy ...
```

It stops on the first failure and the exit status is set to 1 in case of an error.


# Additional documentation

## [Graph schema visualization with GraphViz](docs/README-graphviz.md)


# Plans for future
- better validation of the schema before applying it and/or reviewing how transactions can be used to avoid partial failures

# References
http://docs.janusgraph.org/latest/
