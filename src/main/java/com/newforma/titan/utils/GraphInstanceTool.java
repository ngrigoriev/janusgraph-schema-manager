package com.newforma.titan.utils;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.diskstorage.Backend;
import org.janusgraph.diskstorage.log.Message;
import org.janusgraph.diskstorage.log.MessageReader;
import org.janusgraph.diskstorage.log.ReadMarker;
import org.janusgraph.diskstorage.log.kcvs.KCVSLog;
import org.janusgraph.diskstorage.util.time.TimestampProvider;
import org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration;
import org.janusgraph.graphdb.database.StandardJanusGraph;
import org.janusgraph.graphdb.database.management.ManagementLogger;
import org.janusgraph.graphdb.database.management.ManagementSystem;
import org.janusgraph.graphdb.types.vertices.JanusGraphSchemaVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newforma.titan.schema.GlobalMetaDataManager;

public class GraphInstanceTool {

    private static final long DEFAULT_PING_TIMEOUT_S = TimeUnit.SECONDS.toMillis(60);

    public enum InstanceState { LIVE, DEAD, FORCED_TO_CLOSE };

    private static final String OPTION_LIST_INSTANCES = "l";
    private static final String OPTION_LIST_INSTANCES_LONG = "list-instances";
    private static final String OPTION_KILL_INSTANCES = "k";
    private static final String OPTION_KILL_INSTANCES_LONG = "kill-unresponsive";
    private static final String OPTION_MAX_WAIT_TIME = "t";
    private static final String OPTION_MAX_WAIT_TIME_LONG = "timeout";
    private static final String OPTION_GRAPH_CONFIG_FILE = "g";
    private static final String OPTION_GRAPH_CONFIG_FILE_LONG = "graph-config";

    private static final Logger LOG = LoggerFactory.getLogger(GraphInstanceTool.class);

    public static void main(String[] args) throws Exception {
        final Options options = populateOptions();
        final CommandLine cmdLine;
        try {
            cmdLine = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.out.println("Invalid command: " + e.getMessage());
            printHelp(options);
            System.exit(1);
            return;
        }

        final String graphConfigFileName = cmdLine.getOptionValue(OPTION_GRAPH_CONFIG_FILE);
        final long lagThresholdMs;
        if (cmdLine.hasOption(OPTION_MAX_WAIT_TIME)) {
            lagThresholdMs = TimeUnit.SECONDS.toMillis(Long.parseLong(cmdLine.getOptionValue(OPTION_MAX_WAIT_TIME)));
        } else {
            lagThresholdMs = DEFAULT_PING_TIMEOUT_S;
        }

        final Collection<InstanceInfo> result;

        try (final StandardJanusGraph graph = (StandardJanusGraph) JanusGraphFactory.open(graphConfigFileName)) {
            result = new GraphInstanceTool().verifyInstances(graph, lagThresholdMs, cmdLine.hasOption(OPTION_KILL_INSTANCES));
        }

        if (cmdLine.hasOption(OPTION_LIST_INSTANCES)) {
            System.out.println("Instance summary (id, state):");
            for(final InstanceInfo ii: result) {
                System.out.println("+ " + ii.getId() + "\t\t" + ii.getState());
            }
        }

        System.exit(0);
    }

