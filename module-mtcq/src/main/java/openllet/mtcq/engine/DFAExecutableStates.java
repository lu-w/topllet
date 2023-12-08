package openllet.mtcq.engine;

import openllet.mtcq.model.automaton.DFA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class manages a set of executable states.
 */
public class DFAExecutableStates extends ArrayList<DFAExecutableState>
{
    private final DFA _dfa;

    public DFAExecutableStates(DFA dfa)
    {
        _dfa = dfa;
    }

    /**
     * Merges the given state into this set of executable states by either merging it into an existing state, if
     * possible, or adding it as a completely new one to the collection.
     * @param toAdd The state to add.
     */
    public void mergeOrAdd(DFAExecutableState toAdd)
    {
        DFAExecutableState mergeState = findSameState(toAdd);
        if (mergeState != null)
            mergeState.merge(toAdd);
        else
            add(toAdd);
    }

    private DFAExecutableState findSameState(DFAExecutableState findState)
    {
        for (DFAExecutableState state : this)
            if (state.getDFAState() == findState.getDFAState() && state.getTimePoint() == findState.getTimePoint())
                return state;
        return null;
    }

    /**
     * @return True iff. this state set contains a state that can be executed.
     */
    public boolean hasExecutableState()
    {
        boolean existsExecutableState = false;
        for (DFAExecutableState state : this)
            if (state.canExecute())
            {
                existsExecutableState = true;
                break;
            }
        return existsExecutableState;
    }

    /**
     * @param n A time step.
     * @return True iff. this state set covers the final states of the DFA completely.
     */
    public boolean coversDFAFinalStatesCompletely(int n)
    {
        Collection<Integer> reachableStates = _dfa.getStatesReachableInNSteps(n);
        Set<Integer> coveredStates = new HashSet<>();
        for (DFAExecutableState state : this)
            if (_dfa.isAccepting(state.getDFAState()) && reachableStates.contains(state.getDFAState()))
                coveredStates.add(state.getDFAState());
        return coveredStates.size() == reachableStates.stream().filter(_dfa::isAccepting).toList().size();
    }
}
