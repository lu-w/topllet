package openllet;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.exceptions.InconsistentOntologyException;
import openllet.core.output.TableData;
import openllet.core.utils.Timer;
import openllet.mtcq.engine.MTCQNormalFormEngine;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.mtcq.engine.MTCQEngine;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.mtcq.model.kb.FileBasedTemporalKnowledgeBaseImpl;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.mtcq.parser.ParseException;
import openllet.mtcq.parser.MetricTemporalConjunctiveQueryParser;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.query.*;
import org.apache.jena.shared.NotFoundException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static openllet.OpenlletCmdOptionArg.*;

public class OpenlletTemporalQuery extends OpenlletCmdApp
{
    private String queryFile;
    private String catalogFile;
    private boolean equalAnswersAllowed;
    private QueryResult queryResults;
    private String queryString;
    private MetricTemporalConjunctiveQuery query;
    private TemporalKnowledgeBase kb;
    private final QueryExec<MetricTemporalConjunctiveQuery> queryEngine = new MTCQNormalFormEngine();
    private OutputFormat outputFormat = OutputFormat.TABULAR;

    private enum OutputFormat
    {
        TABULAR, XML, JSON
    }

    // TODO: remove input format from options (maybe a posteriori remove()?)
    // TODO: what about ignore imports in options?

    @Override
    public String getAppId()
    {
        return "Topplet: An Engine for Answering Metric Temporal Conjunctive Queries";
    }

    @Override
    public String getAppCmd()
    {
        return "topllet " + getMandatoryOptions() + "[options] <QUERY> <TKB>";
    }

    @Override
    public OpenlletCmdOptions getOptions()
    {
        final OpenlletCmdOptions options = getGlobalOptions();

        final OpenlletCmdOption catalogOption = new OpenlletCmdOption("catalog");
        catalogOption.setShortOption("c");
        catalogOption.setType("catalog file");
        catalogOption.setDescription("An OASIS XML catalog file to resolve URIs");
        catalogOption.setArg(REQUIRED);
        catalogOption.setIsMandatory(false);
        options.add(catalogOption);

        final OpenlletCmdOption distinctOption = new OpenlletCmdOption("equal");
        distinctOption.setShortOption("e");
        distinctOption.setDescription("Whether different variables of a generated answer can be mapped to the same " +
                "(equal) individual");
        distinctOption.setArg(NONE);
        distinctOption.setIsMandatory(false);
        options.add(distinctOption);

        return options;
    }

    @Override
    public void parseArgs(final String[] args)
    {
        // Handles direct call from topllet CLI and not from openllet temporal-query by adding temporal-query as arg
        if (!"temporal-query".equals(args[0]))
        {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = "temporal-query";
            System.arraycopy(args, 0, newArgs, 1, args.length);
            super.parseArgs(newArgs);
        }
        else
            super.parseArgs(args);
        setCatalogFile(_options.getOption("catalog").getValueAsString());
        setEqualAnswers(_options.getOption("equal").getValueAsBoolean());
        setOutputFormat("Tabular"); // Currently, no other output format is supported, so no option for it.
    }

    private void setCatalogFile(final String s)
    {
        catalogFile = s;
    }

    private void setEqualAnswers(Boolean e)
    {
        equalAnswersAllowed = e;
    }

    protected List<String> parseInputFilesFromFile(String inputFile)
    {
        try
        {
            return FileBasedTemporalKnowledgeBaseImpl.parseKBSFile(inputFile);
        }
        catch (final FileNotFoundException e)
        {
            throw new OpenlletCmdException(e);
        }
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
            if (inputFiles.size() == 2)
            {
                setQueryFile(inputFiles.get(0));
                // tries to parse from input files. if unsuccessful, the original input file is returned.
                inputFiles = parseInputFilesFromFile(inputFiles.get(1));
            }
            else
                throw new OpenlletCmdException("Expected two required input arguments. Received " + inputFiles.size());
            kb = new FileBasedTemporalKnowledgeBaseImpl(inputFiles, catalogFile, timer);
            try
            {
                KnowledgeBase firstKb = kb.get(0);
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
                throw new OpenlletCmdException(e.getMessage());
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

            Path queryFilePath = Paths.get(queryFile);
            if (Files.exists(queryFilePath))
            {
                queryString = Files.readString(queryFilePath);
                query = MetricTemporalConjunctiveQueryParser.parse(queryString, kb, !equalAnswersAllowed);

                finishTask("parsing query file");

                verbose("Query: ");
                verbose("-----------------------------------------------------");
                verbose(queryString.trim());
                verbose("-----------------------------------------------------");
            }
            else
                throw new OpenlletCmdException(new FileNotFoundException(queryFile));
        }
        catch (final IOException | NotFoundException | RuntimeIOException | QueryParseException | ParseException e)
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
        catch (RuntimeException e)
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
        output(queryResults == null || queryResults.isEmpty() ? "no" : "yes");
    }

    private void printSelectQueryResult()
    {
        if (queryResults != null && !queryResults.isEmpty())
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
                if (result != null)
                {
                    String bindingString = result.toString();
                    formattedBinding.add(bindingString);
                }
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
        // TODO implement XML query result for openllet-mtcq
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
        // TODO implement JSON query result for openllet-mtcq
    }
}
