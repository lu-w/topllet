package openllet.query.sparqldl.engine.cncq;

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.ABoxChanges;
import openllet.query.sparqldl.engine.ucq.UnionQueryEngineSimple;
import openllet.query.sparqldl.model.AtomQuery;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.query.sparqldl.model.ucq.UnionQueryImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemiBooleanCNCQEngineSimple extends AbstractSemiBooleanCNCQEngine
{
    private UnionQueryEngineSimple _ucqEngine = new UnionQueryEngineSimple();
    private boolean _rollUpBeforeChecking = false;
    private Map<ATermAppl, ATermAppl> _queryVarsToFreshInds = new HashMap<>();
    private ABoxChanges _changes;
    private ABox _abox = null;

    public SemiBooleanCNCQEngineSimple()
    {
        super();
    }

    public SemiBooleanCNCQEngineSimple(boolean rollUpBeforeChecking)
    {
        this();
        _rollUpBeforeChecking = rollUpBeforeChecking;
    }

    public SemiBooleanCNCQEngineSimple(UnionQueryEngineSimple ucqEngine)
    {
        this();
        _ucqEngine = ucqEngine;
    }

    public SemiBooleanCNCQEngineSimple(UnionQueryEngineSimple ucqEngine, boolean rollUpBeforeChecking)
    {
        this();
        _ucqEngine = ucqEngine;
        _rollUpBeforeChecking = rollUpBeforeChecking;
    }

    @Override
    protected QueryResult execABoxQuery(CNCQQuery q)
    {
        // 1. PRELIMINARY CONSISTENCY CHECK & INITIALIZING VARIABLES
        q.getKB().ensureConsistency();
        _abox = q.getKB().getABox();
        _changes = new ABoxChanges(_abox);

        // 2. SEPARATE POSITIVE AND NEGATIVE PART & MERGE POSITIVE PART
        ConjunctiveQuery positiveQuery = q.mergePositiveQueries();
        List<ConjunctiveQuery> negativeQueries = q.getNegativeQueries();

        // 2. SPLIT & ROLL-UP POSITIVE QUERIES (OPTIONAL)
        if (_rollUpBeforeChecking)
            positiveQuery = positiveQuery.splitAndRollUp(false);

        // 3. PUT POSITIVE ATOMS IN A-BOX
        putQueryAtomsInABox(positiveQuery);

        // 4. QUERY IS NOT SATISFIABLE IF KB IS INCONSISTENT
        if (!q.getKB().isConsistent())
            return new QueryResultImpl(q);

        // 5. CHECK FOR SATISFIABILITY
        QueryResult satResult = computeSatisfiableBindings(q);

        // 6. CLEAN-UP & ROLLING-BACK CHANGES
        cleanUp();

        return satResult;
    }

    private void putQueryAtomsInABox(AtomQuery<?> query)
    {
        for (QueryAtom atom : query.getAtoms())
        {
            switch (atom.getPredicate())
            {
                case Type ->
                {
                    ATermAppl var = getIndividual(atom.getArguments().get(0));
                    ATermAppl type = atom.getArguments().get(1);
                    _changes.apply(new ABoxChanges.TypeChange(var, type));
                }
                case PropertyValue ->
                {
                    ATermAppl subj = getIndividual(atom.getArguments().get(0));
                    ATermAppl pred = atom.getArguments().get(1);
                    ATermAppl obj = getIndividual(atom.getArguments().get(2));
                    _changes.apply(new ABoxChanges.PropertyChange(subj, pred, obj));
                }
                default -> _logger.warning("Encountered query predicate that is not supported: " + atom.getPredicate());
            }
        }
    }

    private ATermAppl getIndividual(ATermAppl var)
    {
        ATermAppl res = var;
        if (!_abox.getKB().isIndividual(var))
        {
            if (!_queryVarsToFreshInds.containsKey(var))
            {
                ABoxChanges.FreshIndChange change = new ABoxChanges.FreshIndChange();
                _changes.apply(change);
                _queryVarsToFreshInds.put(var, change.getInd().getTerm());
            }
            res = _queryVarsToFreshInds.get(var);
        }
        return res;
    }

    private QueryResult computeSatisfiableBindings(CNCQQuery query)
    {
        QueryResult res = new QueryResultImpl(query);
        if (_abox.isConsistent())
        {
            UnionQuery ucq = new UnionQueryImpl(_abox.getKB(), query.isDistinct());
            for (ConjunctiveQuery negQuery : query.getNegativeQueries())
            {
                ConjunctiveQuery positiveQuery = negQuery.copy();
                positiveQuery.setNegation(false);
                ucq.addQuery(positiveQuery);
            }
            res = _ucqEngine.exec(ucq);
        }
        return res.invert();
    }

    private void cleanUp()
    {
        _changes.revertAll();
        _queryVarsToFreshInds = new HashMap<>();
    }
}
