package openllet.query.sparqldl.engine.cncq;

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.ABoxChanges;
import openllet.core.boxes.abox.ABoxChanges.*;
import openllet.query.sparqldl.engine.AbstractBooleanQueryEngine;
import openllet.query.sparqldl.engine.ucq.UnionQueryEngineSimple;
import openllet.query.sparqldl.model.AtomQuery;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.query.sparqldl.model.ucq.UnionQueryImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This engine checks for the *satisfiability* of conjunctions of possibly negated conjunctive queries.
 */
public class BooleanCNCQQueryEngineSimple extends AbstractBooleanQueryEngine<CNCQQuery>
{
    private UnionQueryEngineSimple _ucqEngine = new UnionQueryEngineSimple();
    private boolean _rollUpBeforeChecking = false;
    private Map<ATermAppl, ATermAppl> _queryVarsToFreshInds = new HashMap<>();
    private ABoxChanges _changes;
    private ABox _abox = null;

    public BooleanCNCQQueryEngineSimple()
    {
        super();
    }

    public BooleanCNCQQueryEngineSimple(boolean rollUpBeforeChecking)
    {
        this();
        _rollUpBeforeChecking = rollUpBeforeChecking;
    }

    public BooleanCNCQQueryEngineSimple(UnionQueryEngineSimple ucqEngine)
    {
        this();
        _ucqEngine = ucqEngine;
    }

    public BooleanCNCQQueryEngineSimple(UnionQueryEngineSimple ucqEngine, boolean rollUpBeforeChecking)
    {
        this();
        _ucqEngine = ucqEngine;
        _rollUpBeforeChecking = rollUpBeforeChecking;
    }

    @Override
    protected boolean execBooleanABoxQuery(CNCQQuery q)
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
            return false;

        // 5. CHECK FOR SATISFIABILITY
        boolean isSat = isSatisfied(negativeQueries, q.isDistinct());

        // 6. CLEAN-UP & ROLLING-BACK CHANGES
        cleanUp();

        return isSat;
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
                    _changes.apply(new TypeChange(var, type));
                }
                case PropertyValue ->
                {
                    ATermAppl subj = getIndividual(atom.getArguments().get(0));
                    ATermAppl pred = atom.getArguments().get(1);
                    ATermAppl obj = getIndividual(atom.getArguments().get(2));
                    _changes.apply(new PropertyChange(subj, pred, obj));
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
                FreshIndChange change = new FreshIndChange();
                _changes.apply(change);
                _queryVarsToFreshInds.put(var, change.getInd().getTerm());
            }
            res = _queryVarsToFreshInds.get(var);
        }
        return res;
    }

    private boolean isSatisfied(List<ConjunctiveQuery> negativeQueries, boolean isDistinct)
    {
        boolean res = false;
        if (_abox.isConsistent())
        {
            UnionQuery ucq = new UnionQueryImpl(_abox.getKB(), isDistinct);
            for (ConjunctiveQuery query : negativeQueries)
            {
                ConjunctiveQuery positiveQuery = query.copy();
                positiveQuery.setNegation(false);
                ucq.addQuery(positiveQuery);
            }
            // No need to set result / dist. variables for the UCQ in Boolean engine
            res = _ucqEngine.exec(ucq).isEmpty();
        }
        return res;
    }

    private void cleanUp()
    {
        _changes.revertAll();
        _queryVarsToFreshInds = new HashMap<>();
    }
}
