package openllet;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.exceptions.InconsistentOntologyException;
import openllet.core.output.TableData;
import openllet.core.utils.Timer;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.tcq.engine.TCQEngine;
import openllet.tcq.model.kb.TemporalKnowledgeBase;
import openllet.tcq.model.kb.FileBasedTemporalKnowledgeBaseImpl;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.tcq.parser.ParseException;
import openllet.tcq.parser.TemporalConjunctiveQueryParser;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.query.*;
import org.apache.jena.shared.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static openllet.OpenlletCmdOptionArg.*;

public class OpenlletTemporalQuery extends OpenlletCmdApp
{
    private String queryFile;
    private String catalogFile;
    private QueryResult queryResults;
    private String queryString;
    private TemporalConjunctiveQuery query;
    private TemporalKnowledgeBase kb;
    private final QueryExec<TemporalConjunctiveQuery> queryEngine = new TCQEngine();
    private OutputFormat outputFormat = OutputFormat.TABULAR;

    private enum OutputFormat
    {
        TABULAR, XML, JSON
    }

    @Override
    public String getAppId()
    {
        return "OpenlletTemporalQuery: Temporal Conjunctive Query Engine";
    }

    @Override
    public String getAppCmd()
    {
        return "openllet temporal-query " + getMandatoryOptions() + "[options] <file URI>...";
    }

    @Override
    public OpenlletCmdOptions getOptions()
    {
        final OpenlletCmdOptions options = getGlobalOptions();

        OpenlletCmdOption option = new OpenlletCmdOption("query-file");
        option.setShortOption("q");
        option.setType("<file URI>");
        option.setDescription("Read the temporal conjunctive query from the given file");
        option.setIsMandatory(true);
        option.setArg(REQUIRED);
        options.add(option);

        option = new OpenlletCmdOption("output-format");
        option.setShortOption("o");
        option.setType("Tabular | XML | JSON");
        option.setDescription("Format of result set (SELECT queries)");
        option.setDefaultValue("Tabular");
        option.setIsMandatory(false);
        option.setArg(REQUIRED);
        options.add(option);

        option = new OpenlletCmdOption("catalog");
        option.setShortOption("c");
        option.setType("<file URI>");
        option.setDescription("An OASIS XML catalog file to resolve URIs.");
        option.setArg(REQUIRED);
        option.setIsMandatory(false);
        options.add(option);

        option = new OpenlletCmdOption("bnode");
        option.setDescription("Treat bnodes in the query as undistinguished variables. Undistinguished " +
                "variables can match individuals whose existence is inferred by the " +
                "reasoner, e.g. due to a someValuesFrom restriction.");
        option.setDefaultValue(false);
        option.setIsMandatory(false);
        option.setArg(NONE);
        options.add(option);

        options.add(getIgnoreImportsOption());
        options.add(getInputFormatOption());

        return options;
    }

    @Override
    public void parseArgs(final String[] args)
    {
        super.parseArgs(args);

        setQueryFile(_options.getOption("query-file").getValueAsString());
        setCatalogFile(_options.getOption("catalog").getValueAsString());
        setOutputFormat(_options.getOption("output-format").getValueAsString());
        OpenlletOptions.TREAT_ALL_VARS_DISTINGUISHED = !_options.getOption("bnode").getValueAsBoolean();
    }

    private void setCatalogFile(final String s)
    {
        catalogFile = s;
    }

    private List<String> parseInputFilesFromFile(String inputFile)
    {
        if (new File(inputFile).exists())
            try
            {
                List<String> inputFiles = new ArrayList<>();
                for (String line : IO.readWholeFileAsUTF8(inputFile).lines().toList())
                    if (!line.startsWith("#"))
                        inputFiles.add(line);
                if (!isListOfInputFiles(inputFiles))
                    // returns the original input file if unsuccessful
                    inputFiles = List.of(inputFile);
                return inputFiles;
            }
            catch (final NotFoundException | QueryParseException e)
            {
                throw new OpenlletCmdException(e);
            }
        else
            throw new OpenlletCmdException("File " + inputFile + " does not exist");
    }

    private boolean isListOfInputFiles(List<String> inputFiles)
    {
        return new File(inputFiles.get(0)).exists();
    }

    @Override
    public void run()
    {
        loadInput();
        loadQuery();
        execQuery();
        printQueryResults();
    }

    public void setQueryFile(final String s)
    {
        queryFile = s;
    }

