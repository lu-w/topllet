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

    private static class Changes
    {
        private List<Change> _changes = new ArrayList<>();

        private final ABox _abox;

        Changes(ABox abox)
        {
            _abox = abox;
        }

        private void apply(Change change)
        {
            change.setABox(_abox);
            _changes.add(change);
            change.apply();
        }

        private void revertAll()
        {
            // Sorting because we want to delete individuals at the very latest
            _changes.sort((c1, c2) -> c1 instanceof FreshIndChange && !(c2 instanceof FreshIndChange) ? 1 :
                    !(c1 instanceof FreshIndChange) && c2 instanceof FreshIndChange ? -1 : 0);
            for (Change change : _changes)
                change.revert();
            _changes = new ArrayList<>();
        }
    }

    private abstract static class Change
    {
        protected ABox _abox;

        protected void setABox(ABox abox)
        {
            _abox = abox;
        }

        protected ABox getABox()
        {
            return _abox;
        }

        protected abstract void revert();
        protected abstract void apply();
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

        @Override
        public String toString() {
            return _type + "(" + _type + ")";
        }

        @Override
        protected void revert()
        {
            _abox.getKB().removeType(_ind, _type);
        }

        @Override
        protected void apply()
        {
            _abox.addType(_ind, _type);
        }
    }

    private static class PropertyChange extends Change
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

        @Override
        public String toString() {
            return _pred + "(" + _subj + ", " + _obj + ")";
        }

        @Override
        protected void revert()
        {
            _abox.getKB().removePropertyValue(_pred, _subj, _obj);
        }

        @Override
        protected void apply()
        {
            _abox.addEdge(_pred, _subj, _obj, DependencySet.INDEPENDENT);
        }
    }

    private static class FreshIndChange extends Change
    {
        private Individual _ind = null;
        private static int _freshIndCounter = 0;

        FreshIndChange() { }

        @Override
        public String toString() {
            return "FreshInd(" + _ind.toString() + ")";
        }

        /**
         * @return the fresh individual if the change is applied and null otherwise.
         */
        private Individual getInd()
        {
            return _ind;
        }

        @Override
        protected void revert()
        {
            _abox.removeNode(_ind.getTerm());
        }

        @Override
        protected void apply()
        {
            ATermAppl newName;
            StringBuilder prefix = new StringBuilder();
            // Safely creates new individuals by prepending "_" until no collision is found
            do
                newName = ATermUtils.makeTermAppl(prefix.append("_") + "NEW_IND_" + _freshIndCounter);
            while (_abox.getKB().getIndividuals().contains(newName));
            _ind = _abox.getKB().addIndividual(newName);
            _freshIndCounter++;
        }
    }

    BooleanCNCQQueryEngineSimple()
    {
        super();
    }

    BooleanCNCQQueryEngineSimple(boolean rollUpBeforeChecking)
    {
        this();
        _rollUpBeforeChecking = rollUpBeforeChecking;
    }

    BooleanCNCQQueryEngineSimple(UnionQueryEngineSimple ucqEngine)
    {
        this();
        _ucqEngine = ucqEngine;
    }

    BooleanCNCQQueryEngineSimple(UnionQueryEngineSimple ucqEngine, boolean rollUpBeforeChecking)
    {
        this();
        _ucqEngine = ucqEngine;
        _rollUpBeforeChecking = rollUpBeforeChecking;
    }

    private UnionQueryEngineSimple _ucqEngine = new UnionQueryEngineSimple();
    private boolean _rollUpBeforeChecking = false;
    private Map<ATermAppl, ATermAppl> _queryVarsToFreshInds = new HashMap<>();
    private Changes _changes;
    private ABox _abox = null;

    @Override
    protected boolean execBooleanABoxQuery(CNCQQuery q)
    {
        // 1. PRELIMINARY CONSISTENCY CHECK & INITIALIZING VARIABLES
        q.getKB().ensureConsistency();
        _abox = q.getKB().getABox();
        _changes = new Changes(_abox);

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