    public Collection<InstanceInfo> verifyInstances(final StandardJanusGraph graph, long maxWaitTimeMs,
            boolean closeLagging) throws Exception {
        final ManagementSystem mgmt = (ManagementSystem) graph.openManagement();
        final Set<String> openInstanceIds = new HashSet<>(mgmt.getOpenInstancesInternal());
        mgmt.rollback();

        final String myInstanceId = graph.getConfiguration().getUniqueGraphId();
        LOG.info("My graph instance ID is {}", myInstanceId);
        // not including myself in this exercise
        openInstanceIds.remove(myInstanceId);

        final Collection<InstanceInfo> result = new ArrayList<>(openInstanceIds.size());

        final Backend backend = graph.getBackend();

        final KCVSLog mgmtLog = (KCVSLog) backend.getSystemMgmtLog();

        final GraphDatabaseConfiguration config = graph.getConfiguration();
        final TimestampProvider times = config.getTimestampProvider();

        final ManagementLogger mgmtLogger = new ManagementLogger(graph, mgmtLog, graph.getSchemaCache(), times);
        final Object lock = new Object();
        final EchoMessageReader echoMessageReader = new EchoMessageReader(openInstanceIds, lock);
        mgmtLog.registerReader(ReadMarker.fromNow(), echoMessageReader);

        JanusGraphSchemaVertex metadataVertex = (JanusGraphSchemaVertex) graph.getVertexLabel(GlobalMetaDataManager.METADATA_VERTEX_LABEL);
        if (metadataVertex == null) {
            throw new IOException("Unable to find the vertex label " +
                    GlobalMetaDataManager.METADATA_VERTEX_LABEL +
                    ", probably the schema is not loaded?");
        }

        try {
            synchronized (lock) {
                mgmtLogger.sendCacheEviction(
                        Collections.singleton(metadataVertex),
                        Arrays.asList(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                return Boolean.TRUE;
                            }

                        }), openInstanceIds);
                LOG.debug("Cache eviction sent to {} instance(s), waiting for {} ms...",
                        openInstanceIds.size(), maxWaitTimeMs);
                lock.wait(maxWaitTimeMs);
            }
        } catch (final InterruptedException ie) {
            throw new IOError(ie);
        }

        if (!echoMessageReader.isAllResponded()) {
            for(String deadInstance: echoMessageReader.getRemainingInstances()) {
                LOG.info("Detected non-responsive instance {}", deadInstance);
                if (closeLagging) {
                    final JanusGraphManagement killMgmt = graph.openManagement();
                    try {
                        LOG.warn("Forcing instance {} to close", deadInstance);
                        killMgmt.forceCloseInstance(deadInstance);
                    } finally {
                        killMgmt.commit();
                    }
                    result.add(new InstanceInfo(deadInstance, InstanceState.FORCED_TO_CLOSE));
                } else {
                    result.add(new InstanceInfo(deadInstance, InstanceState.DEAD));
                }
            }
        }

        openInstanceIds.stream().filter(i -> !echoMessageReader.getRemainingInstances().contains(i))
                .map(i -> new InstanceInfo(i, InstanceState.LIVE)).forEach(result::add);

        return result;
    }

    private static Options populateOptions() {
        final Options options = new Options();
        options.addRequiredOption(OPTION_GRAPH_CONFIG_FILE, OPTION_GRAPH_CONFIG_FILE_LONG,
                true, "Graph configuration file");
        options.addOption(OPTION_LIST_INSTANCES, OPTION_LIST_INSTANCES_LONG, false,
                "List currently registered instances along with their observed state");
        options.addOption(OPTION_KILL_INSTANCES, OPTION_KILL_INSTANCES_LONG, false,
                "Force closing of the instances that do not respond to the messages within given time (see -t option)");
        options.addOption(OPTION_MAX_WAIT_TIME, OPTION_MAX_WAIT_TIME_LONG, true,
                "Maximum time to wait for the instance response, in SECONDS. "
                + "Instances that do not respond within this interval are considered dead. Default: " +
                DEFAULT_PING_TIMEOUT_S);
        return options;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("GraphInstanceTool options", options);
    }

    public static class InstanceInfo {
        private final String id;
        private final InstanceState state;

        public InstanceInfo(final String id, final InstanceState state) {
            this.id = id;
            this.state = state;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            InstanceInfo other = (InstanceInfo) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            return true;
        }

        public String getId() {
            return id;
        }

        public InstanceState getState() {
            return state;
        }
    }

    private class EchoMessageReader implements MessageReader {

        final Object lock;
        final Set<String> openInstances;
        final Set<String> remainingInstances;

        private EchoMessageReader(Set<String> instances, Object lock) {
            this.lock = lock;
            this.openInstances = Collections.unmodifiableSet(instances);
            this.remainingInstances = new HashSet<>(instances);
        }

        @Override
        public void read(Message message) {
            LOG.info("Received message: " + message);
            final String senderId = message.getSenderId();
            if (!remainingInstances.remove(senderId)) {
                LOG.warn("Response from an unknown instance <{}>", senderId);
            }
            synchronized (lock) {
                if (remainingInstances.isEmpty()) {
                    LOG.info("Received responsed from all {} instance(s)", openInstances.size());
                    lock.notify();
                }
            }
        }

        public void updateState() {
            LOG.debug("updateState() called");
        }

        private Set<String> getRemainingInstances() {
            return new HashSet<>(remainingInstances);
        }

        private boolean isAllResponded() {
            return remainingInstances.isEmpty();
        }
    }
}
