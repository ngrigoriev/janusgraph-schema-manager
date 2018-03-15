package com.newforma.titan.schema;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.janusgraph.core.EdgeLabel;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.RelationTypeIndex;
import org.janusgraph.core.schema.SchemaStatus;

import com.newforma.titan.schema.types.GraphIndexDef;
import com.newforma.titan.schema.types.LocalEdgeIndexDef;
import com.newforma.titan.schema.types.LocalPropertyIndexDef;

public class IndexUtils {

    /**
     * Returns the collection of the graph indexes that are in either
     * {@link SchemaStatus#INSTALLED} or {@link SchemaStatus#REGISTERED} status.
     * Note that if <b>any</b> property key is in above-mentioned state but
     * <b>no</b> property key is in {@link SchemaStatus#DISABLED} state, then
     * the index is returned.
     *
     * @param candidates
     *            list of indexes to consider
     * @param graph
     *            open graph instance
     * @return list of indexes that are not currently available but not disabled
     *
     * @throws SchemaManagementException
     *             if an index discovery operation fails
     */
    public static Collection<String> getUnavailableIndexes(final Collection<String> candidates, final GraphState graphState,
            final JanusGraph graph) throws SchemaManagementException {

        final JanusGraphManagement mgmt = graph.openManagement();

        final Set<String> unavailableIndexes = new HashSet<>();

        try {
            candidates.stream().filter(n -> graphState.getIndexDef(n) instanceof LocalPropertyIndexDef).filter(n -> {
                final LocalPropertyIndexDef def = (LocalPropertyIndexDef) graphState.getIndexDef(n);
                final PropertyKey pk = graph.getPropertyKey(def.getKey());
                if (pk == null) {
                    return false;
                }
                final RelationTypeIndex idx = mgmt.getRelationIndex(pk, n);
                return idx.getIndexStatus() == SchemaStatus.INSTALLED
                        || idx.getIndexStatus() == SchemaStatus.REGISTERED;
            }).forEach(unavailableIndexes::add);

            candidates.stream().filter(n -> graphState.getIndexDef(n) instanceof LocalEdgeIndexDef).filter(n -> {
                final LocalEdgeIndexDef def = (LocalEdgeIndexDef) graphState.getIndexDef(n);
                final EdgeLabel edge = graph.getEdgeLabel(def.getLabel());
                if (edge == null) {
                    return false;
                }
                final RelationTypeIndex idx = mgmt.getRelationIndex(edge, n);
                return idx.getIndexStatus() == SchemaStatus.INSTALLED
                        || idx.getIndexStatus() == SchemaStatus.REGISTERED;
            }).forEach(unavailableIndexes::add);

            candidates.stream().filter(n -> graphState.getIndexDef(n) instanceof GraphIndexDef).filter(n -> {
                final JanusGraphIndex idx = mgmt.getGraphIndex(n);

                final int numUnavailableKeys = Arrays.stream(idx.getFieldKeys()).mapToInt(k -> {
                    final SchemaStatus status = idx.getIndexStatus(k);
                    if (status == SchemaStatus.DISABLED) {
                        return -idx.getFieldKeys().length;
                    }
                    return status == SchemaStatus.INSTALLED || status == SchemaStatus.REGISTERED ? 1 : 0;
                }).sum();

                return numUnavailableKeys > 0;
            }).forEach(unavailableIndexes::add);
        } catch (final Exception e) {
            throw new SchemaManagementException("Unable to verify the indexes", e);
        } finally {
            mgmt.rollback();
        }

        return unavailableIndexes;

    }
}
