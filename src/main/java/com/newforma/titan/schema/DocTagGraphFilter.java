package com.newforma.titan.schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.newforma.titan.schema.types.DoctagListType;
import com.newforma.titan.schema.types.GraphSchemaDef;
import com.newforma.titan.schema.validator.SchemaValidationException;

public class DocTagGraphFilter {

    private static final Logger LOG = LoggerFactory.getLogger(DocTagGraphFilter.class);

    DocTagGraphFilter() {
        // does nothing
    }

    GraphState filterSchema(GraphState graphState, String tagFilter) throws SchemaValidationException {
        String[] tagSpecs = StringUtils.stripAll(StringUtils.split(tagFilter, ','));
        if (tagSpecs == null || tagSpecs.length == 0) {
            return graphState;
        }

        final GraphSchemaDef graphSchemaDef = graphState.getGraphSchemaDef();

        LOG.info("Filtering graph for documentation with the following filter: {}", tagFilter);

        final Map<String, String> allowedTagsWithColors = new HashMap<>();
        final Set<String> disallowedTags = new HashSet<>();
        for(final String t: tagSpecs) {
            if (t.startsWith("!")) {
                disallowedTags.add(t.substring(1));
            } else {
                if (t.contains(":")) {
                    final String[] tagColorPair = StringUtils.stripAll(StringUtils.split(t, ':'));
                    if (tagColorPair.length != 2) {
                        allowedTagsWithColors.put(tagColorPair[0], "");
                    } else {
                        allowedTagsWithColors.put(tagColorPair[0], tagColorPair[1]);
                    }
                } else {
                    allowedTagsWithColors.put(t, "");
                }
            }
        }

        final GraphSchemaDef filteredSchema =
                filterSchema(graphSchemaDef, allowedTagsWithColors.keySet(), disallowedTags);
        final GraphState newGraphState = new GraphState(filteredSchema);
        newGraphState.setDocColorsByTag(allowedTagsWithColors);

        return newGraphState;
    }

    private GraphSchemaDef filterSchema(GraphSchemaDef graphSchemaDef, Set<String> allowedTags,
            Set<String> disallowedTags) {

        GraphSchemaDef newSchemaDef = new GraphSchemaDef();
        // we do not need to deep-copy the objects here
        newSchemaDef.setDoctagsMeta(graphSchemaDef.getDoctagsMeta());
        newSchemaDef.setGraph(graphSchemaDef.getGraph());
        newSchemaDef.setVertices(graphSchemaDef.getVertices().stream().
                filter(p -> matchesTagFilters(p.getDoctags(), allowedTags, disallowedTags)).
                collect(Collectors.toSet())
        );
        newSchemaDef.setEdges(graphSchemaDef.getEdges().stream().
                filter(p -> matchesTagFilters(p.getDoctags(), allowedTags, disallowedTags)).
                collect(Collectors.toSet())
        );
        newSchemaDef.setProperties(graphSchemaDef.getProperties().stream().
                filter(p -> matchesTagFilters(p.getDoctags(), allowedTags, disallowedTags)).
                collect(Collectors.toSet())
        );
        newSchemaDef.setGraphIndexes(graphSchemaDef.getGraphIndexes().stream().
                filter(p -> matchesTagFilters(p.getDoctags(), allowedTags, disallowedTags)).
                collect(Collectors.toSet())
        );
        newSchemaDef.setLocalEdgeIndexes(graphSchemaDef.getLocalEdgeIndexes().stream().
                filter(p -> matchesTagFilters(p.getDoctags(), allowedTags, disallowedTags)).
                collect(Collectors.toSet())
        );
        newSchemaDef.setLocalPropertyIndexes(graphSchemaDef.getLocalPropertyIndexes().stream().
                filter(p -> matchesTagFilters(p.getDoctags(), allowedTags, disallowedTags)).
                collect(Collectors.toSet())
        );

        return newSchemaDef;
    }

    private boolean matchesTagFilters(final DoctagListType doctagListType,
            final Set<String> allowedTags,
            final Set<String> disallowedTags) {

        if (doctagListType == null) {
            return allowedTags.isEmpty();
        }

        final Set<String> objectTags = doctagListType.getTags();

        if (objectTags.contains(disallowedTags)) {
            return false;
        }

        return !Sets.intersection(objectTags, allowedTags).isEmpty();
    }
}
