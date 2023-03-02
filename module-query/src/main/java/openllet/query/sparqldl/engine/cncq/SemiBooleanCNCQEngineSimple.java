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
import openllet.query.sparqldl.model.results.ResultBindingImpl;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.query.sparqldl.model.ucq.UnionQueryImpl;

import java.util.HashMap;
import java.util.Map;

public class SemiBooleanCNCQEngineSimple extends AbstractSemiBooleanCNCQEngine
{
    private UnionQueryEngineSimple _ucqEngine = new UnionQueryEngineSimple();
    private boolean _rollUpBeforeChecking = false;
    private Map<ATermAppl, ATermAppl> _queryVarsToFreshInds = new HashMap<>();
    private ABoxChanges _changes;

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
        QueryResult satResult = new QueryResultImpl(q);

        // 1. PRELIMINARY CONSISTENCY CHECK
        if (q.getKB().isConsistent())
        {
            _changes = new ABoxChanges(q.getKB().getABox());
            _abox = _changes.getABox();

            // 2. SEPARATE POSITIVE AND NEGATIVE PART & MERGE POSITIVE PART
            ConjunctiveQuery positiveQuery = q.mergePositiveQueries();

            // 2. SPLIT & ROLL-UP POSITIVE QUERIES (OPTIONAL)
            if (_rollUpBeforeChecking)
                positiveQuery = positiveQuery.splitAndRollUp(false);

            // 3. PUT POSITIVE ATOMS IN A-BOX
            putQueryAtomsInABox(positiveQuery);

            // 4. CHECK FOR SATISFIABILITY
            satResult = computeSatisfiableBindings(q);

            // 5. CLEAN-UP & ROLLING-BACK CHANGES
            cleanUp();
        }

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
                _queryVarsToFreshInds.put(var, change.getInd());
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
            ABox abox2 = _abox;
            UnionQuery ucq = new UnionQueryImpl(_abox.getKB(), query.isDistinct());
            for (ConjunctiveQuery negQuery : query.getNegativeQueries())
            {
                ConjunctiveQuery positiveQuery = negQuery.copy();
                positiveQuery.setNegation(false);
                ucq.addQuery(positiveQuery);
            }
            res = _ucqEngine.exec(ucq, _abox);
        }
        // If ABox is inconsistent, the query is not satisfiable (thus the UCQ is trivially entailed)
        else
            res.add(new ResultBindingImpl());
        return res.invert();
    }

    private void cleanUp()
    {
        _changes.revertAll(); // TODO Lukas: we do not need to use the ABox Change class anymore - since we are
        // working on the copies only. Therefore, also no reverting. Nevertheless, profiling (for n=100) gives some
        // weird unknown edges still....
        _queryVarsToFreshInds = new HashMap<>();
    }
}
