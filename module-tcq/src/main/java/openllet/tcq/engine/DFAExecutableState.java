package openllet.tcq.engine;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Bool;
import openllet.core.utils.Timer;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.shared.tools.Log;
import openllet.tcq.model.automaton.DFA;
import openllet.tcq.model.automaton.Edge;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Central class in the satisfiability check for TCQs. It represents triples of a DFA state, time point, and
 * (un)satisfiability knowledge.
 * An executable state can be executed, i.e., according to its current DFA state, all edges are checked and a new set of
 * states are created that represented the propagated (un)satisfiability information from the current state to these new
 * states.
 */
public class DFAExecutableState
{
    public static final Logger _logger = Log.getLogger(DFAExecutableState.class);
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
     * Constructs a new executable state.
     *
     * @param dfa The DFA which corresponds to the given TCQ.
     * @param tcq The TCQ for which the satisfiability check is performed.
     * @param dfaState The DFA state in which this executable state is located.
     * @param satBindings If null, we interpret this as all bindings are satisfiable (useful for initial state).
     *                    Otherwise, contains information on the satisfiable bindings in the current state
     *                    (possibly incomplete).
     * @param unsatBindings If null, we interpret this as no binding unsatisfiable (useful for initial state)
     *                      Otherwise, contains information on the satisfiable bindings in the current state (possibly
     *                      incomplete).
     * @param timePoint The time point at which the executable state is located.
     * @param edgeChecker A point to the edge checker to use. To re-use as much information as much information as
     *                    possible, executable states share their edge checker.
     * @param isInitial True iff. the current state represents the initial state.
     * @param timer A timer for performance measurements (can be null).
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

    /**
     * Sets the satisfiable bindings of this state (does not add - it overwrites).
     * Note that this does not copy over - expect possible manipulations of the given query result later on!
     * @param satBindings Information on the satisfiable bindings in this state.
     */
    protected void setSatBindings(QueryResult satBindings)
    {
        if (satBindings != null)
            _satBindings = satBindings;
    }

    /**
     * Adds unsatisfiable bindings to this state (does not overwrite - it adds).
     * Note that this performs a copy - you can safely pass a pointer here without worrying about side effects.
     * @param unsatBindings The unsatisfiable bindings to add to this state.
     */
    protected void addUnsatBindings(QueryResult unsatBindings)
    {
        if (_unsatBindings == null && unsatBindings != null)
            _unsatBindings = unsatBindings.copy();
        else if (unsatBindings != null)
            _unsatBindings.addAll(unsatBindings);
    }

    /**
     * @return A pointer to the satisfiable bindings of this state (note: use this carefully!)
     */
    public QueryResult getSatBindings()
    {
        return _satBindings;
    }

    /**
     * @return A pointer to the unsatisfiable bindings of this state (note: use this carefully!)
     */
    public QueryResult getUnsatBindings()
    {
        return _unsatBindings;
    }

    /**
     * @return The time point this state represents.
     */
    public int getTimePoint()
    {
        return _timePoint;
    }

    /**
     * @return True iff. this state can be executed, i.e., it is below the maximum time point and has unchcked binding
     * candidates.
     */
    public boolean canExecute()
    {
        return _timePoint < _maxTimePoint && hasUncheckedBindingCandidates() && _kb != null;
    }

    /**
     * This information is created during execution of the state. It is then cached.
     * @return True iff. this state's satisfiability and unsatisfiability knowledge does not cover all possible
     * bindings.
     */
    private boolean hasUncheckedBindingCandidates()
    {
        return _hasUncheckedBindingCandidates;
    }

    /**
     * @return The atemporal knowledge base that this state had to check against.
     */
    public KnowledgeBase getKB()
    {
        return _kb;
    }

    /**
     * @return The DFA state in which this state is located.
     */
    public int getDFAState()
    {
        return _dfaState;
    }

