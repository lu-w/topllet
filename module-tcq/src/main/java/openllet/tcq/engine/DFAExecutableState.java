package openllet.tcq.engine;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Bool;
import openllet.core.utils.Timer;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.tcq.model.automaton.DFA;
import openllet.tcq.model.automaton.Edge;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import java.io.IOException;
import java.util.*;

public class DFAExecutableState
{
    private final int _dfaState;
    private final DFA _dfa;
    private final TemporalConjunctiveQuery _tcq;
    private QueryResult _satBindings;
    private QueryResult _unsatBindings;
    private final int _timePoint;
    private final int _maxTimePoint;
    private final KnowledgeBase _kb;
    private final EdgeConstraintChecker _edgeChecker;
    private boolean _hasUncheckedBindingCandidates = true;
    private final boolean _isInitial;
    private final Timer _timer;

    DFAExecutableState(DFA dfa, TemporalConjunctiveQuery tcq, int dfaState, int timePoint,
                       EdgeConstraintChecker edgeChecker)
    {
        this(dfa, tcq, dfaState, null, null, timePoint, edgeChecker, false, null);
    }

    DFAExecutableState(DFA dfa, TemporalConjunctiveQuery tcq, int dfaState, int timePoint,
                       EdgeConstraintChecker edgeChecker, Timer timer)
    {
        this(dfa, tcq, dfaState, null, null, timePoint, edgeChecker, false, timer);
    }

    DFAExecutableState(DFA dfa, TemporalConjunctiveQuery tcq, int dfaState, int timePoint,
                       EdgeConstraintChecker edgeChecker, boolean isInitial)
    {
        this(dfa, tcq, dfaState, null, null, timePoint, edgeChecker, isInitial, null);
    }

    DFAExecutableState(DFA dfa, TemporalConjunctiveQuery tcq, int dfaState, int timePoint,
                       EdgeConstraintChecker edgeChecker, boolean isInitial, Timer timer)
    {
        this(dfa, tcq, dfaState, null, null, timePoint, edgeChecker, isInitial, timer);
    }

    DFAExecutableState(DFA dfa, TemporalConjunctiveQuery tcq, int dfaState, QueryResult satBindings,
                       QueryResult unsatBindings, int timePoint, EdgeConstraintChecker edgeChecker)
    {
        this(dfa, tcq, dfaState, satBindings, unsatBindings, timePoint, edgeChecker, false, null);
    }

    /**
     *
     * @param dfa
     * @param tcq
     * @param dfaState
     * @param satBindings if null, we interpret this as all bindings are satisfiable (useful for initial state)
     * @param unsatBindings if null, we interpret this as no binding unsatisfiable (useful for initial state)
     * @param timePoint
     * @param edgeChecker
     * @param isInitial
     * @param timer
     */
    DFAExecutableState(DFA dfa, TemporalConjunctiveQuery tcq, int dfaState, QueryResult satBindings,
                       QueryResult unsatBindings, int timePoint, EdgeConstraintChecker edgeChecker, boolean isInitial,
                       Timer timer)
    {
        _dfa = dfa;
        _tcq = tcq;
        _dfaState = dfaState;
        _satBindings = satBindings;
        _unsatBindings = unsatBindings;
        _timePoint = timePoint;
        _maxTimePoint = tcq.getTemporalKB().size();
        _timer = timer;
        if (_timer != null)
            _timer.stop();
        if (timePoint < tcq.getTemporalKB().size())
            _kb = tcq.getTemporalKB().get(timePoint);
        else
            _kb = null;
        if (_timer != null)
            _timer.start();
        _edgeChecker = edgeChecker;
        _isInitial = isInitial;
    }

    protected void setSatBindings(QueryResult satBindings)
    {
        if (satBindings != null)
            _satBindings = satBindings.copy();
    }

    protected void addUnsatBindings(QueryResult unsatBindings)
    {
        if (_unsatBindings == null && unsatBindings != null)
            _unsatBindings = unsatBindings.copy();
        else if (unsatBindings != null)
            _unsatBindings.addAll(unsatBindings);
    }

    public QueryResult getSatBindings()
    {
        return _satBindings;
    }

