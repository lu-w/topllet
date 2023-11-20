package openllet.profiler;

import openllet.aterm.ATermAppl;
import openllet.core.KBLoader;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Timer;
import openllet.owlapi.OWLAPILoader;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.engine.bcq.BCQQueryEngineSimple;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.bcq.BCQQueryImpl;
import openllet.query.sparqldl.model.cq.*;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import org.apache.commons.cli.*;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.bcq.BCQQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class ProfileBCQQuery
{
    private final KnowledgeBase _kb;
    private final List<ATermAppl> _inds;
    private final List<ATermAppl> _classes;
    private final List<ATermAppl> _roles;
    private static final String _PREFIX_DIST_VAR = "_dist_x";
    private static final String _PREFIX_UNDIST_VAR = "_undist_y";
    private int _numDistVars = 0;
    private Random _random;
    private static final boolean _USE_INDIVIDUALS_IN_ATOMS = true;

    public ProfileBCQQuery(KnowledgeBase kb)
    {
        System.out.println("Expressivity: " + kb.getExpressivity());
        _kb = kb;
        _inds = new ArrayList<>(_kb.getIndividuals().stream().toList());
        _classes = new ArrayList<>(_kb.getClasses());
        _roles = new ArrayList<>(_kb.getObjectProperties());
        _roles.remove(ATermUtils.TOP_OBJECT_PROPERTY);
        _roles.remove(ATermUtils.BOTTOM_OBJECT_PROPERTY);
        _classes.remove(ATermUtils.TOP);
        _classes.remove(ATermUtils.BOTTOM);
        // Ensures deterministic order
        _inds.sort(Comparator.comparing(Object::toString));
        _roles.sort(Comparator.comparing(Object::toString));
        _classes.sort(Comparator.comparing(Object::toString));
    }

    private ATermAppl getRandomVarOrInt(int distVars, int undistVars)
    {
        List<Integer> rand = new ArrayList<>();
        if (distVars > 0)
            rand.add(0);
        if (undistVars > 0)
            rand.add(1);
        if (_USE_INDIVIDUALS_IN_ATOMS)
            rand.add(2);
        int varCase = rand.get(_random.nextInt(rand.size()));
        if (_numDistVars < distVars)
            varCase = 0;
        switch (varCase)
        {
            case 0:
                int index = _numDistVars;
                if (index >= distVars)
                    index = _random.ints(1, 1, distVars + 1).findFirst().getAsInt();
                _numDistVars++;
                return ATermUtils.makeVar(_PREFIX_DIST_VAR + index);
            case 1:
                return ATermUtils.makeVar(_PREFIX_UNDIST_VAR + _random.ints(1, 1, undistVars + 1).findFirst().getAsInt());
            default:
                return _inds.get(_random.ints(1, 0, _inds.size()).findFirst().getAsInt());
        }
    }

    private QueryAtom createRandomQueryAtom(int distVars, int undistVars)
    {
        QueryAtom atom;
        if (_random.nextInt(2) == 0)
        {
            ATermAppl clazz = _classes.get(_random.ints(1, 0, _classes.size()).findFirst().getAsInt());
            ATermAppl var = getRandomVarOrInt(distVars, undistVars);
            atom = new QueryAtomImpl(QueryPredicate.Type, var, clazz);
        }
        else
        {
            ATermAppl role = _roles.get(_random.ints(1, 0, _roles.size()).findFirst().getAsInt());
            ATermAppl var1 = getRandomVarOrInt(distVars, undistVars);
            ATermAppl var2 = getRandomVarOrInt(distVars, undistVars);
            atom = new QueryAtomImpl(QueryPredicate.PropertyValue, var1, role, var2);
        }
        return atom;
    }

    public BCQQuery createRandomBCQQuery(int atoms, int distVars, int undistVars)
    {
        BCQQuery q = new BCQQueryImpl(_kb, false);
        int numNegativeAtoms = _random.ints(1, 0, atoms + 1).findFirst().getAsInt();
        int numNegatedSubQueries = numNegativeAtoms;
        if (numNegativeAtoms > 0)
            numNegatedSubQueries = _random.ints(1, 1, numNegativeAtoms + 1).findFirst().getAsInt();
        int numRemainingNegativeAtoms = numNegativeAtoms;
        for (int i = 0; i < numNegatedSubQueries; i++)
        {
            ConjunctiveQuery negQuery = new ConjunctiveQueryImpl(_kb, false);
            negQuery.setNegation(true);
            int numNegativeAtomsInSubQuery = 1;
            if (numRemainingNegativeAtoms > 0)
                numNegativeAtomsInSubQuery  = _random.ints(1, 1, numRemainingNegativeAtoms + 1).findFirst().getAsInt();
            for (int j = 0; j < numNegativeAtomsInSubQuery; j++)
                negQuery.add(createRandomQueryAtom(distVars, undistVars));
            numRemainingNegativeAtoms -= numNegativeAtomsInSubQuery;
            q.addNegativeQuery(negQuery);
            if (numRemainingNegativeAtoms == 0)
                break;
        }
        // pos query (if we have remaining atoms)
        if (atoms - numNegativeAtoms > 0)
        {
            ConjunctiveQuery posQuery = new ConjunctiveQueryImpl(_kb, false);
            for (int i = 0; i < atoms - numNegativeAtoms; i++)
                posQuery.add(createRandomQueryAtom(distVars, undistVars));
            q.addPositiveQuery(posQuery);
        }
        // set dist vars
        for (ConjunctiveQuery cq : q.getQueries())
            for (QueryAtom a : cq.getAtoms())
                for (ATermAppl v : a.getArguments())
                    if (v.toString().contains(_PREFIX_DIST_VAR)) // quite hacky
                    {
                        if (!cq.getResultVars().contains(v))
                            cq.addResultVar(v);
                        if (!cq.getDistVars().contains(v))
                            cq.addDistVar(v, Query.VarType.INDIVIDUAL);
                        if (!q.getDistVars().contains(v))
                            q.addDistVar(v, Query.VarType.INDIVIDUAL);
                        if (!q.getResultVars().contains(v))
                            q.addResultVar(v);
                    }
        return q;
    }

    public List<BCQQuery> createRandomBCQQueries(int atoms, int distVars, int undistVars, int numQueries, int seed)
    {
        List<BCQQuery> queries = new ArrayList<>();
        for (int i = 0; i < numQueries; i++)
        {
            _random = new Random(seed + i);
            queries.add(createRandomBCQQuery(atoms, distVars, undistVars));
        }
        return queries;
    }

    public void profile(List<BCQQuery> queries)
    {
        QueryExec<BCQQuery> eng = new BCQQueryEngineSimple();
        Timer t = new Timer();
        t.start();
        for (BCQQuery q : queries)
        {
            System.out.println("Executing:");
            System.out.println(q);
            QueryResult res = null;
            try
            {
                res = eng.exec(q);
            }
            catch (IOException | InterruptedException e)
            {
                res = new QueryResultImpl(q);
            }
            System.out.println(res.size());
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

        Option a = new Option("a", "atoms", true, "Number of atoms");
        a.setRequired(true);
        options.addOption(a);

        Option d = new Option("d", "distvars", true, "Number of dist. vars (default 0)");
        d.setRequired(false);
        options.addOption(d);

        Option u = new Option("u", "undistvars", true, "Number of dist. vars (default: 0)");
        u.setRequired(false);
        options.addOption(u);

        Option q = new Option("q", "queries", true, "Number of queries (default: 1)");
        q.setRequired(false);
        options.addOption(q);

        Option s = new Option("s", "seed", true, "The seed (default: 0)");
        s.setRequired(false);
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
            formatter.printHelp("BCQ Profiler", options);

            System.exit(1);
        }

        String kbPath = cmd.getOptionValue("kb");
        int av = Integer.parseInt(cmd.getOptionValue("a"));
        int dv = Integer.parseInt(cmd.getOptionValue("d", "0"));
        int uv = Integer.parseInt(cmd.getOptionValue("u", "0"));
        int qv = Integer.parseInt(cmd.getOptionValue("q", "1"));
        int sv = Integer.parseInt(cmd.getOptionValue("s", "0"));

        try
        {
            final ProfileBCQQuery profiler = new ProfileBCQQuery(readKB(kbPath));
            final List<BCQQuery> queries = profiler.createRandomBCQQueries(av, dv, uv, qv, sv);
            profiler.profile(queries);
        }
        catch (final Throwable t)
        {
            t.printStackTrace();
        }
    }
}
