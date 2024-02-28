package openllet.mtcq.engine.atemporal;

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.ABoxChanges;
import openllet.core.utils.Bool;
import openllet.mtcq.model.query.ConjunctiveQueryFormula;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.mtcq.model.query.NotFormula;
import openllet.mtcq.model.query.OrFormula;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.engine.QueryBindingCandidateGenerator;
import openllet.query.sparqldl.engine.QueryResultBasedBindingCandidateGenerator;
import openllet.query.sparqldl.engine.cq.CombinedQueryEngine;
import openllet.query.sparqldl.engine.ucq.UnionQueryEngineSimple;
import openllet.query.sparqldl.model.AtomQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.query.sparqldl.model.ucq.UnionQueryImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static openllet.mtcq.engine.rewriting.MTCQSimplifier.flattenOr;

public class BDQEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    private UnionQueryEngineSimple _ucqEngine = new UnionQueryEngineSimple();
    private Map<ATermAppl, ATermAppl> _queryVarsToFreshInds = new HashMap<>();
    private ABoxChanges _changes;

    @Override
    public boolean supports(MetricTemporalConjunctiveQuery q)
    {
        if (q instanceof OrFormula qOr)
        {
            for (MetricTemporalConjunctiveQuery disjunct : flattenOr(qOr))
                if (!(disjunct instanceof NotFormula not && not.getSubFormula() instanceof ConjunctiveQueryFormula) &&
                        !(disjunct instanceof ConjunctiveQueryFormula))
                    return false;
            return true;
        }
        else
            return (q instanceof NotFormula not && not.getSubFormula() instanceof ConjunctiveQueryFormula) ||
                    (q instanceof ConjunctiveQueryFormula);
    }

    @Override
    protected QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings,
                                        QueryResult restrictToBindings)
    {
        List<MetricTemporalConjunctiveQuery> disjuncts;
        if (q instanceof ConjunctiveQueryFormula cq)
            return new CombinedQueryEngine().exec(cq.getConjunctiveQuery());
        if (q instanceof OrFormula)
            disjuncts = flattenOr(q);
        else if (q instanceof NotFormula)
            disjuncts = List.of(q);
        else
            throw new RuntimeException("Invalid query for BDQ engine: " + q);

        UnionQuery positiveDisjuncts = new UnionQueryImpl(q.getKB(), q.isDistinct());
        List<ConjunctiveQuery> negativeDisjuncts = new ArrayList<>();
        for (MetricTemporalConjunctiveQuery disjunct : disjuncts)
            if (disjunct instanceof ConjunctiveQueryFormula cq)
                positiveDisjuncts.addQuery(cq.getConjunctiveQuery());
            else if (disjunct instanceof NotFormula not && not.getSubFormula() instanceof ConjunctiveQueryFormula cq)
                negativeDisjuncts.add(cq.getConjunctiveQuery());
            else
                throw new RuntimeException("Invalid query for BDQ engine: " + q);

        QueryResult result;
        if (negativeDisjuncts.isEmpty())
            result = execSemiBooleanBDQ(List.of(), positiveDisjuncts, excludeBindings, restrictToBindings);
        else
        {
            // FETCH AND APPLY BINDINGS TO NEGATIV PARTS
            result = new QueryResultImpl(q);
            QueryBindingCandidateGenerator _bindingGenerator =
                    new QueryResultBasedBindingCandidateGenerator(negativeDisjuncts.get(0));  // TODO check if 0 has all vars
            _bindingGenerator.excludeBindings(excludeBindings);
            _bindingGenerator.restrictToBindings(restrictToBindings);
            for (ResultBinding candidateBinding : _bindingGenerator)
            {
                List<ConjunctiveQuery> appliedNegativeDisjuncts = new ArrayList<>();
                for (ConjunctiveQuery negativeDisjunct : negativeDisjuncts)
                    appliedNegativeDisjuncts.add(negativeDisjunct.apply(candidateBinding));
                QueryResult partialResult = execSemiBooleanBDQ(appliedNegativeDisjuncts, positiveDisjuncts,
                        excludeBindings, restrictToBindings);
                // We may have gotten n > 0 bindings from the semi-Boolean engine, create a copy and merge curr. binding
                for (ResultBinding binding : partialResult)
                {
                    ResultBinding copyBinding = candidateBinding.duplicate();
                    copyBinding.merge(binding);
                    result.add(copyBinding);
                }
                _bindingGenerator.informAboutResultForBinding(partialResult.isEmpty() ? Bool.FALSE : Bool.TRUE);
            }
            _bindingGenerator.doNotExcludeBindings();
            _bindingGenerator.doNotRestrictToBindings();
        }
        return result;
    }

    private QueryResult execSemiBooleanBDQ(List<ConjunctiveQuery> negativeBooleanDisjuncts,
                                           UnionQuery positiveDisjuncts, QueryResult excludeBindings,
                                           QueryResult restrictToBindings)
    {
        // 1. PRELIMINARY CONSISTENCY CHECK
        positiveDisjuncts.getKB().ensureConsistency();

        _changes = new ABoxChanges(positiveDisjuncts.getKB().getABox());
        _abox = _changes.getABox();

        // 3. PUT NEGATIVE BOOLEAN ATOMS IN A-BOX
        for (ConjunctiveQuery negativeBooleanDisjunct : negativeBooleanDisjuncts)
            putQueryAtomsInABox(negativeBooleanDisjunct);

        // 4. CHECK FOR ENTAILMENT
        QueryResult results = computeEntailedBindings(positiveDisjuncts, excludeBindings, restrictToBindings);

        // 5. CLEAN-UP & ROLLING-BACK CHANGES
        cleanUp();

        return results;
    }

    private QueryResult computeEntailedBindings(UnionQuery query, QueryResult excludeBindings,
                                                QueryResult restrictToBindings)
    {
        if (_abox.isConsistent())
            return _ucqEngine.exec(query, excludeBindings, restrictToBindings);
        else
            // If the ABox is inconsistent, we have put query atom from the negative part into the ABox causing this
            // -> an inconsistent knowledge base means that the query is entailed already by the negative disjuncts.
            return new QueryResultImpl(query).invert();
    }

    private void putQueryAtomsInABox(AtomQuery<?> query)
    {
        for (QueryAtom atom : query.getAtoms())
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
                default -> _logger.warning("Encountered query predicate that is not supported: " +
                        atom.getPredicate());
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

    private void cleanUp()
    {
        _queryVarsToFreshInds = new HashMap<>();
    }
}
