package com.newforma.titan.schema.validator;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.newforma.titan.schema.types.GraphIndexDef;
import com.newforma.titan.schema.types.GraphSchemaDef;
import com.newforma.titan.schema.types.LocalEdgeIndexDef;
import com.newforma.titan.schema.types.LocalPropertyIndexDef;
import com.newforma.titan.schema.types.SchemaEdgeLabel;
import com.newforma.titan.schema.types.SchemaPropertyDef;
import com.newforma.titan.schema.types.SchemaVertexLabel;
import com.newforma.titan.schema.types.GraphIndexDef.IndexType;

public class SchemaValidator {

	private static final Pattern VERSION_NUM_PATTERN = Pattern.compile("^([0-9]+\\.)+[0-9]+$");

	public SchemaValidator() {
	}

	public void validate(GraphSchemaDef schemaDef) throws SchemaValidationException {

		validateStringRegex(schemaDef.getGraph().getModelVersion(), VERSION_NUM_PATTERN);

		if (schemaDef.getGraph().getConventions() != null) {
			validateNames(
					schemaDef.getVertices().stream().map(SchemaVertexLabel::getLabel).collect(Collectors.toSet()),
					schemaDef.getGraph().getConventions().getVertexLabelPattern(),
					"Vertex");
			validateNames(
					schemaDef.getEdges().stream().map(SchemaEdgeLabel::getLabel).collect(Collectors.toSet()),
					schemaDef.getGraph().getConventions().getEdgeLabelPattern(),
					"Edge");
			validateNames(
					schemaDef.getProperties().stream().map(SchemaPropertyDef::getKey).collect(Collectors.toSet()),
					schemaDef.getGraph().getConventions().getPropertyKeyPattern(),
					"Property");
			validateNames(
					schemaDef.getGraphIndexes().stream().map(GraphIndexDef::getName).collect(Collectors.toSet()),
					schemaDef.getGraph().getConventions().getIndexNamePattern(),
					"Index");
			validateNames(
					schemaDef.getLocalEdgeIndexes().stream().map(LocalEdgeIndexDef::getName).collect(Collectors.toSet()),
					schemaDef.getGraph().getConventions().getIndexNamePattern(),
					"Local Edge Index");
			validateNames(
					schemaDef.getLocalPropertyIndexes().stream().map(LocalPropertyIndexDef::getName).collect(Collectors.toSet()),
					schemaDef.getGraph().getConventions().getIndexNamePattern(),
					"Local Property Index");
		}

		validatePropertyDataTypes(schemaDef);
		validateIndexes(schemaDef);

		// TODO: do more validation before we get to the DB changes
	}



	private void validateIndexes(GraphSchemaDef schemaDef) throws SchemaValidationException {
		for (final GraphIndexDef indexDef : schemaDef.getGraphIndexes()) {
			if (indexDef.getIndexType() == IndexType.MIXED) {
				// must have an index backend specified
				if (StringUtils.isEmpty(indexDef.getIndexBackend())) {
					throw new SchemaValidationException(
							"Index backend name must be specified for the mixed index \"" + indexDef.getName() + "\"");
				}
			}
		}
	}

	private void validatePropertyDataTypes(GraphSchemaDef schemaDef) throws SchemaValidationException {
		for (final SchemaPropertyDef propertyDef : schemaDef.getProperties()) {
			try {
				Class.forName(propertyDef.getDataType());
			} catch (ClassNotFoundException e) {
				throw new SchemaValidationException(
						"Unknown data type " + propertyDef.getDataType() + " for property \"" + propertyDef.getKey() + "\"");
			}
		}
	}

	private void validateNames(Set<String> nameSet, Pattern pattern, String relName) throws SchemaValidationException {
		for (String name: nameSet) {
			if (!pattern.matcher(name).matches()) {
				throw new SchemaValidationException(relName + " name \"" + name
						+ "\" does not match defined regex \"" + pattern + "\"");
			}
		}
	}

	private void validateStringRegex(String str, Pattern pattern) throws SchemaValidationException {
		if (!pattern.matcher(str).matches()) {
			throw new SchemaValidationException("String \"" + str + "\" does not match pattern \"" + pattern + "\"");
		}
	}
}
