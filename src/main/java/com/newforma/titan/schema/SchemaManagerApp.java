package com.newforma.titan.schema;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newforma.titan.schema.actions.ReindexAction;
import com.newforma.titan.schema.actions.ReindexAction.IndexTarget;

public class SchemaManagerApp {

    private static final String OPTION_GENERATE_DOCS = "d";
    private static final String OPTION_REINDEX_SPECIFIC = "i";
    private static final String OPTION_REINDEX_DATA = "r";
    private static final String OPTION_WRITE_TO_DB = "w";
    private static final String OPTION_LOAD_GRAPHML = "l";
    private static final String OPTION_SAVE_GRAPHML = "s";
    private static final String OPTION_FILTER_TAGS = "t";

    private static final Logger LOG = LoggerFactory.getLogger(SchemaManagerApp.class);

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
        if (remainingArgs.length != 1) {
            System.out.println("Missing schema file name");
            printHelp(options);
            System.exit(1);
            return;
        }

        final boolean doApplyChanges = cmdLine.hasOption(OPTION_WRITE_TO_DB);
        final String graphConfigFile = cmdLine.getOptionValue("g");
        final List<ReindexAction> reindexActions = new LinkedList<>();
        if (cmdLine.hasOption('r')) {
            reindexActions.add(new ReindexAction(IndexTarget.valueOf(cmdLine.getOptionValue('r'))));
        }
        if (cmdLine.hasOption('i')) {
            reindexActions.add(new ReindexAction(IndexTarget.NAMED, cmdLine.getOptionValue('i')));
        }
        final String docDir = cmdLine.getOptionValue(OPTION_GENERATE_DOCS);
        final String graphMLToLoad = cmdLine.getOptionValue(OPTION_LOAD_GRAPHML);
        final String graphMLToSave = cmdLine.getOptionValue(OPTION_SAVE_GRAPHML);
        final String tagFilter = cmdLine.getOptionValue(OPTION_FILTER_TAGS);

        try {
            new SchemaManager(remainingArgs[0], graphConfigFile)
                    .andApplyChanges(doApplyChanges)
                    .andReindex(reindexActions)
                    .applyTagFilter(tagFilter).andGenerateDocumentation(docDir)
                    .andLoadData(graphMLToLoad)
                    ./*andSaveData(graphMLToSave).*/run();
        } catch (Throwable t) {
            LOG.error("ERROR", t);
            System.exit(1);
        }

        System.exit(0);
    }

    private static Options populateOptions() {
        final Options options = new Options();
        options.addOption(OPTION_WRITE_TO_DB, false, "Write the relations defined by the schema to the graph");
        options.addOption(OPTION_REINDEX_DATA, true, "Reindex data: ALL, NEW");
        options.addOption(OPTION_REINDEX_SPECIFIC, true, "Reindex the specific index after applying the schema");
        options.addOption(OPTION_GENERATE_DOCS, true, "Generate documentation, write to the specified directory");
        options.addOption(OPTION_LOAD_GRAPHML, true, "Load specific GraphML file into the database");
        options.addOption(OPTION_SAVE_GRAPHML, true, "Save specific GraphML file into a file");
        options.addOption(OPTION_FILTER_TAGS, true, "Apply tag filter for generated documentation. "
                + "Filter format: tag-spec[,tag-spec[,...]]. tag-spec ::= [!]tag-name[:tag-color]. "
                + "Colors are used for DOT diagram. If the filter is specified, then only the elements having the "
                + "specified tags will be included in the documentation and the elements having the tags prefixed with "
                + "\"!\" will be excluded.");
        options.addRequiredOption("g", "graph-config", true, "Graph property file name");
        return options;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("SchemaManager options schema-file", options);
    }
}
