package com.newforma.titan.schema;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.newforma.titan.schema.types.DoctagMeta;
import com.newforma.titan.schema.types.GraphSchemaDef;

public class DocGenerator {

	private static final String INDEX_FILE_NAME = "index.xml";
	private static final String VERTICES_FILE_NAME = "vertices.xml";
	private static final String EDGES_FILE_NAME = "edges.xml";
	private static final String PROPERTIES_FILE_NAME = "properties.xml";
	private static final String GRAPH_INDEXES_FILE_NAME = "graph-indexes.xml";
	private static final String LOCAL_EDGE_INDEXES_FILE_NAME = "local-edge-indexes.xml";
	private static final String LOCAL_PROPERY_INDEXES_FILE_NAME = "local-property-indexes.xml";
	private static final String ALLINONE_FILE_NAME = "allinone.xml";

	private static final String XSLT_FILE_NAME = "styles.xsl";
	private static final String CSS_FILE_NAME = "styles.css";

	private static final String DEFAULT_XSLT_RESOURCE = "/doc-styles/styles.xsl";
	private static final String DEFAULT_CSS_RESOURCE = "/doc-styles/styles.css";

	private final XmlMapper mapper;

	private final SchemaTransformer schemaTransformer;

	DocGenerator() {
		XmlFactory xmlFactory = new XmlFactory();
		this.mapper = new XmlMapper(xmlFactory);
		mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
		schemaTransformer = new SchemaTransformer();
	}

