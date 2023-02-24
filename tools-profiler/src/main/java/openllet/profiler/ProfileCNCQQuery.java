package openllet.profiler;

import openllet.aterm.ATermAppl;
import openllet.core.KBLoader;
import openllet.core.tableau.completion.rule.AbstractTableauRule;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Timer;
import openllet.jena.JenaLoader;
import openllet.owlapi.OWLAPILoader;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.engine.cncq.CNCQQueryEngineSimple;
import openllet.query.sparqldl.model.cncq.CNCQQueryImpl;
import openllet.query.sparqldl.model.cq.*;
import openllet.query.sparqldl.model.results.QueryResult;
import org.apache.commons.cli.*;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.cncq.CNCQQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProfileCNCQQuery
{
    private final KnowledgeBase _kb;

    private static final String _PREFIX_DIST_VAR = "x";
    private static final String _PREFIX_UNDIST_VAR = "y";
    private Random _random;

    public ProfileCNCQQuery(KnowledgeBase kb)
    {
        _kb = kb;
    }

    private ATermAppl getRandomVarOrInt(int distVars, int undistVars)
    {
        List<ATermAppl> inds = _kb.getIndividuals().stream().toList();
        int varCase = _random.nextInt(3);
        ATermAppl var;
        switch (varCase)
        {
            case 0:
                var = inds.get(_random.ints(1, 0, inds.size()).findFirst().getAsInt());
                break;
            case 1:
                var = ATermUtils.makeVar(_PREFIX_DIST_VAR + _random.ints(1, 0, distVars + 1).findFirst().getAsInt());
                break;
            default:
                var = ATermUtils.makeVar(_PREFIX_UNDIST_VAR + _random.ints(1, 0, undistVars + 1).findFirst().getAsInt());
                break;
        }
        return var;
    }

    private QueryAtom createRandomQueryAtom(int distVars, int undistVars)
    {
        List<ATermAppl> inds = new ArrayList<>(_kb.getIndividuals());
        List<ATermAppl> classes = new ArrayList<>(_kb.getClasses());
        List<ATermAppl> roles = new ArrayList<>(_kb.getProperties());
        roles.remove(ATermUtils.TOP_OBJECT_PROPERTY);
        roles.remove(ATermUtils.BOTTOM_OBJECT_PROPERTY);
        classes.remove(ATermUtils.TOP);
        classes.remove(ATermUtils.BOTTOM);
        QueryAtom atom;
        if (_random.nextInt(2) == 0)
        {
            ATermAppl clazz = classes.get(_random.ints(1, 0, classes.size()).findFirst().getAsInt());
            ATermAppl ind = inds.get(_random.ints(1, 0, inds.size()).findFirst().getAsInt());
            atom = new QueryAtomImpl(QueryPredicate.Type, ind, clazz);
        }
        else
        {
            ATermAppl role = roles.get(_random.ints(1, 0, roles.size()).findFirst().getAsInt());
            ATermAppl var1 = getRandomVarOrInt(distVars, undistVars);
            ATermAppl var2 = getRandomVarOrInt(distVars, undistVars);
            atom = new QueryAtomImpl(QueryPredicate.PropertyValue, var1, role, var2);
        }
        return atom;
    }

    public CNCQQuery createRandomCNCQQuery(int atoms, int distVars, int undistVars)
    {
        CNCQQuery q = new CNCQQueryImpl(_kb, false);
        int numNegativeAtoms = _random.ints(1, 0, atoms).findFirst().getAsInt();
        int numNegatedSubQueries = numNegativeAtoms;
        if (numNegativeAtoms > 1)
            numNegatedSubQueries = _random.ints(1, 1, numNegativeAtoms).findFirst().getAsInt();
        int numRemainingNegativeAtoms = numNegativeAtoms;
        for (int i = 0; i < numNegatedSubQueries; i++)
        {
            ConjunctiveQuery negQuery = new ConjunctiveQueryImpl(_kb, false);
            negQuery.setNegation(true);
            int numNegativeAtomsInSubQuery = 1;
            if (numRemainingNegativeAtoms > 1)
                numNegativeAtomsInSubQuery  = _random.ints(1, 1, numRemainingNegativeAtoms + 1).findFirst().getAsInt();
            for (int j = 0; j < numNegativeAtomsInSubQuery; j++)
                negQuery.add(createRandomQueryAtom(distVars, undistVars));
            numRemainingNegativeAtoms -= numNegativeAtomsInSubQuery;
            q.addNegativeQuery(negQuery);
            if (numRemainingNegativeAtoms == 0)
                break;
        }
        // pos query
        ConjunctiveQuery posQuery = new ConjunctiveQueryImpl(_kb, false);
        for (int i = 0; i < atoms - numNegativeAtoms; i++)
            posQuery.add(createRandomQueryAtom(distVars, undistVars));
        q.addPositiveQuery(posQuery);
        // set dist vars
        // TODO Lukaks: take only dist vars actually selected for the query, also do this for sub-queries
        for (int i = 0; i < distVars; i++)
            q.addResultVar(ATermUtils.makeVar(_PREFIX_DIST_VAR + (i + 1)));
        return q;
    }

    public List<CNCQQuery> createRandomCNCQQueries(int minNumAtoms, int maxNumAtoms, int minDistVars, int maxDistVars,
                                                   int minUndistVars, int maxUndistVars, int numQueries, int seed)
    {
        assert(minNumAtoms <= maxNumAtoms  && minDistVars <= maxDistVars && minUndistVars <= maxUndistVars);
        List<CNCQQuery> queries = new ArrayList<>();
        for (int i = 0; i < numQueries; i++)
        {
            _random = new Random(seed + i);
            int atoms = _random.ints(1, minNumAtoms, maxNumAtoms + 1).findFirst().getAsInt();
            int distVars = _random.ints(1, minDistVars, maxDistVars + 1).findFirst().getAsInt();
            int undistVars = _random.ints(1, minUndistVars, maxUndistVars + 1).findFirst().getAsInt();
            queries.add(createRandomCNCQQuery(atoms, distVars, undistVars));
        }
        return queries;
    }

    public void profile(List<CNCQQuery> queries)
    {
        QueryExec<CNCQQuery> eng = new CNCQQueryEngineSimple();
        Timer t = new Timer();
        t.start();
        for (CNCQQuery q : queries)
        {
            System.out.println("Executing:");
            System.out.println(q);
            //QueryResult res = eng.exec(q);
            //System.out.println(res);
        }
        t.stop();
        System.out.println("Total time: " + t.getTotal() + " ms");
        System.out.println("Time per query: " + t.getTotal() / queries.size() + " ms");
    }

    public static KnowledgeBase readKB(String filePath)
    {
        final KBLoader loader = new OWLAPILoader();
        loader.parse(filePath);
        loader.load();
        return loader.getKB();
    }

    public static void main(final String[] args)
    {
        Options options = new Options();

        Option input = new Option("k", "kb", true, "KB file path");
        input.setRequired(true);
        options.addOption(input);

        Option a1 = new Option("a1", "min-atoms", true, "Minimum number of atoms");
        a1.setRequired(false);
        options.addOption(a1);

        Option a2 = new Option("a2", "max-atoms", true, "Maximum number of atoms");
        a2.setRequired(false);
        options.addOption(a2);

        Option d1 = new Option("d1", "min-distvars", true, "Minimum number of dist. vars");
        a2.setRequired(false);
        options.addOption(d1);

        Option d2 = new Option("d2", "max-distvars", true, "Maximum number of dist. vars");
        a2.setRequired(false);
        options.addOption(d2);

        Option u1 = new Option("u1", "min-undistvars", true, "Minimum number of dist. vars");
        a2.setRequired(false);
        options.addOption(u1);

        Option u2 = new Option("u2", "min-undistvars", true, "Maximum number of dist. vars");
        a2.setRequired(false);
        options.addOption(u2);

        Option q = new Option("q", "queries", true, "Number of queries");
        a2.setRequired(false);
        options.addOption(q);

        Option s = new Option("s", "seed", true, "The seed");
        a2.setRequired(false);
        options.addOption(s);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try
        {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e)
        {
            System.out.println(e.getMessage());
            formatter.printHelp("CNCQ Profiler", options);

            System.exit(1);
        }

        String kbPath = cmd.getOptionValue("kb");
        int a1v = Integer.parseInt(cmd.getOptionValue("a1"));
        int a2v = Integer.parseInt(cmd.getOptionValue("a2"));
        int d1v = Integer.parseInt(cmd.getOptionValue("d1"));
        int d2v = Integer.parseInt(cmd.getOptionValue("d2"));
        int u1v = Integer.parseInt(cmd.getOptionValue("u1"));
        int u2v = Integer.parseInt(cmd.getOptionValue("u2"));
        int qv = Integer.parseInt(cmd.getOptionValue("q"));
        int sv = Integer.parseInt(cmd.getOptionValue("s"));

        try
        {
            final ProfileCNCQQuery profiler = new ProfileCNCQQuery(readKB(kbPath));
            final List<CNCQQuery> queries = profiler.createRandomCNCQQueries(a1v, a2v, d1v, d2v, u1v, u2v, qv, sv);
            profiler.profile(queries);
        }
        catch (final Throwable t)
        {
            t.printStackTrace();
        }
    }
}