    public QueryResult getUnsatBindings()
    {
        return _unsatBindings;
    }

    public int getTimePoint()
    {
        return _timePoint;
    }

    public boolean canExecute()
    {
        return _timePoint < _maxTimePoint && hasUncheckedBindingCandidates() && _kb != null;
    }

    private boolean hasUncheckedBindingCandidates()
    {
        return _hasUncheckedBindingCandidates;
    }

    public boolean isAccepting()
    {
        return !hasUncheckedBindingCandidates() && _dfa.isAccepting(_dfaState);
    }

    public KnowledgeBase getKB()
    {
        return _kb;
    }

    public int getDFAState()
    {
        return _dfaState;
    }

    public Collection<DFAExecutableState> execute() throws IOException, InterruptedException
    {
        Collection<DFAExecutableState> newExecutableStates = new HashSet<>();
        if (isInSink())
        {
            DFAExecutableState newState = new DFAExecutableState(_dfa, _tcq, _dfaState, _timePoint + 1,
                    _edgeChecker, _timer);
            newState.setSatBindings(_satBindings);
            newState.addUnsatBindings(_unsatBindings);
            newExecutableStates.add(newState);
        }
        else if (canExecute())
        {
            boolean fullyCheckedAllEdges = true;
            List<Edge> edges = _dfa.getEdges(_dfaState);
            Map<ResultBinding, Integer> bindingsUnsatCount = new HashMap<>();
            Map<Edge, Map<Bool, QueryResult>> edgeResults = new HashMap<>();
            for (Edge edge : edges)
            {
                QueryResult restrictSatToBindings = null;
                if (!_isInitial && _satBindings != null)
                    restrictSatToBindings = _satBindings.copy();  // TODO .copy() maybe not required
                Map<Bool, QueryResult> edgeResult = _edgeChecker.checkEdge(edge, _timePoint, _kb,
                        restrictSatToBindings);
                edgeResults.put(edge, edgeResult);
                // TODO: isEdgeCompletelyChecked not absolute but wrt. sat bindings of current state (no need to check more)
                fullyCheckedAllEdges &= _edgeChecker.isEdgeCompletelyChecked(edge, _timePoint);
                DFAExecutableState newState = new DFAExecutableState(_dfa, _tcq, edge.getToState(), _timePoint + 1,
                        _edgeChecker, _timer);
                // Already unsatisfiable bindings can be directly propagated
                newState.addUnsatBindings(_unsatBindings);
                // Propagate empty results only if we have a complete overall result - otherwise, they indicate a 'don't
                // know'.
                if (!edgeResult.get(Bool.TRUE).isEmpty() || edgeResult.get(Bool.FALSE).isComplete())
                    newState.setSatBindings(edgeResult.get(Bool.TRUE));
                if (!edgeResult.get(Bool.FALSE).isEmpty() || edgeResult.get(Bool.TRUE).isComplete())
                    newState.addUnsatBindings(edgeResult.get(Bool.FALSE));
                for (ResultBinding unsatBinding : edgeResult.get(Bool.FALSE))
                    // We can later only infer satisfiability on unsatisfiability for those that we know are satisfiable
                    if (_satBindings == null || _satBindings.contains(unsatBinding))
                    {
                        if (bindingsUnsatCount.containsKey(unsatBinding))
                            bindingsUnsatCount.put(unsatBinding, bindingsUnsatCount.get(unsatBinding) + 1);
                        else
                            bindingsUnsatCount.put(unsatBinding, 1);
                    }
                newExecutableStates.add(newState);
            }

            // This is an optimization:
            // Iterates through all bindings that have an unsatisfiability count for |Edges|-1. For those, we know that
            // the missing edge *has* to be satisfiable. We add this to the new states and inform the CNCQ sat. manager.
            // TODO this is not true anymore. This only holds if the binding was NOT in the _unsatBindings before firing
            //  this state!
            for (ResultBinding binding : bindingsUnsatCount.keySet())
                if (bindingsUnsatCount.get(binding) == edges.size() - 1)
                    for (Edge edge : edgeResults.keySet())
                        if (!edgeResults.get(edge).get(Bool.TRUE).contains(binding) &&
                                !edgeResults.get(edge).get(Bool.FALSE).contains(binding))
                        {
                            for (CNCQQuery cncq : edge.getCNCQs())
                            {
                                QueryResult satResult = new QueryResultImpl(cncq);
                                satResult.add(binding);
                                _edgeChecker.getSatisfiabilityKnowledgeManager().getKnowledgeOnQuery(cncq).
                                        informAboutSatisfiability(satResult, true, _timePoint);
                                for (DFAExecutableState newState : newExecutableStates)
                                    if (edge.getToState() == newState._dfaState)
                                    {
                                        if (newState.getSatBindings() != null)
                                            newState.getSatBindings().add(binding);
                                        else
                                        {
                                            QueryResult bindingResult = new QueryResultImpl(_tcq);
                                            bindingResult.add(binding);
                                            newState.setSatBindings(bindingResult);
                                        }
                                        break;
                                    }
                                break;
                            }
                        }
            _hasUncheckedBindingCandidates = !fullyCheckedAllEdges;

            // Clean up: remove useless states (states w/o satisfiable bindings are not executable, but states with
            // unsatisfiable bindings have valuable information, so we keep them)
            List<DFAExecutableState> emptyStates = new ArrayList<>();
            for (DFAExecutableState state : newExecutableStates)
                if ((state.getSatBindings() == null && state.getUnsatBindings() == null) ||
                        (state.getSatBindings() != null && state.getSatBindings().isEmpty() &&
                                state.getUnsatBindings() != null && state.getUnsatBindings().isEmpty()))
                    emptyStates.add(state);
            newExecutableStates.removeAll(emptyStates);
        }

        return newExecutableStates;
    }

