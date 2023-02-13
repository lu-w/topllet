package openllet.query.sparqldl.model.ucq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.AbstractCompositeQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.*;
import openllet.shared.tools.Log;

import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnionQueryImpl extends AbstractCompositeQuery<ConjunctiveQuery, UnionQuery> implements UnionQuery
{
    public static final Logger _logger = Log.getLogger(UnionQueryImpl.class);

    public UnionQueryImpl(final KnowledgeBase kb, final boolean distinct)
    {
        super(kb, distinct);
    }

    @Override
    protected String getCompositeDelimiter()
    {
        return "v";
    }

    public UnionQueryImpl(final Query<?> query)
    {
        this(query.getKB(), query.isDistinct());
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

    /**
     * Adds some logging features to addQuery for internal use.
     * @param query the disjunct to add
     * @param logSharedUndistVarWarning whether to log if the new query has undistinguished variables that are
     *                                  already present in the existing disjuncts
     * @return true iff. a warning for sharing undistinguished variables was logged
     */
    private boolean addQuery(final ConjunctiveQuery query, boolean logSharedUndistVarWarning)
    {
        super.addQuery(query);
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
                for (VarType varType : query.getDistVarsWithVarType().keySet())
                    for (ATermAppl var : query.getDistVarsForType(varType))
                        if (a.getArguments().contains(var))
                            singleQuery.addDistVar(var, varType);
                for (ATermAppl var : query.getResultVars())
                    if (a.getArguments().contains(var))
                        singleQuery.addResultVar(var);
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
            subQuery.setQueries(query.getQueries().subList(1, query.getQueries().size()));
            for (ATermAppl var : query.getResultVars())
                subQuery.addResultVar(var);
            for (VarType varType : query.getDistVarsWithVarType().keySet())
                for (ATermAppl var : query.getDistVarsForType(varType))
                    subQuery.addDistVar(var, varType);
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
                    for (VarType varType : query.getDistVarsWithVarType().keySet())
                        for (ATermAppl var : query.getDistVarsForType(varType))
                            if (atom.getArguments().contains(var))
                                q.addDistVar(var, varType);
                    for (ATermAppl var : query.getResultVars())
                        if (atom.getArguments().contains(var))
                            q.addResultVar(var);
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
        // Order of result/distinguished variables may change during conversion to CNF, therefore just reset them.
        cnfQuery.setDistVars(getDistVarsWithVarType());
        cnfQuery.setResultVars(getResultVars());
        return cnfQuery;
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
        return rollUp(false);
    }

    @Override
    public UnionQuery rollUp(boolean stopRollingOnDistVars)
    {
        // We can not roll up if we have nothing to roll up to.
        assert(!getDistVars().isEmpty() || !getConstants().isEmpty() || !getUndistVars().isEmpty());
        UnionQuery rolledUpUnionQuery = new UnionQueryImpl(this);
        for (ConjunctiveQuery conjunctiveQuery : _queries)
        {
            if (_logger.isLoggable(Level.FINER))
                _logger.finer("Rolling up for conjunctive query: " + conjunctiveQuery);

            // 1. step: Find disjoint parts of the query
            List<ConjunctiveQuery> splitQueries = conjunctiveQuery.split(true, stopRollingOnDistVars);
            if (_logger.isLoggable(Level.FINER))
            {
                _logger.finer("Split query: " + splitQueries);
                _logger.finer("Now rolling up each separate element.");
            }

            // 2. step: Roll each part up
            ConjunctiveQuery rolledUpQuery = new ConjunctiveQueryImpl(this.getKB(), this.isDistinct());
            for (ConjunctiveQuery connectedQuery : splitQueries)
            {
                if (connectedQuery.getDistVars().size() <= 1)
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
                    final ATermAppl testClass = connectedQuery.rollUpTo(testIndOrVar,
                            Collections.emptySet(), false);
                    if (_logger.isLoggable(Level.FINER))
                        _logger.finer("Rolled-up Boolean query: " + testIndOrVar + " -> " + testClass);
                    QueryAtom rolledUpAtom = new QueryAtomImpl(QueryPredicate.Type, testIndOrVar, testClass);
                    rolledUpQuery.add(rolledUpAtom);
                }
                else
                {
                    // we can not roll-up queries that contain more than one distinguished variables -> just leave it
                    for (QueryAtom atom : connectedQuery.getAtoms())
                        rolledUpQuery.add(atom);
                }
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
    public List<UnionQuery> split()
    {
        // UCQs shall not be split due to their semantics.
        _logger.fine("Tried to split a union query, but union queries shall not be split.");
        return List.of(this);
    }

    @Override
    protected UnionQuery createQuery(Query<?> query)
    {
        return new UnionQueryImpl(query);
    }
}
