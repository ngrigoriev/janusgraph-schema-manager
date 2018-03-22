package com.newforma.titan.schema;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.janusgraph.core.Multiplicity;

import com.newforma.titan.schema.types.DoctagListType;
import com.newforma.titan.schema.types.SchemaEdgeLabel;
import com.newforma.titan.schema.types.SchemaPropertyDesc;
import com.newforma.titan.schema.types.SchemaRelationshipDesc;
import com.newforma.titan.schema.types.SchemaVertexLabel;

public class DOTGenerator {

	private static final String DOT_FILE_NAME = "graph.dot";

	private static final String STATIC_V_COLOR = "#e0e0e0";

	DOTGenerator() {
		// does nothing
	}

	public void generate(final GraphState graphState, final String targetDir) throws IOException {
		final DOTGraphWriter dotWriter = new DOTGraphWriter(new File(targetDir, DOT_FILE_NAME),
				graphState.getGraphSchemaDef().getGraph().getName());

		dotWriter.writeGraph(graphState);
	}

	private static String getColorNameByTags(GraphState graphState, final DoctagListType tagList) {
	    if (tagList == null) {
	        return null;
	    }

	    final Map<String, String> colorMap = graphState.getDocColorsByTag();

	    for(final String t: tagList.getTags()) {
	        String c = colorMap.get(t);
	        if (StringUtils.isNotEmpty(c)) {
	            return c;
	        }
	    }

	    return null;
	}

	private static class DOTGraphWriter {

	    private final File outFile;
	    private final String graphName;

		private DOTGraphWriter(File outFile, String graphName) {
		    this.outFile = outFile;
		    this.graphName = graphName;
		}

        private void writeGraph(GraphState graphState)  throws IOException {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
                writer.write("graph ");
                writer.write("GRAPH_" + graphName.replaceAll("[^a-zA-Z0-9_]", "_"));
                writer.write(" {");

                final Set<Triple<String, String, String>> duplicateRelationships = new HashSet<>();
                final Set<String> mockVertices = new HashSet<>();

                writeNodes(writer, graphState, duplicateRelationships, mockVertices);

                writeEdges(writer, graphState, duplicateRelationships, mockVertices);

                writer.write("\n}");
                writer.flush();
                writer.close();
            }
        }

        public void writeEdges(final Writer writer, final GraphState graphState,
                final Set<Triple<String, String, String>> duplicateRelationships,
                final Set<String> mockVertices) throws IOException {

            for(final SchemaEdgeLabel e: graphState.getGraphSchemaDef().getEdges()) {
                writeEdge(writer, e, graphState, duplicateRelationships, mockVertices);
            }
        }

		public void writeEdge(final Writer writer, SchemaEdgeLabel e, final GraphState graphState,
		        Set<Triple<String, String, String>> duplicateRelationships,
		        final Set<String> mockVerices) throws IOException {

            final boolean isInContext = graphState.getEdge(e.getLabel()) != null;

			for(SchemaRelationshipDesc rel: e.getRelationships()) {

				if (!e.getUnidirected()) {
					final Triple<String, String, String> vertexTriple =
					        Triple.of(rel.getIn(), e.getLabel(), rel.getOut());
					if (duplicateRelationships.contains(vertexTriple)) {
						continue;
					}
					duplicateRelationships.add(vertexTriple);
				}

				// creating the vertices referenced in the relationships but not defined
				try ( StringWriter sw = new StringWriter() ) {
    				Arrays.asList(rel.getIn(), rel.getOut()).stream().sequential().
    				    filter(v -> graphState.getVertex(v) == null).
    				    filter(v -> !mockVerices.contains(v)).
    				    forEach(v -> {
    				        final SchemaVertexLabel mockVertex = new SchemaVertexLabel();
    				        mockVertex.setLabel(v);
                            try {
                                writeNode(sw, mockVertex, graphState,
                                        Collections.emptySet(), Collections.emptySet());
                            } catch (final IOException ex) {
                                throw new RuntimeException("not expected", ex);
                            }
    				        mockVerices.add(v);
    				    });
    				sw.flush();
    				writer.write(sw.toString());
				}

				writer.write('\n');

				writer.write(rel.getOut());
				writer.write(" -- ");
				writer.write(rel.getIn());
				writer.write(" [ label=");
				writeEdgeLabel(writer, e);
				writer.write(", style=");
				if (isInContext) {
				    writer.write("solid");
				} else {
				    writer.write("dashed");
				}
				if (e.getInvisible()) {
				    writer.write(", arrowtail=odot");
				} else {
				    writer.write(", arrowtail=none");
				}
				writer.write(", dir=both");
				if (e.getUnidirected()) {
				    writer.write(", arrowhead=odiamond");
				} else if (e.getInvisible()) {
				    writer.write(", arrowhead=empty");
				}
	            final String color = getColorNameByTags(graphState, rel.getDoctags());
	            if (color != null) {
	                writer.write(", color=");
	                writer.write(color);
	            }
				writer.write(" ]; ");
			}
		}

