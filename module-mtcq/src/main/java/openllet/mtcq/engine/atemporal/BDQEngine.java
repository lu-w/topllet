package openllet.mtcq.engine.atemporal;

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.ABoxChanges;
import openllet.core.utils.Bool;
import openllet.core.utils.Timer;
import openllet.mtcq.model.query.*;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.engine.QueryBindingCandidateGenerator;
import openllet.query.sparqldl.engine.QueryResultBasedBindingCandidateGenerator;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.engine.ucq.BooleanUnionQueryEngineSimple;
import openllet.query.sparqldl.engine.ucq.UnionQueryEngineSimple;
import openllet.query.sparqldl.model.AtomQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQueryImpl;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.results.*;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.query.sparqldl.model.ucq.UnionQueryImpl;

import java.util.*;

import static openllet.mtcq.engine.rewriting.MTCQSimplifier.flattenOr;

/**
 * Engine for answering Boolean Disjunctive Queries (BDQs), i.e., a union of (possibly negated) CQs.
 * Required as a non-temporal base case of the {@code MTCQNormalFormEngine}.
 * TODO:
 *  - Handle that case that the given BDQ contains parts that are over different result variables (answer separately)
 */
public class BDQEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    private final UnionQueryEngineSimple _ucqEngine = new UnionQueryEngineSimple();
    private Map<ATermAppl, ATermAppl> _queryVarsToFreshInds = new HashMap<>();
    private ABoxChanges _changes;
    public static long cqCalls = 0;
    public static long cqCandidates = 0;
    public static Timer cqTimer = new Timer();
    public static Timer ucqTimer = new Timer();

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
    public QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings,
                                        QueryResult restrictToBindings)
    {
        List<MetricTemporalConjunctiveQuery> disjuncts;
        if (q instanceof ConjunctiveQueryFormula cq)
        {
            cqCalls++;
            cqCandidates += restrictToBindings.size();
            cqTimer.start();
            QueryResult res = new QueryEngine().exec(cq.getConjunctiveQuery(), excludeBindings, restrictToBindings);
            cqTimer.stop();
            if (res instanceof MultiQueryResults mqr)
                res = mqr.toQueryResultImpl(cq.getConjunctiveQuery());
            return res;
        }
        if (q instanceof OrFormula)
            disjuncts = flattenOr(q);
        else if (q instanceof NotFormula)
            disjuncts = List.of(q);
        else
            throw new RuntimeException("Invalid query for BDQ engine: " + q);

        UnionQuery positiveDisjuncts = new UnionQueryImpl(q.getKB(), q.isDistinct());
        List<ConjunctiveQuery> negativeDisjuncts = new ArrayList<>();
        List<ATermAppl> removeAnswerVars = new ArrayList<>();
        for (MetricTemporalConjunctiveQuery disjunct : disjuncts)
            if (disjunct instanceof ConjunctiveQueryFormula cq)
                positiveDisjuncts.addQuery(cq.getConjunctiveQuery());
            else if (disjunct instanceof NotFormula not && not.getSubFormula() instanceof ConjunctiveQueryFormula cq)
            {
                negativeDisjuncts.add(cq.getConjunctiveQuery());
                removeAnswerVars.addAll(cq.getResultVars());
            }
            else
                throw new RuntimeException("Invalid query for BDQ engine: " + q);
        for (ATermAppl answerVarInNegation : removeAnswerVars)
            positiveDisjuncts.removeResultVar(answerVarInNegation);

        QueryResult result;
        ucqTimer.start();
        if (negativeDisjuncts.isEmpty())
            result = execSemiBooleanBDQ(List.of(), positiveDisjuncts, excludeBindings, restrictToBindings);
        else
        {
            // FETCH AND APPLY BINDINGS TO NEGATIVE PARTS
            result = new QueryResultImpl(q);
            ConjunctiveQuery generatorQuery = new ConjunctiveQueryImpl(q);
            generatorQuery.setResultVars(removeAnswerVars);
            QueryBindingCandidateGenerator _bindingGenerator =
                    new QueryResultBasedBindingCandidateGenerator(generatorQuery);
            _bindingGenerator.excludeBindings(excludeBindings);
            _bindingGenerator.restrictToBindings(restrictToBindings);
            for (ResultBinding candidateBinding : _bindingGenerator)
            {
                List<ConjunctiveQuery> appliedNegativeDisjuncts = new ArrayList<>();
                for (ConjunctiveQuery negativeDisjunct : negativeDisjuncts)
                    appliedNegativeDisjuncts.add(negativeDisjunct.apply(candidateBinding));
                UnionQuery boundPositiveDisjuncts = positiveDisjuncts.apply(candidateBinding);
                QueryResult candidateExcludeBindings;
                // sets correct bindings to exclude for the pos. parts based on the current candidate for the neg. part
                candidateExcludeBindings = new QueryResultImpl(boundPositiveDisjuncts);
                if (excludeBindings != null)
                    candidateExcludeBindings.addAll(excludeBindings.getRestOfPartialBinding(candidateBinding, boundPositiveDisjuncts));
                // enforces distinctness constraint over positive and negative query parts
                if (q.isDistinct())
                    for (ATermAppl var : candidateBinding.getAllVariables())
                        for (ATermAppl answerVar : boundPositiveDisjuncts.getResultVars())
                        {
                            QueryResult restriction = new QueryResultImpl(boundPositiveDisjuncts);
                            ResultBinding binding = new ResultBindingImpl();
                            binding.setValue(answerVar, candidateBinding.getValue(var));
                            restriction.add(binding);
                            candidateExcludeBindings.addAll(restriction);
                        }
                QueryResult partialResult = execSemiBooleanBDQ(appliedNegativeDisjuncts, boundPositiveDisjuncts,
                        candidateExcludeBindings, restrictToBindings);
                partialResult.removeAll(candidateExcludeBindings);  // TODO fix in UCQ engine (candidates are not excluded)
                if (!partialResult.isEmpty())
                {
                    // We may have gotten n > 0 bindings from the semi-Boolean engine, create a copy and merge curr. binding
                    if (candidateBinding.getAllVariables().containsAll(partialResult.getResultVars()) ||
                            Collections.disjoint(candidateBinding.getAllVariables(), partialResult.getResultVars()))
                        for (ResultBinding binding : partialResult)
                        {
                            ResultBinding copyBinding = candidateBinding.duplicate();
                            copyBinding.merge(binding);
                            result.add(copyBinding);
                        }
                        // Result is actually not "partial" at all - it is more specific than our candidate binding
                    else if (partialResult.getResultVars().containsAll(candidateBinding.getAllVariables()))
                        result = partialResult;
                        // Overlapping variable sets (e.g., pos: x,y and neg: y,z) -> can never happen since y has been bound
                        // in the negative part and thus pos should only be over x.
                    else
                        throw new RuntimeException("Got overlapping variable sets for positive and negative part. " +
                                "This should never happen.");
                }
                _bindingGenerator.informAboutResultForBinding(partialResult.isEmpty() ? Bool.FALSE : Bool.TRUE);
            }
            _bindingGenerator.doNotExcludeBindings();
            _bindingGenerator.doNotRestrictToBindings();
        }
        ucqTimer.stop();
        return result;
    }

    /**
     * Executes a semi-Boolean disjunctive query with positive and negative parts, i.e., the negative disjuncts are
     * already Boolean (no variables remain) and the positive disjuncts have to be answered. For this, it puts the
     * negative Boolean disjuncts into the ABox as data axioms.
     * @param negativeBooleanDisjuncts The list of conjunctive queries from the negated part of the BDQ.
     * @param positiveDisjuncts The union of conjunctive queries from the positive part of the BDQ.
     * @param excludeBindings A set of bindings to be excluded from the result.
     * @param restrictToBindings If not null, the answer will be restricted to this set of bindings.
     * @return The query result for the given positive disjuncts.
     */
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
        positiveDisjuncts.setKB(_abox.getKB());

        // 4. CHECK FOR ENTAILMENT
        QueryResult results = computeEntailedBindings(positiveDisjuncts, excludeBindings, restrictToBindings);

        // 5. CLEAN-UP & ROLLING-BACK CHANGES
        cleanUp();

        return results;
    }

    /**
     * Answers the given UCQ. A wrapper around the UCQ engine, but handles consistency checks: If the ABox is
     * inconsistent (e.g. due to negated disjuncts added earlier), considers the query as entailed.
     * Handles the edge cases when no positive query is present or when restrict bindings are given.
     * @param query The union of conjunctive queries to evaluate.
     * @param excludeBindings Bindings to exclude from the result.
     * @param restrictToBindings Bindings to restrict the result to.
     * @return Answers to the UCQ.
     */
    private QueryResult computeEntailedBindings(UnionQuery query, QueryResult excludeBindings,
                                                QueryResult restrictToBindings)
    {
        QueryResult result;
        if (_abox.isConsistent())
        {
            if (!query.isEmpty())
                result = _ucqEngine.exec(query, excludeBindings, restrictToBindings);
            else
            {
                BooleanUnionQueryEngineSimple.calls++;
                result = new QueryResultImpl(query);
            }
        }
        else
        {
            BooleanUnionQueryEngineSimple.calls++;
            if (restrictToBindings == null)
                // If the ABox is inconsistent, we have put query atom from the negative part into the ABox causing this
                // -> an inconsistent knowledge base means that the query is entailed already by the negative disjuncts.
                result = new QueryResultImpl(query).invert();
            else
                result = restrictToBindings.copy();
        }
        return result;
    }

    /**
     * Applies all atoms of a given query into the current ABox, converting them into temporary assertions.
     * Supports only Type and PropertyValue predicates. Used to model negated conjunctive queries by turning their
     * positive form into a test for inconsistency.
     * @param query The query whose atoms should be added to the ABox.
     */
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

    /**
     * Retrieves or creates a fresh individual in the ABox for a given query variable; uses a cache internally.
     * Ensures variables used in negative conjunctive queries are grounded as individuals.
     * @param var The query variable to convert into an individual.
     * @return A fresh or existing individual term corresponding to the query variable.
     */
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

    /**
     * Cleans up variables to fresh individual mapping.
     * Should be called after ABox modifications to ensure the next query is processed cleanly.
     */
    private void cleanUp()
    {
        _queryVarsToFreshInds = new HashMap<>();
    }
}