    public void setOutputFormat(final String s)
    {
        if (s == null)
            outputFormat = OutputFormat.TABULAR;
        else
        if (s.equalsIgnoreCase("Tabular"))
            outputFormat = OutputFormat.TABULAR;
        else
        if (s.equalsIgnoreCase("XML"))
            outputFormat = OutputFormat.XML;
        else
        if (s.equalsIgnoreCase("JSON"))
            outputFormat = OutputFormat.JSON;
        else
            throw new OpenlletCmdException("Invalid output format: " + outputFormat);
    }

    private void loadInput()
    {
        Timer timer = _timers.createTimer("loading ontologies");
        try
        {
            List<String> inputFiles = Arrays.asList(getInputFiles());
            if (inputFiles.size() == 1)
                // tries to parse from input files. if unsuccessful, the original input file is returned.
                inputFiles = parseInputFilesFromFile(inputFiles.get(0));
            kb = new FileBasedTemporalKnowledgeBaseImpl(inputFiles, catalogFile, timer);
            try
            {
                KnowledgeBase firstKb = kb.first();
                if (firstKb != null)
                {
                    startTask("initial consistency check");
                    final boolean isConsistent = firstKb.isConsistent();
                    finishTask("initial consistency check");

                    if (!isConsistent)
                        throw new OpenlletCmdException("Ontology is inconsistent, run \"openllet explain\" to get " +
                                "the reason");
                }
            }
            catch (Exception e)
            {
                throw new OpenlletCmdException(e);
            }

        }
        catch (final InconsistentOntologyException e)
        {
            throw new OpenlletCmdException("Cannot query inconsistent ontology!", e);
        }
        catch (final RuntimeException e)
        {
            throw new OpenlletCmdException(e);
        }
    }

    private void loadQuery()
    {
        try
        {
            verbose("Query file: " + queryFile);
            startTask("parsing query file");

            queryString = IO.readWholeFileAsUTF8(queryFile);
            query = TemporalConjunctiveQueryParser.parse(queryString, kb);

            finishTask("parsing query file");

            verbose("Query: ");
            verbose("-----------------------------------------------------");
            verbose(queryString.trim());
            verbose("-----------------------------------------------------");
        }
        catch (final NotFoundException | RuntimeIOException | QueryParseException | ParseException e)
        {
            throw new OpenlletCmdException(e);
        }
    }

    private void execQuery()
    {
        Timer timer = _timers.createTimer("query execution (w/o loading & initial consistency check)");
        try
        {
            queryResults = queryEngine.exec(query, null, timer);
        }
        catch (RuntimeException | IOException | InterruptedException e)
        {
            throw new OpenlletCmdException(e);
        }
    }

    private void printQueryResults()
    {
        if (query.getResultVars().isEmpty())
            printAskQueryResult();
        else
            printSelectQueryResult();
    }

    private void printAskQueryResult()
    {
        output("ASK query result: ");
        output(queryResults.isEmpty() ? "no" : "yes");
    }

    private void printSelectQueryResult()
    {
        if (!queryResults.isEmpty())
        {
            if (outputFormat == OutputFormat.TABULAR)
                printTabularQueryResults();
            else if (outputFormat == OutputFormat.XML)
                printXMLQueryResults();
            else if (outputFormat == OutputFormat.JSON)
                printJSONQueryResults();
            else
                printTabularQueryResults();
        }
        else
        {
            output("Query Results (0 answers): ");
            output("NO RESULTS");
        }
    }

    private void printTabularQueryResults()
    {
        // number of distinct bindings
        int count = 0;

        // variables used in select
        final List<ATermAppl> resultVars = query.getResultVars();

        final List<List<String>> data = new ArrayList<>();
        for (ResultBinding binding : queryResults)
        {
            final List<String> formattedBinding = new ArrayList<>();
            for (ATermAppl resultVar : resultVars)
            {
                final ATermAppl result = binding.getValue(resultVar);
                // format the result
                String bindingString = result.toString(); // TODO
                formattedBinding.add(bindingString);
            }

            data.add(formattedBinding);
            count++;
        }

        output("Query Results (" + count + " answers): ");

        @SuppressWarnings({ "unchecked", "rawtypes" })
        final TableData table = new TableData((Collection) data, resultVars);
        final StringWriter tableSW = new StringWriter();
        table.print(tableSW);
        output(tableSW.toString());
    }

    private void printXMLQueryResults()
    {
        // TODO
        System.out.println("XML query result output not implemented yet");
    }

    private void printJSONQueryResults()
    {
        if (_verbose)
        {
            System.out.println("/* ");
            System.out.println(queryString.replace("*/", "* /"));
            System.out.println("*/ ");
        }
        System.out.println("JSON query result output not implemented yet");
        // TODO
    }

    public QueryResult getQueryResults()
    {
        return queryResults;
    }
}
