package openllet.tcq.engine;

import openllet.tcq.model.automaton.DFA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DFAExecutableStates extends ArrayList<DFAExecutableState>
{
    private final DFA _dfa;

    public DFAExecutableStates(DFA dfa)
    {
        _dfa = dfa;
    }

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

    public boolean coversDFAStatesCompletely()
    {
        Set<Integer> coveredStates = new HashSet<>();
        for (DFAExecutableState state : this)
            coveredStates.add(state.getDFAState());
        return coveredStates.size() == _dfa.getStates().size();
    }
    public boolean coversDFAFinalStatesCompletely()
    {
        Set<Integer> coveredStates = new HashSet<>();
        for (DFAExecutableState state : this)
            if (_dfa.isAccepting(state.getDFAState()))
                coveredStates.add(state.getDFAState());
        return coveredStates.size() == _dfa.getStates().stream().filter(_dfa::isAccepting).toList().size();
    }
}