package com.newforma.titan.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.janusgraph.core.EdgeLabel;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.RelationTypeIndex;
import org.janusgraph.core.schema.SchemaStatus;
import org.janusgraph.graphdb.database.management.ManagementSystem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.newforma.titan.schema.types.GraphIndexDef;
import com.newforma.titan.schema.types.LocalEdgeIndexDef;
import com.newforma.titan.schema.types.LocalPropertyIndexDef;

public class IndexUtilsTest {


    @Mock
    private JanusGraph graph;

    @Mock
    private GraphState graphState;

    @Mock
    private ManagementSystem mgmt;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(graph.openManagement()).thenReturn(mgmt);
    }


    @Test
    public void getUnavailableIndexes_localEdgeIndex() throws SchemaManagementException {

        final List<SchemaStatus> indexStatuses = Arrays.asList(
                    SchemaStatus.DISABLED,
                    SchemaStatus.INSTALLED,
                    SchemaStatus.REGISTERED,
                    SchemaStatus.ENABLED
                );

        for(int i = 0 ; i < indexStatuses.size(); i++) {
            final LocalEdgeIndexDef leIdx = Mockito.mock(LocalEdgeIndexDef.class);
            when(graphState.getIndexDef("index" + i)).thenReturn(leIdx);
            when(leIdx.getLabel()).thenReturn("edgeLabel" + i);

            final EdgeLabel el = Mockito.mock(EdgeLabel.class);

            when(graph.getEdgeLabel("edgeLabel" + i)).thenReturn(el);

            final RelationTypeIndex rti = Mockito.mock(RelationTypeIndex.class);
            when(mgmt.getRelationIndex(eq(el), eq("index" + i))).thenReturn(rti);
            when(rti.getIndexStatus()).thenReturn(indexStatuses.get(i));
        }

        final Collection<String> unavailableIndexes = IndexUtils
                .getUnavailableIndexes(Arrays.asList("index0", "index1", "index2", "index3"), graphState, graph);

        assertEquals(2, unavailableIndexes.size());
        assertTrue(unavailableIndexes.contains("index1"));
        assertTrue(unavailableIndexes.contains("index2"));
    }

    @Test
    public void getUnavailableIndexes_localPropertyIndex() throws SchemaManagementException {

        final List<SchemaStatus> indexStatuses = Arrays.asList(
                    SchemaStatus.DISABLED,
                    SchemaStatus.INSTALLED,
                    SchemaStatus.REGISTERED,
                    SchemaStatus.ENABLED
                );

        for(int i = 0 ; i < indexStatuses.size(); i++) {
            final LocalPropertyIndexDef lpIdx = Mockito.mock(LocalPropertyIndexDef.class);
            when(graphState.getIndexDef("index" + i)).thenReturn(lpIdx);
            when(lpIdx.getKey()).thenReturn("property" + i);

            final PropertyKey pk = Mockito.mock(PropertyKey.class);

            when(graph.getPropertyKey("property" + i)).thenReturn(pk);

            final RelationTypeIndex rti = Mockito.mock(RelationTypeIndex.class);
            when(mgmt.getRelationIndex(eq(pk), eq("index" + i))).thenReturn(rti);
            when(rti.getIndexStatus()).thenReturn(indexStatuses.get(i));
        }

        final Collection<String> unavailableIndexes = IndexUtils
                .getUnavailableIndexes(Arrays.asList("index0", "index1", "index2", "index3"), graphState, graph);

        assertEquals(2, unavailableIndexes.size());
        assertTrue(unavailableIndexes.contains("index1"));
        assertTrue(unavailableIndexes.contains("index2"));
    }

    @Test
    public void getUnavailableIndexes_graphIndex() throws SchemaManagementException {

        final List<SchemaStatus[]> indexStatuses = Arrays.asList(
                    new SchemaStatus[]{SchemaStatus.DISABLED},          // index0
                    new SchemaStatus[]{SchemaStatus.INSTALLED},         // index1
                    new SchemaStatus[]{SchemaStatus.REGISTERED},        // index2
                    new SchemaStatus[]{SchemaStatus.ENABLED},           // index3
                    new SchemaStatus[]{SchemaStatus.INSTALLED, SchemaStatus.DISABLED},      // index4
                    new SchemaStatus[]{SchemaStatus.REGISTERED, SchemaStatus.DISABLED},     // index5
                    new SchemaStatus[]{SchemaStatus.ENABLED, SchemaStatus.DISABLED},        // index6
                    new SchemaStatus[]{SchemaStatus.ENABLED, SchemaStatus.INSTALLED},       // index7
                    new SchemaStatus[]{SchemaStatus.REGISTERED, SchemaStatus.INSTALLED}      // index8

                );

        final Collection<String> indexNames = new ArrayList<>(indexStatuses.size());
        for(int i = 0 ; i < indexStatuses.size(); i++) {

            indexNames.add("index" + i);

            final GraphIndexDef lpIdx = Mockito.mock(GraphIndexDef.class);
            when(graphState.getIndexDef("index" + i)).thenReturn(lpIdx);

            final JanusGraphIndex jgi = Mockito.mock(JanusGraphIndex.class);
            when(mgmt.getGraphIndex(eq("index" + i))).thenReturn(jgi);

            final SchemaStatus[] pkStatuses = indexStatuses.get(i);
            final PropertyKey[] pkis = new PropertyKey[pkStatuses.length];
            for(int pkIdx = 0; pkIdx < pkis.length; pkIdx++) {
                final String pkName = "property" + i + "_" + pkIdx;
                final PropertyKey pk = Mockito.mock(PropertyKey.class);
                pkis[pkIdx] = pk;
                when(graph.getPropertyKey(eq(pkName))).thenReturn(pk);
                when(jgi.getIndexStatus(eq(pk))).thenReturn(pkStatuses[pkIdx]);
            }

            when(jgi.getFieldKeys()).thenReturn(pkis);
        }

        final Collection<String> unavailableIndexes = IndexUtils
                .getUnavailableIndexes(indexNames, graphState, graph);

        assertEquals(4, unavailableIndexes.size());
        assertTrue(unavailableIndexes.contains("index1"));
        assertTrue(unavailableIndexes.contains("index2"));
        assertTrue(unavailableIndexes.contains("index7"));
        assertTrue(unavailableIndexes.contains("index8"));
    }
}
