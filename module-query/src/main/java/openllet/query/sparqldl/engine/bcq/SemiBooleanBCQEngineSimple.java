package openllet.query.sparqldl.engine.bcq;

import openllet.aterm.ATermAppl;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.ABoxChanges;
import openllet.query.sparqldl.engine.cq.CombinedQueryEngine;
import openllet.query.sparqldl.engine.ucq.UnionQueryEngineSimple;
import openllet.query.sparqldl.model.AtomQuery;
import openllet.query.sparqldl.model.bcq.BCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.results.*;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.query.sparqldl.model.ucq.UnionQueryImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SemiBooleanBCQEngineSimple extends AbstractSemiBooleanBCQEngine
{
    private UnionQueryEngineSimple _ucqEngine = new UnionQueryEngineSimple();
    private Map<ATermAppl, ATermAppl> _queryVarsToFreshInds = new HashMap<>();
    private ABoxChanges _changes;

    public SemiBooleanBCQEngineSimple()
    {
        super();
    }

    public SemiBooleanBCQEngineSimple(UnionQueryEngineSimple ucqEngine)
    {
        this();
        _ucqEngine = ucqEngine;
    }

    @Override
    protected QueryResult execABoxQuery(BCQQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
            throws IOException, InterruptedException
    {
        QueryResult satResult = new QueryResultImpl(q);

        // 1. PRELIMINARY CONSISTENCY CHECK
        q.getKB().ensureConsistency();

        // If there are 0 negated subqueries in q, we can check for entailment: if q is entailed, then it is satisfiable
        if (OpenlletOptions.BCQ_ENGINE_USE_CQ_ENTAILMENT_AS_SUFFICIENT_CONDITION && q.getNegativeQueries().size() == 0)
        {
            boolean isEntailed = true;
            for (ConjunctiveQuery cq : q.getPositiveQueries())
                isEntailed &= !(new CombinedQueryEngine().exec(cq).isEmpty());
            if (isEntailed)
            {
                satResult.add(new ResultBindingImpl());
                return satResult;
            }
        }

        _changes = new ABoxChanges(q.getKB().getABox());
        _abox = _changes.getABox();

        // 2. SEPARATE POSITIVE AND NEGATIVE PART & MERGE POSITIVE PART
        ConjunctiveQuery positiveQuery = q.mergePositiveQueries();

        // 2. SPLIT & ROLL-UP POSITIVE QUERIES (OPTIONAL)
        if (OpenlletOptions.BCQ_ENGINE_ROLL_UP_POSITIVE_PART_BEFORE_CHECKING)
            positiveQuery = positiveQuery.splitAndRollUp(false);

        // 3. PUT POSITIVE ATOMS IN A-BOX
        putQueryAtomsInABox(positiveQuery);

        // 4. CHECK FOR SATISFIABILITY
        satResult = computeSatisfiableBindings(q, excludeBindings, restrictToBindings);

        // 5. CLEAN-UP & ROLLING-BACK CHANGES
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
                _queryVarsToFreshInds.put(var, change.getInd());
            }
            res = _queryVarsToFreshInds.get(var);
        }
        return res;
    }

    private QueryResult computeSatisfiableBindings(BCQQuery query, QueryResult excludeBindings,
                                                   QueryResult restrictToBindings)
            throws IOException, InterruptedException
    {
        QueryResult res = new QueryResultImpl(query);
        if (_abox.isConsistent())
        {
            UnionQuery ucq = new UnionQueryImpl(_abox.getKB(), query.isDistinct());
            for (ConjunctiveQuery negQuery : query.getNegativeQueries())
            {
                ConjunctiveQuery positiveQuery = negQuery.copy();
                positiveQuery.setKB(ucq.getKB());
                positiveQuery.setNegation(false);
                ucq.addQuery(positiveQuery);
            }
            // If UCQ is empty, we only need to check satisfiability of negated parts wrt. the KB. Therefore,
            // we have to assume that all possible bindings are satisfiable (which is later done by inverting the empty
            // results binding).
            if (!ucq.isEmpty())
            {
                res = _ucqEngine.exec(ucq, excludeBindings, restrictToBindings);
                if (res instanceof MultiQueryResults mqr)
                    res = mqr.toQueryResultImpl(query);
            }
            res = res.invert();
            if (!query.getResultVars().isEmpty())
            {
                // If we invert, we may include bindings that we should have excluded. If explicitly do so again.
                res.removeAll(excludeBindings);
                // If we invert, we need to restrict to bindings again.
                res.retainAll(restrictToBindings);
            }
        }
        // If ABox is inconsistent, we have put a positive atom from the BCQ to the KB that lead to inconsistency
        //  -> the query is not entailed and we return the empty query result.
        return res;
    }

    private void cleanUp()
    {
        _queryVarsToFreshInds = new HashMap<>();
    }
}
