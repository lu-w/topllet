package openllet.mtcq.engine;

import openllet.mtcq.model.automaton.DFA;

import java.util.ArrayList;

public class FAStates extends ArrayList<FAState>
{
    /**
     * Merges the given state into this set of executable states by either merging it into an existing state, if
     * possible, or adding it as a completely new one to the collection.
     * @param toAdd The state to add.
     */
    public void mergeOrAdd(FAState toAdd)
    {
        FAState mergeState = findSameState(toAdd);
        if (mergeState != null)
            mergeState.merge(toAdd);
        else
            add(toAdd);
    }

    private FAState findSameState(FAState findState)
    {
        for (FAState state : this)
            if (state.getInternalFAState() == findState.getInternalFAState() &&
                    state.getTimePoint() == findState.getTimePoint())
                return state;
        return null;
    }

    /**
     * @return True iff. this state set contains a state that can be executed.
     */
    public boolean hasExecutableState()
    {
        boolean existsExecutableState = false;
        for (FAState state : this)
            if (state.canExecute())
            {
                existsExecutableState = true;
                break;
            }
        return existsExecutableState;
    }
}
