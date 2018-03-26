package com.newforma.titan.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.tinkerpop.gremlin.groovy.engine.GremlinExecutor;
import org.apache.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngineFactory;
import org.apache.tinkerpop.gremlin.jsr223.ConcurrentBindings;
import org.apache.tinkerpop.gremlin.jsr223.DefaultGremlinScriptEngineManager;
import org.apache.tinkerpop.gremlin.jsr223.GremlinScriptEngine;
import org.apache.tinkerpop.gremlin.jsr223.GremlinScriptEngineManager;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import groovy.json.JsonBuilder;

import com.google.common.collect.ImmutableMap;

public class ScriptRunnerApp {
    private static final String OPTION_GRAPH_CONFIG = "g";

    private static final Logger LOG = LoggerFactory.getLogger(ScriptRunnerApp.class);

    public static void main(String[] args) {
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

        final String[] remainingArgs = cmdLine.getArgs();
        if (remainingArgs.length == 0) {
            System.out.println("At least one Groovy script must be specified");
            printHelp(options);
            System.exit(1);
            return;
        }

        final String graphConfigFile = cmdLine.getOptionValue(OPTION_GRAPH_CONFIG);

        try {
            runScripts(graphConfigFile, remainingArgs);
        } catch (final Throwable t) {
            LOG.error("Execution failed", t);
            System.exit(1);
        }

        System.exit(0);
    }

    private static void runScripts(String graphConfigFileName, String[] scriptFileNames) throws IOException {

        final GremlinGroovyScriptEngineFactory factory = new GremlinGroovyScriptEngineFactory();
        final GremlinScriptEngineManager scriptEngineManager = new DefaultGremlinScriptEngineManager();
        factory.setCustomizerManager(scriptEngineManager);

        final GremlinScriptEngine groovyEngine = factory.getScriptEngine();

        LOG.debug("Instantiated Groovy engine {}", groovyEngine);

        final PropertiesConfiguration graphConfig = new PropertiesConfiguration();
        LOG.info("Connecting to the graph using {}", graphConfigFileName);
        try {
            graphConfig.load(new File(graphConfigFileName));
        } catch (ConfigurationException e) {
            throw new IOException("Failed to load graph configuration from " + graphConfigFileName, e);
        }

        try (final JanusGraph graph = JanusGraphFactory.open(graphConfig)) {

            Bindings globalBindings = new ConcurrentBindings(ImmutableMap.<String, Object>builder().
                    put("graph", graph).
                    put("g", graph.traversal()).build());

            final GremlinExecutor.Builder gremlinExecutorBuilder = GremlinExecutor.build()
                    .afterFailure((b, e) -> { ((JanusGraph) b.get("graph")).tx().rollback(); })
                    .beforeEval(b -> { ((JanusGraph) b.get("graph")).tx().rollback(); })
                    .afterTimeout(b -> { ((JanusGraph) b.get("graph")).tx().rollback(); })
                    .globalBindings(globalBindings);
            final GremlinExecutor scriptExecutor = gremlinExecutorBuilder.create();

            for (final String scriptFile : scriptFileNames) {
                LOG.info("Executing script {}", scriptFile);
                try (final Reader scriptReader = new FileReader(scriptFile)) {

                    Optional<CompiledScript> compiledScript = scriptExecutor.compile(FileUtils.readFileToString(new File(scriptFile)));
                    if (compiledScript.isPresent()) {
                        final Object result = compiledScript.get().eval();
                        LOG.debug("Script raw result \"{}\"", result);
                        final String jsonResult;
                        if (result != null) {
                            jsonResult = new JsonBuilder(result).toPrettyString();
                        } else {
                            jsonResult = "NULL";
                        }

                        LOG.info("Script {} returned \"{}\"", scriptFile, jsonResult);
                        graph.tx().commit();
                    } else {
                        throw new IOException("Failed to compile script " + scriptFile);
                    }
                } catch (final ScriptException se) {
                    throw new IOException("Failed to execute script " + scriptFile, se);
                } catch (final Throwable t) {
                    throw new IOException("Internal error when running " + scriptFile, t);
                }
            }
        }

        LOG.info("Executed {} script(s)", scriptFileNames.length);
    }

    private static Options populateOptions() {
        final Options options = new Options();
        options.addOption(OPTION_GRAPH_CONFIG, true, "Graph configuration property file");
        return options;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ScriptRunnerApp options groovy-script [groovy-script...]", options);
    }
}
