package openllet.mtcq.engine;

import openllet.core.OpenlletOptions;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.shared.tools.Log;
import openllet.mtcq.engine.automaton.MLTL2DFA;
import openllet.mtcq.model.automaton.DFA;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;

import java.util.*;
import java.util.logging.Logger;

/**
 * Query engine for answering non-Boolean metric temporal conjunctive queries.
 * It basically steps through the FA corresponding to the given formula and propagates (un)satisfiability knowledge
 * along the breadth-first state traversal.
 * As an optimization, it uses CQ answering in a pre-processing run to efficiently generate (partial)
 * (un)satisfiability knowledge.
 */
public class MTCQEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
        implements QueryExec<MetricTemporalConjunctiveQuery>
{
    public static final Logger _logger = Log.getLogger(MTCQEngine.class);

    private EdgeConstraintChecker _edgeChecker;

    public MTCQEngine()
    {
        _booleanEngine = null; // Enforces this engine also as the Boolean engine.
    }

    /**
     * Answers the given query on its temporal knowledge base.
     * @param q The query to answer
     * @return The set of certain answers
     */
    @Override
    protected QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q)
    {
        _logger.fine("Starting entailment check for MTCQ " + q);
        String negMtcqProp = q.toNegatedPropositionalAbstractionString();
        _logger.finer("Checking DFA satisfiability for negated and propositionally abstracted MTCQ " + negMtcqProp);
        DFA automaton;
        automaton = MLTL2DFA.convert(negMtcqProp, q);
        _edgeChecker = new EdgeConstraintChecker(q, automaton);
        // FIRST RUN - USE CQ ENGINE ONLY
        QueryResult excludeResults = null;
        Map<Boolean, QueryResult> satResult = new HashMap<>();
        int cqResultNumber = 0;
        long timeFirstRun = 0;
        if (OpenlletOptions.MTCQ_ENGINE_USE_CQ_ENGINE)
        {
            _logger.fine("Trying underapproximating semantics check on DFA");
            _edgeChecker.setUnderapproximatingSemantics(true);
            satResult = _checkDFASatisfiability(automaton, q, null);
            excludeResults = new QueryResultImpl(q);
            for (QueryResult excludeResult : satResult.values())
                for (ResultBinding binding : excludeResult)
                    excludeResults.add(binding);
            cqResultNumber = satResult.get(false).size();
            if (_timer != null)
            {
                timeFirstRun = _timer.getTotal();
                _logger.fine("CQ semantics DFA check took " + timeFirstRun + " ms");
            }
            _logger.fine(String.format("CQ semantics DFA check returned %.6f %% (",
                    (double) 100 * (satResult.get(true).size() + satResult.get(false).size()) /
                            satResult.get(true).getMaxSize())
                    + satResult.get(true).size() +  " satisfiable and " + satResult.get(false).size() +
                    " unsatisfiable bindings out of " + satResult.get(true).getMaxSize() + " bindings)");
        }
        // SECOND RUN - USE BCQ ENGINE BASED ON RESULTS OF CQ ENGINE
        if (excludeResults == null || !excludeResults.isComplete())
        {
            if (OpenlletOptions.MTCQ_ENGINE_USE_INCREMENTAL_LOADING)
                q.getTemporalKB().resetLoader();
            _edgeChecker.excludeBindings(excludeResults);
            _edgeChecker.setUnderapproximatingSemantics(false);
            _logger.fine("Trying full blown semantics check on DFA");
            satResult = _checkDFASatisfiability(automaton, q, satResult);
            _logger.fine(_edgeChecker.getSatisfiabilityKnowledgeManager().getStats());
            if (_timer != null)
                _logger.fine("Full semantics DFA check took " + (_timer.getTotal() - timeFirstRun) +  " ms");
            _logger.fine(String.format("Full semantics DFA check returned %.6f %% (",
                    (double) 100 * (satResult.get(true).size() + satResult.get(false).size()) /
                            satResult.get(true).getMaxSize())
                    + satResult.get(true).size() +  " satisfiable and " + satResult.get(false).size() +
                    " unsatisfiable bindings out of " + satResult.get(true).getMaxSize() + " bindings)");
        }
        double cqResultRatio = 100.0;
        if (!satResult.get(false).isEmpty())
            cqResultRatio = 100.0 * ((double) cqResultNumber / satResult.get(false).size());
        _logger.fine(String.format("CQ semantics check returned a definite answer on %.6f %% of the entailed" +
                " bindings", cqResultRatio));
        satResult.get(false).explicate();
        return satResult.get(false);
    }

    /**
     * Answers the given query on its temporal knowledge base.
     * @param q The query to answer
     * @param excludeBindings ignored
     * @return The set of certain answers
     */
    @Override
    protected QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings,
                                        QueryResult restrictToBindings)
    {
        return execABoxQuery(q);
    }

    /**
     * Implements the satisfiability check for a MTCQ that is represented by the given automaton. It represents the core
     * (i.e., Algorithm 1 in the supplementary paper) of the answering process.
     * @param dfa The automaton representing the MTCQ.
     * @param mtcq The MTCQ to check satisfiability for.
     * @return Guaranteed satisfiability information, i.e., true -> final state has been reached, false -> it is certain
     *  that no final state can be reached
     */
    private Map<Boolean, QueryResult> _checkDFASatisfiability(DFA dfa, MetricTemporalConjunctiveQuery mtcq,
                                                              Map<Boolean, QueryResult> knownResults)
    {
        Integer initState = dfa.getInitialState();
        DFAExecutableStates states = new DFAExecutableStates(dfa);

        if (initState != null)
            states.add(new DFAExecutableState(dfa, mtcq, initState, 0, _edgeChecker, true, _timer));

        _logger.finer("Starting in state " + states);

        // Main loop - execute DFA states until nothing can be fired anymore
        // Note that implicitly, we perform a breadth-first search.
        // We iterate as long as there is a state with a time point <= n
        while(states.hasExecutableState())
        {
            DFAExecutableState execState = states.get(0);
            if (execState != null)
            {
                _logger.finer("Executing state " + execState.getDFAState() + " @ t = " + execState.getTimePoint());
                // We execute the selected state, which creates a list of new states to consider.
                for (DFAExecutableState newState : execState.execute())
                {
                    _logger.finer("Adding/merging state " + newState.getDFAState() + " @ t = "
                            + newState.getTimePoint());
                    // Those states are then merged or added to the already existing states. Merge needs to happen in
                    // case the time point and state combination is already represented. Then, bindings are merged.
                    states.mergeOrAdd(newState);
                }
            }
            states.remove(execState);
        }

        return assembleFinalResult(dfa, mtcq, states, knownResults);
    }

    /**
     * Assembles the final result after the satisfiability check on the DFA is finished. Handles both the case of having
     * the under-approximating semantics and the full-semantics check beforehand.
     * @param dfa The DFA that corresponds to the given MTCQ
     * @param mtcq The MTCQ that has been checked for satisfiability
     * @param states The states at time point n+1 that can not be executed anymore
     * @param knownResults Possible results from a prior iteration that are already known. Can be null.
     * @return A map from true to all bindings that are satisfiable and false to all bindings that are unsatisfiable
     */
    private Map<Boolean, QueryResult> assembleFinalResult(DFA dfa, MetricTemporalConjunctiveQuery mtcq,
                                                          DFAExecutableStates states,
                                                          Map<Boolean, QueryResult> knownResults)
    {
        _logger.fine("Assembling final result of DFA check");

        // Initializes result to be returned later, depending on previously known results
        Map<Boolean, QueryResult> result = new HashMap<>();
        if (knownResults == null || (knownResults.get(false) == null && knownResults.get(true) == null))
        {
            result.put(false, new QueryResultImpl(mtcq));
            result.put(true, new QueryResultImpl(mtcq));
        }
        else if (knownResults.get(false) == null && knownResults.get(true) != null)
        {
            result.put(false, new QueryResultImpl(mtcq));
            result.put(true, knownResults.get(true));
        }
        else if (knownResults.get(true) == null && knownResults.get(false) != null)
        {
            result.put(false, knownResults.get(false));
            result.put(true, new QueryResultImpl(mtcq));
        }
        else
        {
            result.put(false, knownResults.get(false));
            result.put(true, knownResults.get(true));
        }

        if (!states.isEmpty())
        {
            // Case 1: Under-approximating run (we need to consider unsatisfiability information)
            if (_edgeChecker.isUnderapproximatingSemantics())
            {
                int n = mtcq.getTemporalKB().size();
                // Assemble final result - all bindings in final states satisfy the DFA, and all bindings in none final
                // states do not. Note: there may be bindings not considered here (i.e., result unknown)
                // Note 2: we can only for unsat for all final states if all actual DFA final states were examined
                QueryResult unsatForAllFinalStates = null;
                boolean canInferFromUnsat = states.coversDFAFinalStatesCompletely(n);
                // Optimization: Unsatisfiability for all final states has to be checked only for states reachable in
                //  n steps. Other final states can be excluded safely.
                Collection<Integer> reachableFinalStates = dfa.getStatesReachableInNSteps(n);
                for (DFAExecutableState state : states)
                    if (dfa.isAccepting(state.getDFAState()) && reachableFinalStates.contains(state.getDFAState()))
                    {
                        if (state.getSatBindings() != null)
                            result.get(true).addAll(state.getSatBindings());
                        if (canInferFromUnsat)
                        {
                            if (state.getUnsatBindings() == null)
                            {
                                unsatForAllFinalStates = null;
                                break;
                            }
                            if (unsatForAllFinalStates == null)
                                unsatForAllFinalStates = state.getUnsatBindings();
                            else
                                unsatForAllFinalStates.retainAll(state.getUnsatBindings());
                        }
                    }
                if (unsatForAllFinalStates != null)
                    result.get(false).addAll(unsatForAllFinalStates);
            }
            // Case 2: Full-semantics run (satisfiability information is sufficient to derive acceptance)
            else
            {
                // Finds all satisfiable bindings from some non-final state (potential candidates for rejection)
                QueryResult potentiallyRejectedBindings = new QueryResultImpl(mtcq);
                for (DFAExecutableState state : states)
                    if (!dfa.isAccepting(state.getDFAState()) && state.getSatBindings() != null)
                        potentiallyRejectedBindings.addAll(state.getSatBindings());
                // Removes those for which a counterexample was generated (i.e. that were accepted)
                QueryResult counterexampleFound = new QueryResultImpl(mtcq);
                for (ResultBinding potentiallyRejectedBinding : potentiallyRejectedBindings)
                    for (DFAExecutableState state : states)
                        if (dfa.isAccepting(state.getDFAState()))
                            if (state.getSatBindings() != null &&
                                    state.getSatBindings().contains(potentiallyRejectedBinding))
                            {
                                counterexampleFound.add(potentiallyRejectedBinding);
                                break;
                            }
                potentiallyRejectedBindings.removeAll(counterexampleFound);
                result.get(false).addAll(potentiallyRejectedBindings);
                result.put(true, result.get(false).invert());
            }
        }

        return result;
    }
}