    /**
     * Execute this state, i.e., it checks all edges of the current state and propagates (un)satisfiability knowledge
     * according to the (un)satisfiability of the CNCQs on the edges. Thus, a new collection of new states is created.
     * @return The new collection of states after execution.
     * @throws IOException If CNCQ query engine encountered an IO exception.
     * @throws InterruptedException If CNCQ query engine was interrupted.
     */
    public Collection<DFAExecutableState> execute() throws IOException, InterruptedException
    {
        Collection<DFAExecutableState> newExecutableStates = new ArrayList<>();
        QueryResult restrictSatToBindings = null;
        if (!_isInitial && _satBindings != null)
            restrictSatToBindings = _satBindings;
        else if (!_isInitial)
            // We can not infer satisfiability if prior state has no information about its satisfiability.
            restrictSatToBindings = new QueryResultImpl(_tcq);

        // Sinks are handled efficiently by just propagating all information directly.
        if (isInSink())
        {
            DFAExecutableState newState = new DFAExecutableState(_dfa, _tcq, _dfaState, _timePoint + 1,
                    _edgeChecker, _timer);
            newState.setSatBindings(_satBindings);
            newState.addUnsatBindings(_unsatBindings);
            newExecutableStates.add(newState);
        }
        // Check if we can execute this state, then check all edges and create appropriate successor states.
        else if (canExecute())
        {
            boolean fullyCheckedAllEdges = true;
            List<Edge> edges = _dfa.getEdges(_dfaState);
            Map<ResultBinding, Integer> bindingsUnsatCount = new HashMap<>();
            Map<Edge, Map<Bool, QueryResult>> edgeResults = new HashMap<>();
            // Checks each edge, i.e., finds (un)satisfiable bindings for the union of the CNCQs on the edge.
            for (Edge edge : edges)
            {
                Map<Bool, QueryResult> edgeResult = _edgeChecker.checkEdge(edge, _timePoint, _kb,
                        restrictSatToBindings);
                edgeResults.put(edge, edgeResult);
                // TODO: isEdgeCompletelyChecked can be computed not absolute but w.r.t. sat bindings of current state.
                //  No need to check more, but does not work with underapprox. semantics mode - is it worth the cost?
                fullyCheckedAllEdges &= _edgeChecker.isEdgeCompletelyChecked(edge, _timePoint);
                DFAExecutableState newState = new DFAExecutableState(_dfa, _tcq, edge.getToState(), _timePoint + 1,
                        _edgeChecker, _timer);
                // Already unsatisfiable bindings can be directly propagated
                newState.addUnsatBindings(_unsatBindings);
                // Propagate empty results only if we have a complete overall result - otherwise, they indicate a 'don't
                // know'.
                edgeResult.get(Bool.TRUE).removeAll(_unsatBindings);
                if (!edgeResult.get(Bool.TRUE).isEmpty() || edgeResult.get(Bool.FALSE).isComplete())
                    newState.setSatBindings(edgeResult.get(Bool.TRUE));
                if (!edgeResult.get(Bool.FALSE).isEmpty() || edgeResult.get(Bool.TRUE).isComplete())
                    newState.addUnsatBindings(edgeResult.get(Bool.FALSE));
                for (ResultBinding unsatBinding : edgeResult.get(Bool.FALSE))
                    // We can later only infer satisfiability from unsatisfiability for those bindings that we know were
                    // satisfiable and were not unsatisfiable in this state
                    if ((_satBindings == null || _satBindings.contains(unsatBinding)) &&
                            (_unsatBindings == null || !_unsatBindings.contains(unsatBinding)))
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
            for (ResultBinding binding : bindingsUnsatCount.keySet())
                if (bindingsUnsatCount.get(binding) == edges.size() - 1 &&
                        (restrictSatToBindings == null || restrictSatToBindings.contains(binding)))
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
                if ((state.getSatBindings() != null && state.getSatBindings().isEmpty() &&
                        state.getUnsatBindings() != null && state.getUnsatBindings().isEmpty()))
                    emptyStates.add(state);
            newExecutableStates.removeAll(emptyStates);
        }

        return newExecutableStates;
    }

    /**
     * @return True iff. the current DFA state is a sink
     */
    private boolean isInSink()
    {
        List<Edge> edges = _dfa.getEdges(_dfaState);
        return edges.size() == 1 && edges.get(0).getFromState() == edges.get(0).getToState();
    }

    /**
     * Merges a given other state into this state, i.e., it adds all satisfiable bindings of the other state to this
     * state and takes the intersection of the unsatisfiable bindings of both states.
     * Also updates unsatisfiability information, if the given state introduces new satisfiability information on some
     * binding.
     * Updates this state in-place.
     * @param toMerge A DFAExecutableState that refers to exactly the same time point and DFA state.
     */
    public void merge(DFAExecutableState toMerge)
    {
        assert(getTimePoint() == toMerge.getTimePoint() && getDFAState() == toMerge.getDFAState());
        if (toMerge.getSatBindings() != null)
        {
            if (_satBindings == null)
                _satBindings = toMerge.getSatBindings();
            else
                _satBindings.addAll(toMerge.getSatBindings());
        }
        if (toMerge.getUnsatBindings() != null && _unsatBindings != null)
            // We can only merge unsatisfiable bindings if one of them is not null -> if we got some null unsatisfiable
            // binding, this means we have *no* knowledge, and it could be anything. No arbitration can be done.
            _unsatBindings.retainAll(toMerge.getUnsatBindings());
        // Required because retainAll does nothing when given null - but null means here 'don't know anything' about
        // unsatisfiability, but we need total agreement from all states.
        else
            _unsatBindings = null;
        // In prior iterations, we may have added an unsat info that becomes sat through some other incoming edge.
        if (_unsatBindings != null && toMerge._satBindings != null)
            for (ResultBinding binding : toMerge._satBindings)
                _unsatBindings.remove(binding);
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
