package openllet.tcq.engine;

import openllet.core.OpenlletOptions;
import openllet.core.utils.Bool;
import openllet.core.utils.Timer;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.shared.tools.Log;
import openllet.tcq.engine.automaton.MLTL2DFA;
import openllet.tcq.model.automaton.DFA;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.tcq.parser.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class TCQEngine extends AbstractQueryEngine<TemporalConjunctiveQuery>
        implements QueryExec<TemporalConjunctiveQuery>
{
    public static final Logger _logger = Log.getLogger(TCQEngine.class);

    private EdgeConstraintChecker _edgeChecker;

    public TCQEngine()
    {
        _booleanEngine = null; // Enforces this engine also as the Boolean engine.
    }

    @Override
    protected QueryResult execABoxQuery(TemporalConjunctiveQuery q)
            throws IOException, InterruptedException
    {
        _logger.fine("Starting entailment check for TCQ " + q);
        String negTcqProp = q.toNegatedPropositionalAbstractionString();
        _logger.finer("Checking DFA satisfiability for negated and propositionally abstracted TCQ " + negTcqProp);
        DFA automaton;
        try
        {
            automaton = MLTL2DFA.convert(negTcqProp, q);
        }
        catch (ParseException e)
        {
            throw new IOException(e.getMessage());
        }
        _edgeChecker = new EdgeConstraintChecker(q, automaton);
        // FIRST RUN - USE CQ ENGINE ONLY
        QueryResult excludeResults = null;
        Map<Boolean, QueryResult> satResult = new HashMap<>();
        if (OpenlletOptions.TCQ_ENGINE_USE_CQ_ENGINE)
        {
            _logger.fine("Trying underapproximating semantics check on DFA");
            Timer t = new Timer();
            t.start();
            _edgeChecker.setUnderapproximatingSemantics(true);
            satResult = _checkDFASatisfiability(automaton, q, null);
            excludeResults = new QueryResultImpl(q);
            for (QueryResult excludeResult : satResult.values())
                for (ResultBinding binding : excludeResult)
                    excludeResults.add(binding);
            t.stop();
            _logger.finer("CQ semantics DFA check returned " + satResult.get(true).size() +
                    " satisfiable and " + satResult.get(false).size() + " unsatisfiable bindings out of " +
                    satResult.get(true).getMaxSize() + " bindings in " + t.getTotal() +  " ms");
        }
        // SECOND RUN - USE CNCQ ENGINE BASED ON RESULTS OF CQ ENGINE
        if (excludeResults == null || !excludeResults.isComplete())
        {
            Timer t = new Timer();
            t.start();
            _edgeChecker.excludeBindings(excludeResults);
            _edgeChecker.setUnderapproximatingSemantics(false);
            _logger.fine("Trying full blown semantics check on DFA");
            satResult = _checkDFASatisfiability(automaton, q, satResult);
            _edgeChecker.doNotExcludeBindings();
            t.stop();
            _logger.finer("Full semantics DFA check returned " + satResult.get(true).size() +
                    " satisfiable and " + satResult.get(false).size() + " unsatisfiable bindings out of " +
                    satResult.get(true).getMaxSize() + " bindings in " + t.getTotal() +  " ms");
        }
        satResult.get(false).explicate();
        return satResult.get(false);
    }

    @Override
    protected QueryResult execABoxQuery(TemporalConjunctiveQuery q, QueryResult excludeBindings,
                                        QueryResult restrictToBindings) throws IOException, InterruptedException
    {
        return execABoxQuery(q);
    }

    /**
     * TODO
     * @param dfa
     * @param tcq
     * @return Guaranteed satisfiability information, i.e. true -> final state has been reached, false -> it is certain
     *  that no final state can be reached
     */
    private Map<Boolean, QueryResult> _checkDFASatisfiability(DFA dfa, TemporalConjunctiveQuery tcq,
                                                              Map<Boolean, QueryResult> knownResults)
            throws IOException, InterruptedException
    {
        Integer initState = dfa.getInitialState();
        DFAExecutableStates states = new DFAExecutableStates(dfa);

        if (initState != null)
            states.add(new DFAExecutableState(dfa, tcq, initState, 0, _edgeChecker, true, _timer));

        _logger.finer("Starting in state " + states);

        // Main loop - execute DFA states until nothing can be fired anymore
        while(states.hasExecutableState())
        {
            DFAExecutableState execState = states.get(0);
            if (execState != null)
                for (DFAExecutableState newState : execState.execute())
                    states.mergeOrAdd(newState);
            states.remove(execState);
        }

        return assembleFinalResult(dfa, tcq, states, knownResults);
    }

    private Map<Boolean, QueryResult> assembleFinalResult(DFA dfa, TemporalConjunctiveQuery tcq,
                                                          DFAExecutableStates states,
                                                          Map<Boolean, QueryResult> knownResults)
    {
        Map<Boolean, QueryResult> result = new HashMap<>();
        if (knownResults == null)
        {
            result.put(false, new QueryResultImpl(tcq));
            result.put(true, new QueryResultImpl(tcq));
        }
        else
        {
            result.put(false, knownResults.get(false));
            result.put(true, knownResults.get(true));
        }

        if (states.size() > 0)
        {
            if (_edgeChecker.isUnderapproximatingSemantics())
            {
                int n = tcq.getTemporalKB().size();
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
            else
            {
                // Finds all satisfiable bindings from some non-final state (potential candidates for rejection)
                QueryResult potentiallyRejectedBindings = new QueryResultImpl(tcq);
                for (DFAExecutableState state : states)
                    if (!dfa.isAccepting(state.getDFAState()) && state.getSatBindings() != null)
                        potentiallyRejectedBindings.addAll(state.getSatBindings());
                // Removes those for which a counterexample was generated (i.e. that were accepted)
                QueryResult counterexampleFound = new QueryResultImpl(tcq);
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
