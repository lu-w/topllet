package openllet.query.sparqldl.engine.cncq;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Individual;
import openllet.core.utils.ATermUtils;
import openllet.query.sparqldl.engine.AbstractBooleanQueryEngine;
import openllet.query.sparqldl.engine.ucq.UnionQueryEngineSimple;
import openllet.query.sparqldl.model.AtomQuery;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.query.sparqldl.model.ucq.UnionQueryImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This engine checks for the *satisfiability* of conjunctions of possibly negated conjunctive queries.
 */
public class BooleanCNCQQueryEngineSimple extends AbstractBooleanQueryEngine<CNCQQuery>
{
    // TODO Lukas: Change classes should have apply() and revert() functions and a pointer to their KB
    private abstract static class Change
    {
    }
    private static class TypeChange extends Change
    {
        private final ATermAppl _ind;
        private final ATermAppl _type;

        TypeChange(ATermAppl ind, ATermAppl type)
        {
            _ind = ind;
            _type = type;
        }

        private ATermAppl getInd()
        {
            return _ind;
        }

        private ATermAppl getType()
        {
            return _type;
        }

        @Override
        public String toString() {
            return _type + "(" + _type + ")";
        }
    }
    private class PropertyChange extends Change
    {
        private final ATermAppl _subj;
        private final ATermAppl _pred;
        private final ATermAppl _obj;

        PropertyChange(ATermAppl subj, ATermAppl pred, ATermAppl obj)
        {
            _subj = subj;
            _pred = pred;
            _obj = obj;
        }

        private ATermAppl getSubj()
        {
            return _subj;
        }

        private ATermAppl getPred()
        {
            return _pred;
        }

        private ATermAppl getObj()
        {
            return _obj;
        }

        @Override
        public String toString() {
            return _pred + "(" + _subj + ", " + _obj + ")";
        }
    }

    private static class FreshIndChange extends Change
    {
        private final ATermAppl _ind;

        FreshIndChange(ATermAppl ind)
        {
            _ind = ind;
        }

        private ATermAppl getInd()
        {
            return _ind;
        }

        @Override
        public String toString() {
            return _ind.toString();
        }
    }

    private UnionQueryEngineSimple _ucqEngine = new UnionQueryEngineSimple();
    private boolean _rollUpBeforeChecking = false;
    private Map<ATermAppl, ATermAppl> _queryVarsToFreshInds = new HashMap<>();
    private List<Change> _changes = new ArrayList<>();
    private ABox _abox = null;
    private int _freshIndCounter = 0;

    @Override
    protected boolean execBooleanABoxQuery(CNCQQuery q)
    {
        // 1. PRELIMINARY CONSISTENCY CHECK
        q.getKB().ensureConsistency();
        _abox = q.getKB().getABox();

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
                    ATermAppl var = getIndividual(atom.getArguments().get(0), _abox);
                    ATermAppl type = atom.getArguments().get(1);
                    _abox.addType(var, type);
                    _changes.add(new TypeChange(var, type));
                }
                case PropertyValue ->
                {
                    ATermAppl subj = getIndividual(atom.getArguments().get(0), _abox);
                    ATermAppl pred = atom.getArguments().get(1);
                    ATermAppl obj = getIndividual(atom.getArguments().get(2), _abox);
                    _abox.addEdge(pred, subj, obj, DependencySet.INDEPENDENT);
                    _changes.add(new PropertyChange(subj, pred, obj));
                }
                default -> _logger.warning("Encountered query predicate that is not supported: " + atom.getPredicate());
            }
        }
    }

    private ATermAppl getIndividual(ATermAppl var, ABox abox)
    {
        ATermAppl res = var;
        if (!abox.getKB().isIndividual(var))
        {
            if (!_queryVarsToFreshInds.containsKey(var))
            {
                // TODO Lukas: create some method that safely fetches a fresh individual name given a prefix
                Individual ind = abox.getKB().addIndividual(ATermUtils.makeTermAppl("__NEW_CNCQ_IND_" + _freshIndCounter));
                ATermAppl freshInd = ind.getTerm();
                _freshIndCounter++;
                _queryVarsToFreshInds.put(var, freshInd);
                _changes.add(new FreshIndChange(freshInd));
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
        // Sorting because we want to delete individuals at the very latest
        _changes.sort((c1, c2) -> c1 instanceof FreshIndChange && !(c2 instanceof FreshIndChange) ? 1 :
                !(c1 instanceof FreshIndChange) && c2 instanceof FreshIndChange ? -1 : 0);
        for (Change change : _changes)
            if (change instanceof TypeChange c)
                _abox.getKB().removeType(c.getInd(), c.getType());
            else if (change instanceof PropertyChange c)
                _abox.getKB().removePropertyValue(c.getPred(), c.getSubj(), c.getObj());
            else if (change instanceof FreshIndChange c)
                _abox.removeNode(c.getInd());
        _queryVarsToFreshInds = new HashMap<>();
        _changes = new ArrayList<>();
    }
}
