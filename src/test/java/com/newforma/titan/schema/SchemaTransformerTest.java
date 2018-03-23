package com.newforma.titan.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import com.newforma.titan.schema.types.GraphSchemaDef;
import com.newforma.titan.schema.types.SchemaEdgeLabel;
import com.newforma.titan.schema.types.SchemaPropertyDesc;
import com.newforma.titan.schema.types.SchemaRelationshipDesc;
import com.newforma.titan.schema.types.SchemaVertexLabel;
import com.newforma.titan.schema.types.SchemaVertexRelationshipDesc;

public class SchemaTransformerTest {

    final SchemaLoader loader = SchemaLoader.getInstance();

    @Test
    public void testCascade_REPLACE_IF_EMPTY_empty() throws Exception {
        final GraphSchemaDef schema;
        try (InputStream is = getClass().getResourceAsStream("doctag_cascading_test_001.json")) {
            schema = loader.loadFrom(is, "junit.json",
                    new File(System.getProperty("java.io.tmpdir")));
        }

        final Map<String, SchemaEdgeLabel> edgeMap = schema.getEdges().stream().collect(Collectors.toMap(SchemaEdgeLabel::getLabel, Function.identity()));
        assertEquals(2, edgeMap.size());

        final Map<String, SchemaVertexLabel> vertexMap = schema.getVertices().stream().collect(Collectors.toMap(SchemaVertexLabel::getLabel, Function.identity()));
        assertEquals(2, vertexMap.size());

        // verifying cascading to the vertex properties

        final Set<SchemaPropertyDesc> v1props = vertexMap.get("v1").getProperties();
        assertEquals(1, v1props.size());
        final SchemaPropertyDesc v1p1 = v1props.iterator().next();
        assertEquals(new HashSet<>(Arrays.asList("vtag1", "vtag2")), v1p1.getDoctags().getTags());

        final Set<SchemaPropertyDesc> v2props = vertexMap.get("v2").getProperties();
        assertEquals(1, v2props.size());
        final SchemaPropertyDesc v2p2 = v2props.iterator().next();
        assertEquals(new HashSet<>(Arrays.asList("vtag3", "vtag4")), v2p2.getDoctags().getTags());

        // verifying cascading to the edge properties

        final Set<SchemaPropertyDesc> e1props = edgeMap.get("e1").getProperties();
        assertEquals(1, e1props.size());
        final SchemaPropertyDesc e1p1 = e1props.iterator().next();
        assertEquals(new HashSet<>(Arrays.asList("etag1", "etag2")), e1p1.getDoctags().getTags());

        final Set<SchemaPropertyDesc> e2props = edgeMap.get("e2").getProperties();
        assertEquals(1, e2props.size());
        final SchemaPropertyDesc e2p2 = e2props.iterator().next();
        assertEquals(new HashSet<>(Arrays.asList("etag3", "etag4")), e2p2.getDoctags().getTags());

        // verifying the cascading to the relationships

        final Set<SchemaVertexRelationshipDesc> v1rels = vertexMap.get("v1").getRelationships();
        assertEquals(1, v1rels.size());
        final SchemaVertexRelationshipDesc v1r1 = v1rels.iterator().next();
        assertEquals(new HashSet<>(Arrays.asList("vtag1", "vtag2")), v1r1.getDoctags().getTags());

        final Set<SchemaVertexRelationshipDesc> v2rels = vertexMap.get("v2").getRelationships();
        assertEquals(1, v2rels.size());
        final SchemaVertexRelationshipDesc v2r1 = v2rels.iterator().next();
        assertEquals(new HashSet<>(Arrays.asList("vtag3", "vtag4")), v2r1.getDoctags().getTags());

        final Set<SchemaRelationshipDesc> e1rels = edgeMap.get("e1").getRelationships();
        assertEquals(1, e1rels.size());
        final SchemaRelationshipDesc e1r1 = e1rels.iterator().next();
        assertEquals(new HashSet<>(Arrays.asList("etag1", "etag2")), e1r1.getDoctags().getTags());

        final Set<SchemaRelationshipDesc> e2rels = edgeMap.get("e2").getRelationships();
        assertEquals(1, e2rels.size());
        final SchemaRelationshipDesc e2r1 = e2rels.iterator().next();
        assertEquals(new HashSet<>(Arrays.asList("etag3", "etag4")), e2r1.getDoctags().getTags());
    }

    @Test
    public void testCascade_REPLACE_IF_EMPTY_no_parent_doctags() throws Exception {
        final GraphSchemaDef schema;
        try (InputStream is = getClass().getResourceAsStream("doctag_cascading_empty_parent_doctags.json")) {
            schema = loader.loadFrom(is, "junit.json",
                    new File(System.getProperty("java.io.tmpdir")));
        }

        schema.getVertices().stream().forEach(v -> v.getRelationships().stream().forEach(
                vp -> assertTrue(vp.getDoctags() == null || vp.getDoctags().getTags().isEmpty())));
        schema.getVertices().stream().forEach(v -> v.getProperties().stream().forEach(
                vr -> assertTrue(vr.getDoctags() == null || vr.getDoctags().getTags().isEmpty())));

        schema.getEdges().stream().forEach(e -> e.getRelationships().stream().forEach(
                ep -> assertTrue(ep.getDoctags() == null || ep.getDoctags().getTags().isEmpty())));
        schema.getEdges().stream().forEach(e -> e.getProperties().stream().forEach(
                er -> assertTrue(er.getDoctags() == null || er.getDoctags().getTags().isEmpty())));
    }
}
