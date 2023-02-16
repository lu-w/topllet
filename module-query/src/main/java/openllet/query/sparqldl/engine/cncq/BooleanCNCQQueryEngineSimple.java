package openllet.query.sparqldl.engine.cncq;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Individual;
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

    @Override
    protected boolean execBooleanABoxQuery(CNCQQuery q)
    {
        // 1. PRELIMINARY CONSISTENCY CHECK
        q.getKB().ensureConsistency();

        // 2. SEPARATE POSITIVE AND NEGATIVE PART & MERGE POSITIVE PART
        ConjunctiveQuery positiveQuery = q.mergePositiveQueries();
        List<ConjunctiveQuery> negativeQueries = q.getNegativeQueries();

        // 2. SPLIT & ROLL-UP POSITIVE QUERIES (OPTIONAL)
        if (_rollUpBeforeChecking)
            positiveQuery = positiveQuery.splitAndRollUp(false);

        // 3. PUT POSITIVE ATOMS IN A-BOX
        ABox modifiedABox = putQueryAtomsInABox(positiveQuery, q.getKB());

        // 4. QUERY IS NOT SATISFIABLE IF KB IS INCONSISTENT
        if (!modifiedABox.isConsistent())
            return false;

        // 5. CHECK FOR SATISFIABILITY
        boolean isSat = isSatisfied(negativeQueries, modifiedABox.getKB(), q.isDistinct());

        // 6. CLEAN-UP
        cleanUp();

        return isSat;
    }

    private ABox putQueryAtomsInABox(AtomQuery<?> query, KnowledgeBase kb)
    {
        final ABox copy = kb.getABox().copy();
        for (QueryAtom atom : query.getAtoms())
        {
            switch (atom.getPredicate())
            {
                case Type ->
                {
                    ATermAppl var = getIndividual(atom.getArguments().get(0), copy);
                    ATermAppl type = atom.getArguments().get(1);
                    copy.addType(var, type);
                }
                case PropertyValue ->
                {
                    ATermAppl subj = getIndividual(atom.getArguments().get(0), copy);
                    ATermAppl pred = atom.getArguments().get(1);
                    ATermAppl obj = getIndividual(atom.getArguments().get(2), copy);
                    copy.addEdge(pred, subj, obj, DependencySet.INDEPENDENT);
                }
                default -> _logger.warning("Encountered query predicate that is not supported: " + atom.getPredicate());
            }
        }
        return copy;
    }

    private ATermAppl getIndividual(ATermAppl var, ABox abox)
    {
        ATermAppl res = var;
        if (!abox.getKB().isIndividual(var))
        {
            if (!_queryVarsToFreshInds.containsKey(var))
                _queryVarsToFreshInds.put(var, // TODO Lukas: null here?
                        abox.addFreshIndividual(null, DependencySet.INDEPENDENT).getTerm());
            res = _queryVarsToFreshInds.get(var);
        }
        return res;
    }

    private boolean isSatisfied(List<ConjunctiveQuery> negativeQueries, KnowledgeBase kb, boolean isDistinct)
    {
        UnionQuery ucq = new UnionQueryImpl(kb, isDistinct);
        for (ConjunctiveQuery query : negativeQueries)
        {
            ConjunctiveQuery positiveQuery = query.copy();
            positiveQuery.setNegation(false);
            ucq.addQuery(positiveQuery);
        }
        System.out.println(ucq);
        // No need to set result / dist. variables for the UCQ in Boolean engine
        return _ucqEngine.exec(ucq).isEmpty();
    }

    private void cleanUp()
    {
        _queryVarsToFreshInds = new HashMap<>();
    }
}
