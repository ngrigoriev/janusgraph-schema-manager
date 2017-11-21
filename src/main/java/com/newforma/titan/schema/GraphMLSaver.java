package com.newforma.titan.schema;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.janusgraph.core.JanusGraph;

public class GraphMLSaver {

    private static final Logger LOG = LoggerFactory.getLogger(GraphMLSaver.class);

    private final String saveToLoad;
    private JanusGraph graph;

    public GraphMLSaver(JanusGraph graph, final String saveToLoad) {
        this.saveToLoad = saveToLoad;
        this.graph = graph;
    }

    void run() throws SchemaManagementException {
        final File graphMLFile = new File(saveToLoad);

        LOG.info("Writing GraphML data to " + graphMLFile);

        final GraphMLWriter graphMLWriter = GraphMLWriter.build().normalize(true).create();

        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(graphMLFile))) {
            graphMLWriter.writeGraph(os, graph);
        } catch (IOException e) {
            throw new SchemaManagementException("Failed to load GraphML data from " + graphMLFile + " into the graph",
                    e);
        }
    }

}
