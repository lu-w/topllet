package openllet.query.sparqldl.engine.cncq;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.abox.ABox;
import openllet.query.sparqldl.engine.AbstractBooleanQueryEngine;
import openllet.query.sparqldl.engine.ucq.UnionQueryEngineSimple;
import openllet.query.sparqldl.model.AtomQuery;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.query.sparqldl.model.ucq.UnionQueryImpl;

import java.util.List;

/**
 * This engine checks for the *satisfiability* of conjunctions of possibly negated conjunctive queries.
 */
public class BooleanCNCQQueryEngineSimple extends AbstractBooleanQueryEngine<CNCQQuery>
{
    private UnionQueryEngineSimple _ucqEngine = new UnionQueryEngineSimple();
    private boolean _rollUpBeforeChecking = false;

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
        KnowledgeBase modifiedKB = putQueryAtomsInABox(positiveQuery, q.getKB());

        // 4. CHECK FOR SATISFIABILITY
        return isSatisfied(negativeQueries, modifiedKB, q.isDistinct());
    }

    private KnowledgeBase putQueryAtomsInABox(AtomQuery<?> query, KnowledgeBase kb)
    {
        final ABox copy = kb.getABox().copy();
        for (QueryAtom atom : query.getAtoms())
        {
            switch (atom.getPredicate())
            {
                case Type ->
                {
                    ATermAppl var = atom.getArguments().get(0);
                    ATermAppl type = atom.getArguments().get(1);
                    if (!kb.isIndividual(var))
                        var = copy.addFreshIndividual(null, DependencySet.INDEPENDENT).getTerm();
                    copy.addType(var, type);
                }
                case PropertyValue ->
                {
                    ATermAppl subj = atom.getArguments().get(0);
                    ATermAppl pred = atom.getArguments().get(1);
                    ATermAppl obj = atom.getArguments().get(2);
                    if (!kb.isIndividual(subj))
                        subj = copy.addFreshIndividual(null, DependencySet.INDEPENDENT).getTerm();
                    if (!kb.isIndividual(obj))
                        obj = copy.addFreshIndividual(null, DependencySet.INDEPENDENT).getTerm();
                    copy.addEdge(pred, subj, obj, DependencySet.INDEPENDENT);
                }
                default -> _logger.warning("Encountered query predicate that is not supported: " + atom.getPredicate());
            }
        }
        return copy.getKB();
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
        // no need to set result / dist. variables for the UCQ in Boolean engine
        return _ucqEngine.exec(ucq).isEmpty();
    }
}
