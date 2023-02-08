package openllet.query.sparqldl.model.ucq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.ATermUtils;
import openllet.query.sparqldl.model.AbstractQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.cq.*;
import openllet.shared.tools.Log;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnionQueryImpl extends AbstractQuery implements UnionQuery
{
    public static final Logger _logger = Log.getLogger(UnionQueryImpl.class);

    protected List<ConjunctiveQuery> _queries = new ArrayList<>();

    public UnionQueryImpl(final KnowledgeBase kb, final boolean distinct)
    {
        super(kb, distinct);
    }

    public UnionQueryImpl(final UnionQuery query)
    {
        this(query.getKB(), query.isDistinct());
    }

    @Override
    public List<ConjunctiveQuery> getQueries()
    {
        return _queries;
    }

    @Override
    public void setQueries(List<ConjunctiveQuery> queries)
    {
        boolean warningLogged = false;
        _queries = new ArrayList<>();
        for (ConjunctiveQuery q : queries)
            if (!warningLogged)
                warningLogged = addQuery(q, true);
            else
                addQuery(q, false);
    }

    @Override
    public UnionQuery apply(final ResultBinding binding)
    {
        final UnionQuery query = copy();
        query.setQueries(new ArrayList<>());
        for (ConjunctiveQuery disjunct : _queries)
        {
            ConjunctiveQuery boundDisjunct = disjunct.apply(binding);
            query.addQuery(boundDisjunct);
        }
        return query;
    }

    public void addQuery(final ConjunctiveQuery query)
    {
        addQuery(query, false);
    }

    /**
     * Adds some logging features to addQuery for internal use.
     * @param query the disjunct to add
     * @param logSharedUndistVarWarning whether to log if the new query has undistinguished variables that are
     *                                  already present in the existing disjuncts
     * @return true iff. a warning for sharing undistinguished variables was logged
     */
    private boolean addQuery(final ConjunctiveQuery query, boolean logSharedUndistVarWarning)
    {
        this._queries.add(query);
        // Propagates variables to the union query
        _allVars.addAll(query.getVars());
        for (ATermAppl resVar : query.getResultVars())
            if (!_resultVars.contains(resVar))
                _resultVars.add(resVar);
        for (final VarType type : VarType.values())
            _distVars.get(type).addAll(query.getDistVarsForType(type));
        // Updates the ground information (this may have changed due to the new disjunct)
        _ground &= query.isGround();
        if (logSharedUndistVarWarning && _logger.isLoggable(Level.FINE) && disjunctsShareUndistVars())
        {
            _logger.fine("Union query " + this + " contains disjuncts that share undistinguished variables. Will " +
                    "treat them as different variables.");
            return true;
        }
        return false;
    }

    /**
     * Recursive implementation of toCNF.
     * Note: This method creates new query and union query objects, but it does not create new atoms. It rather re-uses
     * the atoms from the original query.
     * TODO Lukas: check whether we need to do a deep copy of the atoms. For now, this should suffice.
     * @param query The input union query (i.e. of the form (a ^ ... ^ b) v ... v (c ^ ... ^ d)) to convert into CNF
     * @return A list of union queries, where each conjunctive query of each union query contains only one atom
     * representing the CNF of the input query
     */
    private List<DisjunctiveQuery> toCNFRec(UnionQuery query)
    {
        List<DisjunctiveQuery> newCnf = new ArrayList<>();
        // Safety check: no queries given
        if (query.getQueries().size() == 0)
            return newCnf;
        // Base case: CNF(a_1 ^ ... ^ a_n) = a_1 ^ ... ^ a_n
        // Return a list of atoms (wrapped in a Query and again in a UnionQuery) to represent this conjunction.
        else if (query.getQueries().size() == 1) {
            for (QueryAtom a : query.getQueries().get(0).getAtoms())
            {
                DisjunctiveQuery singleUnion = new DisjunctiveQueryImpl(query.getKB(), query.isDistinct());
                ConjunctiveQuery singleQuery = new ConjunctiveQueryImpl(query.getQueries().get(0));
                singleQuery.add(a);
                singleUnion.addQuery(singleQuery);
                newCnf.add(singleUnion);
            }
            return newCnf;
        }
        // Recursive case: CNF((a_1 ^ ... ^ a_n) v q) = \bigwedge_{x \in a_1 ... a_n} \bigwedge_{c \in CNF(q)} (x v c)
        else
        {
            // Fetch CNF(q)
            UnionQuery subQuery = new UnionQueryImpl(query.getKB(), query.isDistinct());
            for (ConjunctiveQuery q : query.getQueries().subList(1, query.getQueries().size()))
                subQuery.addQuery(q);
            List<DisjunctiveQuery> cnf = toCNFRec(subQuery);
            // Create (x v c) for all atoms x and conjuncts c
            for (QueryAtom atom : query.getQueries().get(0).getAtoms())
                for (DisjunctiveQuery conjunct : cnf)
                {
                    DisjunctiveQuery c = new DisjunctiveQueryImpl(query.getKB(), query.isDistinct());
                    for (ConjunctiveQuery sq : conjunct.getQueries())
                        c.addQuery(sq);
                    ConjunctiveQuery q = new ConjunctiveQueryImpl(query.getKB(), query.isDistinct());
                    q.add(atom);
                    c.addQuery(q);
                    newCnf.add(c);
                }
        }
        return newCnf;
    }

    @Override
    public CNFQuery toCNF()
    {
        List<DisjunctiveQuery> cnf = toCNFRec(this);
        CNFQuery cnfQuery = new CNFQueryImpl(this.getKB(), this.isDistinct());
        cnfQuery.setQueries(cnf);
        return cnfQuery;
    }

    @Override
    public UnionQuery reorder(int[] queries)
    {
        // TODO Lukas: implement query reordering for UCQs.
        return copy();
    }

    @Override
    public boolean disjunctsShareUndistVars()
    {
        boolean shared = false;
        if (_queries.size() > 1)
        {
            Set<ATermAppl> undistVars = _queries.get(0).getUndistVars();
            for (ConjunctiveQuery query : _queries.subList(1, _queries.size()))
                undistVars.retainAll(query.getUndistVars());
            shared = !undistVars.isEmpty();
        }
        return shared;
    }

    @Override
    public UnionQuery rollUp()
    {
        // TODO Lukas: rolling up should be able to stop on distinguished variables
        // We can not roll up if we have nothing to roll up to.
        assert(!getDistVars().isEmpty() || !getConstants().isEmpty() || !getUndistVars().isEmpty());
        UnionQuery rolledUpUnionQuery = new UnionQueryImpl(this);
        for (ConjunctiveQuery conjunctiveQuery : _queries)
        {
            if (_logger.isLoggable(Level.FINER))
                _logger.finer("Rolling up for conjunctive query: " + conjunctiveQuery);

            // 1. step: Find disjoint parts of the query
            List<Query> splitQueries = conjunctiveQuery.split(true);
            if (_logger.isLoggable(Level.FINER))
            {
                _logger.finer("Split query: " + splitQueries);
                _logger.finer("Now rolling up each separate element.");
            }

            // 2. step: Roll each part up
            ConjunctiveQuery rolledUpQuery = new ConjunctiveQueryImpl(this.getKB(), this.isDistinct());
            for (Query connectedQuery : splitQueries)
            {
                final ATermAppl testIndOrVar;
                if (!connectedQuery.getDistVars().isEmpty())
                    testIndOrVar = connectedQuery.getDistVars().iterator().next();
                else if (!connectedQuery.getConstants().isEmpty())
                    testIndOrVar = connectedQuery.getConstants().iterator().next();
                else if (!connectedQuery.getUndistVars().isEmpty())
                    testIndOrVar = connectedQuery.getUndistVars().iterator().next();
                else
                    throw new RuntimeException("Rolling up procedure did not find any individual or variable to roll " +
                            "up to.");
                final ATermAppl testClass = ((ConjunctiveQuery) connectedQuery).rollUpTo( testIndOrVar,
                        Collections.emptySet(), false);
                if (_logger.isLoggable(Level.FINER))
                    _logger.finer("Rolled-up Boolean query: " + testIndOrVar + " -> " + testClass);
                QueryAtom rolledUpAtom = new QueryAtomImpl(QueryPredicate.Type, testIndOrVar, testClass);
                rolledUpQuery.add(rolledUpAtom);
            }
            rolledUpUnionQuery.addQuery(rolledUpQuery);
        }
        for (ATermAppl var : _resultVars)
            rolledUpUnionQuery.addResultVar(var);
        for (VarType varType : _distVars.keySet())
            for (ATermAppl var : _distVars.get(varType))
                rolledUpUnionQuery.addDistVar(var, varType);
        return rolledUpUnionQuery;
    }

    @Override
    public List<Query> split()
    {
        // TODO Lukas
        return List.of(this);
    }

    @Override
    public boolean hasCycle()
    {
        boolean hasCycle = false;
        for (ConjunctiveQuery q : _queries)
            hasCycle |= q.hasCycle();
        return hasCycle;
    }

    @Override
    public UnionQuery copy()
    {
        UnionQuery copy = new UnionQueryImpl(this);
        for (ConjunctiveQuery q : _queries)
            copy.addQuery((ConjunctiveQuery) q.copy());
        return copy;
    }

    @Override
    public String toString()
    {
        return toString(false);
    }

    public String toString(final boolean multiLine)
    {
        final String indent = multiLine ? "     " : " ";
        final StringBuilder sb = new StringBuilder();

        sb.append(ATermUtils.toString(_name)).append("(");
        for (int i = 0; i < _resultVars.size(); i++)
        {
            final ATermAppl var = _resultVars.get(i);
            if (i > 0)
                sb.append(", ");
            sb.append(ATermUtils.toString(var));
        }
        sb.append(")");

        sb.append(" :-");

        List<ConjunctiveQuery> queries = _queries;
        if (_queries.size() == 0)
            queries = List.of((ConjunctiveQuery) this);
        for (int i = 0; i < queries.size(); i++)
        {
            final ConjunctiveQuery query = queries.get(i);
            if (i > 0)
            {
                sb.append(" v");
                if (multiLine)
                    sb.append("\n");
            }
            if (query.getAtoms().size() > 0)
            {
                if (multiLine)
                    sb.append("\n");
                for (int j = 0; j < query.getAtoms().size(); j++) {
                    final QueryAtom a = query.getAtoms().get(j);
                    if (j > 0) {
                        sb.append(",");
                        if (multiLine)
                            sb.append("\n");
                    }

                    sb.append(indent);
                    sb.append(a.toString()); // TODO qNameProvider
                }
            }
        }

        sb.append(".");
        if (multiLine)
            sb.append("\n");
        return sb.toString();
    }
}