	public void generate(final GraphState graphState, final String targetDir) throws IOException {
		final File dir = new File(targetDir);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new IOException("Failed to create directory " + dir);
			}
		}

		final GraphSchemaDef graphSchema = schemaTransformer.transformForDocumentation(graphState);
		final Map<String, DoctagMeta> doctagMetas =
		        graphState.getGraphSchemaDef().getDoctagsMeta().
		            stream().
		            collect(Collectors.toMap(DoctagMeta::getDoctag, Function.identity()));

		serializeDoc(new File(dir, PROPERTIES_FILE_NAME),
				ImmutableMap.<String, Object> builder()
					.put("schema-meta", graphSchema.getGraph())
					.put("schema-doctags-meta", doctagMetas)
					.put("schema-properties",
							ImmutableMap.<String, Object> builder()
								.put("property", graphSchema.getProperties())
								.build())
					.build());
		serializeDoc(new File(dir, VERTICES_FILE_NAME), ImmutableMap.<String, Object> builder()
					.put("schema-meta", graphSchema.getGraph())
					.put("schema-doctags-meta", doctagMetas)
					.put("schema-vertices",
							ImmutableMap.<String, Object> builder()
								.put("vertex", graphSchema.getVertices())
								.build())
					.build());
		serializeDoc(new File(dir, EDGES_FILE_NAME),
				ImmutableMap.<String, Object> builder()
					.put("schema-meta", graphSchema.getGraph())
					.put("schema-doctags-meta", doctagMetas)
					.put("schema-edges",
							ImmutableMap.<String, Object> builder()
								.put("edge", graphSchema.getEdges())
								.build())
					.build());
		serializeDoc(new File(dir, GRAPH_INDEXES_FILE_NAME),
				ImmutableMap.<String, Object> builder()
					.put("schema-meta", graphSchema.getGraph())
					.put("schema-doctags-meta", doctagMetas)
					.put("schema-indexes",
							ImmutableMap.<String, Object> builder()
								.put("index", graphSchema.getGraphIndexes())
								.build())
					.build());
		serializeDoc(new File(dir, LOCAL_PROPERY_INDEXES_FILE_NAME),
				ImmutableMap.<String, Object> builder()
					.put("schema-meta", graphSchema.getGraph())
					.put("schema-doctags-meta", doctagMetas)
					.put("schema-local-property-indexes",
							ImmutableMap.<String, Object> builder()
								.put("property-index", graphSchema.getLocalPropertyIndexes())
								.build())
					.build());
		serializeDoc(new File(dir, LOCAL_EDGE_INDEXES_FILE_NAME),
				ImmutableMap.<String, Object> builder()
					.put("schema-meta", graphSchema.getGraph())
					.put("schema-doctags-meta", doctagMetas)
					.put("schema-local-edge-indexes",
							ImmutableMap.<String, Object> builder()
								.put("edge-index", graphSchema.getLocalEdgeIndexes())
								.build())
					.build());
		serializeDoc(new File(dir, INDEX_FILE_NAME),
				ImmutableMap.<String, Object> builder()
					.put("schema-meta", graphSchema.getGraph())
					.build());

	      serializeDoc(new File(dir, ALLINONE_FILE_NAME),
	                ImmutableMap.<String, Object> builder()
	                    .put("doc-meta",
	                            ImmutableList.<Object>builder().add("allinone").build())
	                    .put("schema-meta", graphSchema.getGraph())
	                    .put("schema-doctags-meta", doctagMetas)
	                    .put("schema-vertices",
	                            ImmutableMap.<String, Object> builder()
	                                .put("vertex", graphSchema.getVertices())
	                                .build())
	                    .put("schema-edges",
	                            ImmutableMap.<String, Object> builder()
	                                .put("edge", graphSchema.getEdges())
	                                .build())
	                    .put("schema-properties",
	                            ImmutableMap.<String, Object> builder()
	                                .put("property", graphSchema.getProperties())
	                                .build())
	                    .put("schema-indexes",
	                            ImmutableMap.<String, Object> builder()
	                                .put("index", graphSchema.getGraphIndexes())
	                                .build())
	                    .put("schema-local-property-indexes",
	                            ImmutableMap.<String, Object> builder()
	                                .put("property-index", graphSchema.getLocalPropertyIndexes())
	                                .build())
	                    .put("schema-local-edge-indexes",
	                            ImmutableMap.<String, Object> builder()
	                                .put("edge-index", graphSchema.getLocalEdgeIndexes())
	                                .build())
	                    .build());

		writeTransformations(dir);
		writeStyles(dir);
	}

	private void serializeDoc(final File file, Map<String, Object> docMap) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write("<?xml version='1.0' encoding='UTF-8'?>\n");
		writer.write("<?xml-stylesheet href=\"");
		writer.write(XSLT_FILE_NAME);
		writer.write("\" type=\"text/xsl\"?>\n");
		mapper.writer().withDefaultPrettyPrinter().withRootName("doc-root").writeValue(writer,docMap);
		// whoever wrote it does no know that it is a bad practice to close something you did no open in the first place...
	}

	private void writeTransformations(File targetDir) throws IOException {
		String styleFile = StringUtils.defaultString(System.getenv("DOC_XSLT_FILE"), System.getProperty("doc.xslt.file"));
		if (!StringUtils.isEmpty(styleFile)) {
			FileUtils.copyFile(new File(styleFile), new File(targetDir, XSLT_FILE_NAME));
		} else {
			// using default
			try (FileOutputStream os = new FileOutputStream(new File(targetDir, XSLT_FILE_NAME))) {
				IOUtils.copy(DocGenerator.class.getResourceAsStream(DEFAULT_XSLT_RESOURCE), os);
			}
		}
	}

	private void writeStyles(File targetDir) throws IOException {
		String styleFile = StringUtils.defaultString(System.getenv("DOC_CSS_FILE"), System.getProperty("doc.css.file"));
		if (!StringUtils.isEmpty(styleFile)) {
			FileUtils.copyFile(new File(styleFile), new File(targetDir, CSS_FILE_NAME));
		} else {
			// using default
			try (FileOutputStream os = new FileOutputStream(new File(targetDir, CSS_FILE_NAME))) {
				IOUtils.copy(DocGenerator.class.getResourceAsStream(DEFAULT_CSS_RESOURCE), os);
			}
		}
	}
}
