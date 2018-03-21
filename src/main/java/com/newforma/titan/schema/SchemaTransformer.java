package com.newforma.titan.schema;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newforma.titan.schema.types.DoctagCascading;
import com.newforma.titan.schema.types.DoctagListType;
import com.newforma.titan.schema.types.GraphSchemaDef;
import com.newforma.titan.schema.types.IDocTaggable;
import com.newforma.titan.schema.types.SchemaEdgeLabel;
import com.newforma.titan.schema.types.SchemaPropertyDef;
import com.newforma.titan.schema.types.SchemaPropertyDesc;
import com.newforma.titan.schema.types.SchemaRelationshipDesc;
import com.newforma.titan.schema.types.SchemaVertexLabel;
import com.newforma.titan.schema.types.SchemaVertexRelationshipDesc;

/**
 * Performs some basic transformations and enhancements of the schema
 * @author Nikolai Grigoriev
 *
 */
public class SchemaTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaTransformer.class);

    public SchemaTransformer() {
        // does nothing
    }

    public GraphSchemaDef cascadeDoctags(final GraphSchemaDef schemaDef) {
        if (schemaDef.getGraph() == null || schemaDef.getGraph().getDocumentation() == null) {
            return schemaDef;
        }
        final DoctagCascading cascadingSettings = schemaDef.getGraph().getDocumentation().getDoctagCascading();
        if (cascadingSettings == null || cascadingSettings == DoctagCascading.NO) {
            return schemaDef;
        }
        schemaDef.getVertices().stream().
            filter(v -> v.getDoctags() != null).
            flatMap(v -> v.getProperties().stream().
                    filter(Objects::nonNull).
                    map(p -> new AbstractMap.SimpleEntry<DoctagListType, IDocTaggable>(v.getDoctags(), p))).
                forEach(t -> cascadeTagList(t, cascadingSettings));
        schemaDef.getVertices().stream().
            filter(v -> v.getDoctags() != null).
            flatMap(v -> v.getRelationships().stream().
                    filter(Objects::nonNull).
                    map(p -> new AbstractMap.SimpleEntry<DoctagListType, IDocTaggable>(v.getDoctags(), p))).
                forEach(t -> cascadeTagList(t, cascadingSettings));
        schemaDef.getEdges().stream().
            filter(e -> e.getDoctags() != null).
            flatMap(e -> e.getProperties().stream().
                    filter(Objects::nonNull).
                    map(p -> new AbstractMap.SimpleEntry<DoctagListType, IDocTaggable>(e.getDoctags(), p))).
                forEach(t -> cascadeTagList(t, cascadingSettings));
        schemaDef.getEdges().stream().
            filter(e -> e.getDoctags() != null).
            flatMap(e -> e.getRelationships().stream().
                    filter(Objects::nonNull).
                    map(p -> new AbstractMap.SimpleEntry<DoctagListType, IDocTaggable>(e.getDoctags(), p))).
                forEach(t -> cascadeTagList(t, cascadingSettings));

        return schemaDef;
    }



    private Object cascadeTagList(SimpleEntry<DoctagListType, IDocTaggable> entryPair, DoctagCascading cascadingSettings) {
        final DoctagListType parentDocTags = entryPair.getKey();
        final IDocTaggable childElement = entryPair.getValue();

        if (childElement.getDoctags() == null) {
            childElement.setDoctags(new DoctagListType(new HashSet<>(0)));
        }

        final DoctagListType childDocTags = childElement.getDoctags();

        switch(cascadingSettings) {
        case REPLACE_IF_EMPTY:
            if (!childDocTags.getTags().isEmpty()) {
                break;
            }
        case REPLACE:
            childDocTags.setTags(parentDocTags.getTags());
            break;
        case APPEND_IF_EMPTY:
            if (!childDocTags.getTags().isEmpty()) {
                break;
            }
        case APPEND:
            Set<String> childTags = new LinkedHashSet<>(childDocTags.getTags());
            childTags.addAll(parentDocTags.getTags());
            childDocTags.setTags(childTags);
            break;
        default:
            // not expected to happen
            throw new RuntimeException("unsupported cascading setting");
        }

        return entryPair;
    }

    public GraphSchemaDef transformForDocumentation(final GraphState graphState) {
        // not cloning the schema because we do not do any structural changes here
        final GraphSchemaDef schemaDef = graphState.getGraphSchemaDef();

        // relationships may be defined in context of both vertexes and edges. We need to merge
        // both lists for both sets

        // we need to avoid copying the same relationships back
        Map<String, Collection<SchemaRelationshipDesc>> extraEdgeRelationships = new HashMap<>();

        for(final SchemaVertexLabel v: schemaDef.getVertices()) {
            for(final SchemaVertexRelationshipDesc rel: v.getRelationships()) {
                final SchemaEdgeLabel targetEdge = graphState.getEdge(rel.getEdge());
                Collection<SchemaRelationshipDesc> extraRelsForEdge = extraEdgeRelationships.get(rel.getEdge());
                if (extraRelsForEdge == null) {
                    extraRelsForEdge = new LinkedList<>();
                    extraEdgeRelationships.put(rel.getEdge(), extraRelsForEdge);
                }
                if (targetEdge == null) {
                    LOG.warn("Relationship description for vertex \"{}\" refers to non-existing edge label \"{}\", ignoring",
                            v.getLabel(), rel.getEdge());
                } else {
                    final SchemaRelationshipDesc desc = new SchemaRelationshipDesc();
                    desc.setDescription(rel.getDescription());
                    desc.setIn(rel.getDirection() == Direction.IN ? v.getLabel() : rel.getVertex());
                    desc.setOut(rel.getDirection() == Direction.IN ? rel.getVertex() : v.getLabel());
                    desc.setDoctags(rel.getDoctags());
                    extraRelsForEdge.add(desc);
                }
            }
        }

        for(final SchemaEdgeLabel e: schemaDef.getEdges()) {
            for(final SchemaRelationshipDesc rel: e.getRelationships()) {
                final SchemaVertexLabel outVertex = graphState.getVertex(rel.getOut());
                if (outVertex == null) {
                    LOG.warn("Relationship description for edge \"{}\" refers to non-existing outgoing vertex label \"{}\", ignoring",
                            e.getLabel(), rel.getOut());
                } else {
                    final SchemaVertexRelationshipDesc desc = new SchemaVertexRelationshipDesc();
                    desc.setDescription(rel.getDescription());
                    desc.setEdge(e.getLabel());
                    desc.setDirection(Direction.OUT);
                    desc.setVertex(rel.getIn());
                    desc.setDoctags(rel.getDoctags());
                    outVertex.getRelationships().add(desc);
                }

                final SchemaVertexLabel inVertex = graphState.getVertex(rel.getIn());
                if (inVertex == null) {
                    LOG.warn("Relationship description for edge \"{}\" refers to non-existing incoming vertex label \"{}\", ignoring",
                            e.getLabel(), rel.getIn());
                } else {
                    final SchemaVertexRelationshipDesc desc = new SchemaVertexRelationshipDesc();
                    desc.setDescription(rel.getDescription());
                    desc.setEdge(e.getLabel());
                    desc.setDirection(Direction.IN);
                    desc.setVertex(rel.getOut());
                    desc.setDoctags(rel.getDoctags());
                    inVertex.getRelationships().add(desc);
                }
            }
        }

        // now modifying the edges
        for(final Map.Entry<String, Collection<SchemaRelationshipDesc>> relEntry: extraEdgeRelationships.entrySet()) {
            SchemaEdgeLabel edge = graphState.getEdge(relEntry.getKey());
            if (edge != null) {
                edge.getRelationships().addAll(relEntry.getValue());
            }
        }

        // adding inherited documentation
        for(final SchemaVertexLabel v: schemaDef.getVertices()) {
            for(final SchemaPropertyDesc p: v.getProperties()) {
                if (StringUtils.isEmpty(p.getDescription())) {
                    final SchemaPropertyDef pDef = graphState.getProperty(p.getKey());
                    if (pDef != null) {
                        p.setDescription("(copied) " + pDef.getDescription());
                    }
                }
            }
        }

        for(final SchemaEdgeLabel e: schemaDef.getEdges()) {
            for(final SchemaPropertyDesc p: e.getProperties()) {
                if (StringUtils.isEmpty(p.getDescription())) {
                    final SchemaPropertyDef pDef = graphState.getProperty(p.getKey());
                    if (pDef != null) {
                        p.setDescription("(copied) " + pDef.getDescription());
                    }
                }
            }
            for(final SchemaRelationshipDesc rel: e.getRelationships()) {
                if (StringUtils.isEmpty(rel.getDescription())) {
                    rel.setDescription("(copied) " + e.getDescription());
                }
            }
        }


        return schemaDef;
    }}
