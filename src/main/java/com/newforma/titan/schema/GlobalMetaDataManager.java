package com.newforma.titan.schema;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.newforma.titan.schema.types.GraphSchemaDef;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.core.schema.SchemaAction;
import org.janusgraph.core.schema.SchemaStatus;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.graphdb.database.management.ManagementSystem;

public class GlobalMetaDataManager {

	// Get latest with Gremlin:
	// g.V().hasLabel("GRAPHREVISIONMETADATAVERTEX").has("graphmetadataidproperty", "globalmetadata").order().by("graphmetadataucreationtime", decr).limit(1).valueMap(true)

	public static final String METADATA_VERTEX_LABEL = "GRAPHREVISIONMETADATAVERTEX";
	public static final String METADATA_ID_PROPERTY_KEY = "graphmetadataidproperty";
	public static final String METADATA_ID_PROPERTY_VALUE = "globalmetadata";
	public static final String METADATA_DATA_PROPERTY_KEY = "graphmetadataproperty";
	public static final String METADATA_TIME_PROPERTY_KEY = "graphmetadataucreationtime";
	public static final String METADATA_ID_GRAPH_INDEX = "graphmetadatapropertyidx";

	public static final String META_PROP_GRAPH_NAME = "graph_name";
	public static final String META_PROP_MODEL_VERSION = "model_version";
	public static final String META_PROP_UPDATED_ON = "updated_on";

	private static final Logger LOG = LoggerFactory.getLogger(GlobalMetaDataManager.class);

	GlobalMetaDataManager() {
		// does nothing
	}

	void updateGraph(final JanusGraph graph, final GraphSchemaDef schemaDef, final SchemaManager schemaManager) throws IOException, SchemaManagementException {
		ensureConfigured(graph, schemaManager);
		tag(graph, schemaDef);
	}


	private void tag(final JanusGraph graph, GraphSchemaDef schemaDef) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 'T' HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		ObjectMapper mapper = new ObjectMapper();
		final String graphInfo = mapper.writeValueAsString(ImmutableMap.<String, String>builder()
					.put(META_PROP_GRAPH_NAME, schemaDef.getGraph().getName())
					.put(META_PROP_MODEL_VERSION, schemaDef.getGraph().getModelVersion())
					.put(META_PROP_UPDATED_ON, sdf.format(new Date())).build());

		Transaction tx = graph.tx();
		// creating new metadata vertex
		JanusGraphVertex metaVertex = graph.addVertex(METADATA_VERTEX_LABEL);
		metaVertex.property(METADATA_ID_PROPERTY_KEY, METADATA_ID_PROPERTY_VALUE);
		metaVertex.property(METADATA_DATA_PROPERTY_KEY, graphInfo);
		metaVertex.property(METADATA_TIME_PROPERTY_KEY, Long.valueOf(System.currentTimeMillis()));
		tx.commit();
	}


	private void ensureConfigured(final JanusGraph graph, final SchemaManager schemaManager) throws IOException, SchemaManagementException {

		graph.tx().rollback();

		JanusGraphManagement mgmt;

		mgmt = graph.openManagement();
		if (mgmt.getVertexLabel(METADATA_VERTEX_LABEL) == null) {
			LOG.info("Initializing vertex label {}", METADATA_VERTEX_LABEL);
			mgmt.makeVertexLabel(METADATA_VERTEX_LABEL).setStatic().make();
			mgmt.commit();
		} else {
			LOG.debug("Vertex label {} exists", METADATA_VERTEX_LABEL);
			mgmt.rollback();
		}

		mgmt = graph.openManagement();
		if (mgmt.getPropertyKey(METADATA_ID_PROPERTY_KEY) == null) {
			LOG.info("Initializing property key {}", METADATA_ID_PROPERTY_KEY);
			mgmt.makePropertyKey(METADATA_ID_PROPERTY_KEY).cardinality(Cardinality.SINGLE).dataType(java.lang.String.class).make();
			mgmt.commit();
		} else {
			LOG.debug("Property key {} exists", METADATA_ID_PROPERTY_KEY);
			mgmt.rollback();
		}

		mgmt = graph.openManagement();
		if (mgmt.getPropertyKey(METADATA_TIME_PROPERTY_KEY) == null) {
			LOG.info("Initializing property key {}", METADATA_TIME_PROPERTY_KEY);
			mgmt.makePropertyKey(METADATA_TIME_PROPERTY_KEY).cardinality(Cardinality.SINGLE).dataType(java.lang.Long.class).make();
			mgmt.commit();
		} else {
			LOG.debug("Property key {} exists", METADATA_TIME_PROPERTY_KEY);
			mgmt.rollback();
		}

		mgmt = graph.openManagement();
		if (mgmt.getPropertyKey(METADATA_DATA_PROPERTY_KEY) == null) {
			LOG.info("Initializing property key {}", METADATA_DATA_PROPERTY_KEY);
			mgmt.makePropertyKey(METADATA_DATA_PROPERTY_KEY).cardinality(Cardinality.SINGLE).dataType(java.lang.String.class).make();
			mgmt.commit();
		} else {
			LOG.debug("Property key {} exists", METADATA_DATA_PROPERTY_KEY);
			mgmt.rollback();
		}

		mgmt = graph.openManagement();
		final JanusGraphIndex idx = mgmt.getGraphIndex(METADATA_ID_GRAPH_INDEX);
		if (idx == null) {
			LOG.info("Initializing graph index {}", METADATA_ID_GRAPH_INDEX);
			mgmt.buildIndex(METADATA_ID_GRAPH_INDEX, Vertex.class)
				.addKey(mgmt.getPropertyKey(METADATA_ID_PROPERTY_KEY))
				.indexOnly(mgmt.getVertexLabel(METADATA_VERTEX_LABEL))
				.buildCompositeIndex();
			mgmt.commit();
			try {
				ManagementSystem.awaitGraphIndexStatus(graph, METADATA_ID_GRAPH_INDEX).call();
			} catch (InterruptedException e) {
				throw new IOException("Interrupted while waiting for the index " + METADATA_ID_GRAPH_INDEX + " to be available");
			}
			mgmt = graph.openManagement();
			// no need to re-index
			try {
				mgmt.updateIndex(mgmt.getGraphIndex(METADATA_ID_GRAPH_INDEX), SchemaAction.ENABLE_INDEX).get();
			} catch (InterruptedException | ExecutionException e) {
				throw new SchemaManagementException("Unable to update index " + METADATA_ID_GRAPH_INDEX, e);
			}
			mgmt.commit();
		} else if (idx.getIndexStatus(mgmt.getPropertyKey(METADATA_ID_PROPERTY_KEY)) == SchemaStatus.DISABLED) {
		    throw new SchemaManagementException("Metadata index " + METADATA_ID_GRAPH_INDEX + " is DISABLED");
		} else {
		    mgmt.rollback();
		    LOG.info("Metadata index {} exists", METADATA_ID_GRAPH_INDEX);
		    schemaManager.ensureGraphIndexReady(graph, METADATA_ID_GRAPH_INDEX);
		}
	}
}