		private void writeEdgeLabel(final Writer writer, final SchemaEdgeLabel e) throws IOException {
		    writer.write('<');
		    writer.write(e.getLabel());
		    if (e.getMultiplicity() != null && e.getMultiplicity() != Multiplicity.MULTI) {
		        writer.write("<BR/><FONT POINT-SIZE=\"8\">[");
		        writer.write(e.getMultiplicity().toString());
		        writer.write("]</FONT>");
		    }
		    writer.write('>');
		}

		private void writeNodes(final Writer writer, final GraphState graphState,
		        final Set<Triple<String, String, String>> duplicateRelationships,
		        final Set<String> mockVerices) throws IOException {

            for(final SchemaVertexLabel v: graphState.getGraphSchemaDef().getVertices()) {
                writeNode(writer, v, graphState, duplicateRelationships, mockVerices);
            }
		}

		private void writeNode(final Writer writer, final SchemaVertexLabel v, final GraphState graphState,
		        final Set<Triple<String, String, String>> duplicateRelationships,
		        final Set<String> mockVerices) throws IOException {

		    final boolean isInContext = graphState.getVertex(v.getLabel()) != null;

		    // verifying if there are any out-of-context edges referred to in the
		    // relationships
		    try ( StringWriter sw = new StringWriter() ) {
    		    v.getRelationships().stream().
    		        filter(rel -> graphState.getEdge(rel.getEdge()) == null).
    		        forEach(rel -> {
                            final Triple<String, String, String> vertexTriple = rel.getDirection() == Direction.IN
                                    ? Triple.of(rel.getVertex(), rel.getEdge(), v.getLabel())
                                    : Triple.of(v.getLabel(), rel.getEdge(), rel.getVertex());
                            if (duplicateRelationships.contains(vertexTriple)) {
                                return;
                            }

                            final SchemaEdgeLabel mockEdge =
                                    new SchemaEdgeLabel();
                            mockEdge.setLabel(rel.getEdge());

                            final SchemaRelationshipDesc mockRel = new SchemaRelationshipDesc();
                            mockRel.setIn(vertexTriple.getRight());
                            mockRel.setOut(vertexTriple.getLeft());

                            final Set<SchemaRelationshipDesc> mockRelationships =
                                    Collections.singleton(mockRel);
                            mockEdge.setRelationships(mockRelationships);

                            try {
                                writeEdge(sw, mockEdge, graphState, duplicateRelationships, mockVerices);
                            } catch (final IOException e) {
                                throw new RuntimeException("Not expected", e);
                            }

                            duplicateRelationships.add(vertexTriple);
    		        });
                sw.flush();
                writer.write(sw.toString());
		    }

			writer.write('\n');
			writer.write(v.getLabel());
			writer.write(" [ label=<<FONT POINT-SIZE=\"10\"><TABLE CELLSPACING=\"0\" CELLPADDING=\"0\" BORDER=\"0\" CELLBORDER=\"1\"><TR><TD BORDER=\"0\"><FONT POINT-SIZE=\"14\"><B>\\N</B></FONT></TD></TR>");
			for(final SchemaPropertyDesc p: v.getProperties()) {
				writer.write("<TR><TD>");
				writer.write(p.getKey());
				writer.write("</TD></TR>");
			}
			writer.write("</TABLE></FONT>> , style=\"rounded");
			if (!isInContext) {
			    writer.write(",dashed");
			}
			if (v.getStatic()) {
	             writer.write(",filled");
			}
			writer.write('"');
			final String color = getColorNameByTags(graphState, v.getDoctags());
			if (color != null) {
			    writer.write(", color=");
			    writer.write(color);
			}
			if (v.getStatic()) {
			    writer.write(", fillcolor=\"");
			    writer.write(STATIC_V_COLOR);
			    writer.write('"');
			}
			writer.write("];");
		}
	}
}
