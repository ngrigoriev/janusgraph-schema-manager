package com.newforma.titan.schema;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.newforma.titan.schema.types.DoctagListType;
import com.newforma.titan.schema.types.DoctagListTypeDeserializer;
import com.newforma.titan.schema.types.GraphSchemaDef;
import com.newforma.titan.schema.types.TTLType;
import com.newforma.titan.schema.types.TTLTypeDeserializer;
import com.newforma.titan.schema.validator.SchemaValidationException;

public class SchemaLoader {

    private static final String SCHEMA_FORMAT_VERSION = "1.0";

    private static final String SCHEMA_RESOURCE = "/schema/graph-schema-def-" + SCHEMA_FORMAT_VERSION + ".json";

    private static final Logger LOG = LoggerFactory.getLogger(SchemaLoader.class);

    private SchemaLoader() {

    }

    public static SchemaLoader getInstance() {
        return new SchemaLoader();
    }

    public GraphSchemaDef loadFrom(File rootFile) throws IOException, SchemaValidationException {
        try (InputStream is = new FileInputStream(rootFile)){
            return loadFrom(is, rootFile.getName(), rootFile.getParentFile());
        }
    }

    // TODO: implement include resolvers
    public GraphSchemaDef loadFrom(final InputStream schemaStream, String rootName, File basePath) throws IOException, SchemaValidationException {
        HashSet<String> includesTracker = new HashSet<>();
        includesTracker.add(new File(basePath, rootName).getAbsolutePath());

        return processIncludes(loadSingleJsonFrom(schemaStream), includesTracker, basePath);
    }

    private GraphSchemaDef processIncludes(GraphSchemaDef rootSchema, HashSet<String> includesTracker, File baseDir)
            throws IOException, SchemaValidationException {
        for (final String includeFile : rootSchema.getIncludes()) {
            File f = new File(includeFile);
            if (!f.isAbsolute()) {
                f = new File(baseDir, includeFile);
            }
            if (!f.exists()) {
                throw new SchemaValidationException("Reference to non-existing include file " + f.getAbsolutePath());
            }
            if (!includesTracker.add(f.getAbsolutePath())) {
                throw new SchemaValidationException(
                        "Include file " + f.getAbsolutePath() + " is referenced more than once");
            }
            LOG.info("Loading included schema from {}", f.getAbsolutePath());

            final GraphSchemaDef nestedSchema;

            try (InputStream is = new FileInputStream(f)) {
                nestedSchema = processIncludes(loadSingleJsonFrom(is), includesTracker,
                    f.getParentFile());
            }

            // merging all the elements of this nested schema into the parent
            rootSchema.getEdges().addAll(nestedSchema.getEdges());
            rootSchema.getVertices().addAll(nestedSchema.getVertices());
            rootSchema.getProperties().addAll(nestedSchema.getProperties());
            rootSchema.getGraphIndexes().addAll(nestedSchema.getGraphIndexes());
            rootSchema.getLocalPropertyIndexes().addAll(nestedSchema.getLocalPropertyIndexes());
            rootSchema.getLocalEdgeIndexes().addAll(nestedSchema.getLocalEdgeIndexes());
            rootSchema.getDoctagsMeta().addAll(nestedSchema.getDoctagsMeta());
        }

        return rootSchema;
    }

    private GraphSchemaDef loadSingleJsonFrom(InputStream jsonStream) throws IOException, SchemaValidationException {
        final JsonNode titanSchema = JsonLoader.fromReader(new InputStreamReader(new BufferedInputStream(jsonStream)));
        final JsonNode titalSchemaDef = JsonLoader.fromResource(SCHEMA_RESOURCE);

        final JsonValidator validator = JsonSchemaFactory.newBuilder().freeze().getValidator();
        final ProcessingReport report;
        try {
            report = validator.validate(titalSchemaDef, titanSchema, true);
        } catch (ProcessingException e) {
            throw new IOException(e);
        }
        if (!report.isSuccess()) {
            final StringBuilder reportMessage = new StringBuilder("JSON validation report:\n");
            for (final ProcessingMessage pm : report) {
                boolean swallowMessage = false;
                if (pm.getLogLevel() == LogLevel.WARNING) {
                    // can be safely ignored
                    if (pm.getMessage()
                            .matches("the following keywords are unknown and will be ignored: \\[javaType\\]")) {
                        swallowMessage = true;
                    }
                    if (pm.getMessage()
                            .matches("the following keywords are unknown and will be ignored: \\[javaName\\]")) {
                        swallowMessage = true;
                    }
                }

                if (!swallowMessage) {
                    reportMessage.append(pm.toString()).append('\n');
                }
            }
            throw new SchemaValidationException("Graph schema validation error: " + reportMessage);
        }

        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(TTLType.class, new TTLTypeDeserializer());
        module.addDeserializer(DoctagListType.class, new DoctagListTypeDeserializer());

        mapper.registerModule(module);

        SchemaTransformer transformer = new SchemaTransformer();

        GraphSchemaDef def =  mapper.treeToValue(titanSchema, GraphSchemaDef.class);

        assertSchemaFormatVersion(def);

        return transformer.cascadeDoctags(def);
    }

    private void assertSchemaFormatVersion(GraphSchemaDef def) throws SchemaValidationException {
        final String schemaFormatVersion = def.getGraph().getSchemaFormatVersion();
        if (StringUtils.isEmpty(schemaFormatVersion)) {
            throw new SchemaValidationException("Missing or invalid schema format version");
        }

        // TODO: in the future this will be more complex than just simple comparison
        if (!SCHEMA_FORMAT_VERSION.equals(schemaFormatVersion)) {
            throw new SchemaValidationException("Unsuported schema format version \"" + schemaFormatVersion +
                    "\", supported version: \"" + SCHEMA_FORMAT_VERSION + "\"");
        }
    }
}
