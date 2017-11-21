package com.newforma.titan.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import com.newforma.titan.schema.types.GraphIndexDef;
import com.newforma.titan.schema.types.GraphSchemaDef;
import com.newforma.titan.schema.types.LocalEdgeIndexDef;
import com.newforma.titan.schema.types.LocalPropertyIndexDef;
import com.newforma.titan.schema.types.SchemaEdgeLabel;
import com.newforma.titan.schema.types.SchemaPropertyDef;
import com.newforma.titan.schema.types.SchemaVertexLabel;
import com.newforma.titan.schema.validator.SchemaValidationException;

/**
 * Keeps the graph state when manipulating the schema
 * @author Nikolai
 *
 */
@NotThreadSafe
public class GraphState {

	public enum ElementType {VERTEX, EDGE, PROPERTY, INDEX, LOCAL_INDEX}

	final Map<ElementType, Set<String>> existingRelations = new HashMap<>();
	final Map<ElementType, Set<String>> pendingRelations = new HashMap<>();
	final List<String> allIndexNames = new ArrayList<>(16);
	final List<String> newIndexNames = new ArrayList<>(16);

	final Map<String, Object> indexMap = new HashMap<String, Object>();

	final Map<String, SchemaPropertyDef> propertyMap = new HashMap<>();
	final Map<String, SchemaVertexLabel> vertexMap = new HashMap<>();
	final Map<String, SchemaEdgeLabel> edgeMap = new HashMap<>();

	private Map<String, String> docColorsByTag = new HashMap<>();


	final GraphSchemaDef graphSchemaDef;

	private boolean ttlSupported;

	GraphState(GraphSchemaDef graphSchemaDef) throws SchemaValidationException {
		for(ElementType et: ElementType.values()) {
			existingRelations.put(et, new HashSet<String>());
			pendingRelations.put(et, new HashSet<String>());
		}
		this.graphSchemaDef = graphSchemaDef;
		indexSchema();
	}

	private void indexSchema() throws SchemaValidationException {
		for(final SchemaPropertyDef p: graphSchemaDef.getProperties()) {
			if (propertyMap.put(p.getKey(), p) != null) {
				throw new SchemaValidationException("Duplicate property key \"" + p.getKey() + "\"");
			}
		}
		for(final SchemaVertexLabel v: graphSchemaDef.getVertices()) {
			if (vertexMap.put(v.getLabel(), v) != null) {
				throw new SchemaValidationException("Duplicate vertex label \"" + v.getLabel() + "\"");
			}
		}
		for(final SchemaEdgeLabel e: graphSchemaDef.getEdges()) {
			if (edgeMap.put(e.getLabel(), e) != null) {
				throw new SchemaValidationException("Duplicate edge label \"" + e.getLabel() + "\"");
			}
		}

		for(final GraphIndexDef i: graphSchemaDef.getGraphIndexes()) {
			if (indexMap.put(i.getName(), i) != null) {
				throw new SchemaValidationException("Duplicate index name \"" + i.getName() + "\"");
			}
		}
		for(final LocalEdgeIndexDef i: graphSchemaDef.getLocalEdgeIndexes()) {
			if (indexMap.put(i.getName(), i) != null) {
				throw new SchemaValidationException("Duplicate index name \"" + i.getName() + "\"");
			}
		}
		for(final LocalPropertyIndexDef i: graphSchemaDef.getLocalPropertyIndexes()) {
			if (indexMap.put(i.getName(), i) != null) {
				throw new SchemaValidationException("Duplicate index name \"" + i.getName() + "\"");
			}
		}
	}

	public boolean addElement(ElementType relationType, String keyOrLabel) {
		return existingRelations.get(relationType).add(keyOrLabel);
	}

	public boolean elementExists(ElementType relationType, String keyOrLabel) {
		return existingRelations.get(relationType).contains(keyOrLabel);
	}

	public boolean addPendingElement(ElementType relationType, String keyOrLabel) {
		return pendingRelations.get(relationType).add(keyOrLabel);
	}

	public boolean pendingElementExists(ElementType relationType, String keyOrLabel) {
		return pendingRelations.get(relationType).contains(keyOrLabel);
	}

	public GraphSchemaDef getGraphSchemaDef() {
		return graphSchemaDef;
	}

	public boolean isTtlSupported() {
		return ttlSupported;
	}

	public void setTtlSupported(boolean ttlSupported) {
		this.ttlSupported = ttlSupported;
	}

	public void addIndex(String indexName, boolean isNew) {
		this.allIndexNames.add(indexName);
		if (isNew) {
			this.newIndexNames.add(indexName);
		}
	}

	public Collection<String> getAllIndexes() {
		return new ArrayList<String>(allIndexNames);
	}

	public Collection<String> getNewIndexes() {
		return new ArrayList<String>(newIndexNames);
	}


	public Object getIndexDef(String indexName) {
		return indexMap.get(indexName);
	}

	public SchemaPropertyDef getProperty(String key) {
		return propertyMap.get(key);
	}

	public SchemaVertexLabel getVertex(String label) {
		return vertexMap.get(label);
	}

	public SchemaEdgeLabel getEdge(String label) {
		return edgeMap.get(label);
	}

    public Map<String, String> getDocColorsByTag() {
        return docColorsByTag;
    }

    public void setDocColorsByTag(Map<String, String> docColorsByTag) {
        this.docColorsByTag = docColorsByTag;
    }
}
