package com.newforma.titan.schema;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;

import com.newforma.titan.schema.types.DoctagListType;
import com.newforma.titan.schema.types.SchemaEdgeLabel;
import com.newforma.titan.schema.types.SchemaPropertyDesc;
import com.newforma.titan.schema.types.SchemaRelationshipDesc;
import com.newforma.titan.schema.types.SchemaVertexLabel;

public class DOTGenerator {

	private static final String DOT_FILE_NAME = "graph.dot";

	DOTGenerator() {
		// does nothing
	}

	public void generate(final GraphState graphState, final String targetDir) throws IOException {
		final DOTGraphWriter dotWriter = new DOTGraphWriter(new File(targetDir, DOT_FILE_NAME),
				graphState.getGraphSchemaDef().getGraph().getName());

		for(final SchemaVertexLabel v: graphState.getGraphSchemaDef().getVertices()) {
			dotWriter.writeNode(v, graphState);
		}

		for(final SchemaEdgeLabel e: graphState.getGraphSchemaDef().getEdges()) {
			dotWriter.writeEdge(e, graphState);
		}

		dotWriter.close();
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

		private final Writer writer;

		private DOTGraphWriter(File f, String graphName) throws IOException {
			this.writer = new BufferedWriter(new FileWriter(f));

			writer.write("graph ");
			writer.write("GRAPH_" + graphName.replaceAll("[^a-zA-Z0-9_]", "_"));
			writer.write(" {");
		}

		public void writeEdge(final SchemaEdgeLabel e, final GraphState graphState) throws IOException {

			final Set<Pair<String, String>> duplicateRelationships = new HashSet<>();

			for(SchemaRelationshipDesc rel: e.getRelationships()) {

				if (!e.getUnidirected()) {
					final Pair<String, String> key1 = new Pair<>(rel.getIn(), rel.getOut());
					final Pair<String, String> key2 = new Pair<>(rel.getOut(), rel.getIn());
					if (duplicateRelationships.contains(key1) || duplicateRelationships.contains(key2)) {
						continue;
					}
					duplicateRelationships.add(key1);
				}

				writer.write('\n');

				writer.write(rel.getOut());
				writer.write(" -- ");
				writer.write(rel.getIn());
				writer.write(" [ label=");
				writer.write(e.getLabel());
				writer.write(", style=");
				writer.write(e.getInvisible() ? "dotted" : "solid");
				if (e.getUnidirected()) {
					writer.write(", dir=forward");
				}
	            final String color = getColorNameByTags(graphState, rel.getDoctags());
	            if (color != null) {
	                writer.write(", color=");
	                writer.write(color);
	            }
				writer.write(" ]; ");
			}
		}

		public void writeNode(final SchemaVertexLabel v, final GraphState graphState) throws IOException {
			writer.write('\n');
			writer.write(v.getLabel());
			writer.write(" [ label=<<FONT POINT-SIZE=\"10\"><TABLE CELLSPACING=\"0\" CELLPADDING=\"0\" BORDER=\"0\" CELLBORDER=\"1\"><TR><TD BORDER=\"0\"><FONT POINT-SIZE=\"14\"><B>\\N</B></FONT></TD></TR>");
			for(final SchemaPropertyDesc p: v.getProperties()) {
				writer.write("<TR><TD>");
				writer.write(p.getKey());
				writer.write("</TD></TR>");
			}
			writer.write("</TABLE></FONT>> , style=rounded");
			final String color = getColorNameByTags(graphState, v.getDoctags());
			if (color != null) {
			    writer.write(", color=");
			    writer.write(color);
			}
			writer.write("];");
		}

		private void close() throws IOException {
			writer.write("\n}");
			writer.flush();
			writer.close();
		}
	}
}