    private boolean isInSink()
    {
        List<Edge> edges = _dfa.getEdges(_dfaState);
        return edges.size() == 1 && edges.get(0).getFromState() == edges.get(0).getToState();
    }

    private boolean isInAcceptingSink()
    {
        return _dfa.isAccepting(_dfaState) && isInSink();
    }

    public void merge(DFAExecutableState toMerge)
    {
        // TODO
        //  We shall actually only add "unsat" definitely here if ALL states agreed on UNSAT!!!!
        //  -> done by retainAll
        //  however, unsatBindings shall *always* be passed from one state to ALL its successors
        //  no matter what we do, if we had an unsat binding in the past, we can never satisfy it again
        //  -> done in execute when generating new states
        assert(getTimePoint() == toMerge.getTimePoint() && getDFAState() == toMerge.getDFAState());
        if (toMerge.getSatBindings() != null)
        {
            if (_satBindings == null)
                _satBindings = toMerge.getSatBindings().copy();
            else
                _satBindings.addAll(toMerge.getSatBindings());
        }
        if (toMerge.getUnsatBindings() != null)
        {
            if (_unsatBindings == null)
                _unsatBindings = toMerge.getUnsatBindings().copy();
            else
                _unsatBindings.retainAll(toMerge.getUnsatBindings());
        }
        // In prior iterations, we may have added an unsat info that becomes sat through some other incoming edge.
        _unsatBindings.removeAll(_satBindings);
    }

    @Override
    public String toString()
    {
        String res = "State " + _dfaState + " @ t=" + _timePoint;
        if (_satBindings != null)
            res += " - SAT " + _satBindings;
        if (_unsatBindings != null)
            res += " - UNSAT " + _unsatBindings;
        return res;
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (_satBindings == null ? 0 : _satBindings.hashCode());
        result = PRIME * result + (_unsatBindings == null ? 0 : _unsatBindings.hashCode());
        result = PRIME * result + _dfaState;
        result = PRIME * result + _timePoint;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DFAExecutableState other = (DFAExecutableState) obj;
        if (_satBindings == null && other._satBindings != null)
            return false;
        if (_unsatBindings == null && other._unsatBindings != null)
            return false;
        else
            return other._dfaState == _dfaState && other._timePoint == _timePoint &&
                    _satBindings.equals(other._satBindings) && _unsatBindings.equals(other._unsatBindings);
    }
}
