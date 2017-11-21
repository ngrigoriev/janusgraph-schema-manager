package com.newforma.titan.schema;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.janusgraph.core.JanusGraph;

public class GraphMLLoader {

	private static final Logger LOG = LoggerFactory.getLogger(GraphMLLoader.class);

	private final String fileToLoad;
	private JanusGraph graph;

	GraphMLLoader(JanusGraph graph, final String fileToLoad) {
		this.fileToLoad = fileToLoad;
		this.graph = graph;
	}


	void run() throws SchemaManagementException {
		final File graphMLFile = new File(fileToLoad);

		if (!graphMLFile.canRead()) {
			throw new SchemaManagementException("GraphML file " + graphMLFile + " does not exist or is not readable");
		}

		LOG.info("Loading GraphML data from " + graphMLFile);

		final GraphMLReader graphMLReader = GraphMLReader.build().create();

		try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(graphMLFile))) {
			graphMLReader.readGraph(is, graph);
		} catch (IOException e) {
			throw new SchemaManagementException("Failed to load GraphML data from " + graphMLFile + " into the graph", e);
		}
	}
}
