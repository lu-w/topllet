package openllet.tcq.model.automaton;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class DFA extends CompactDFA<String>
{
    private final List<Edge> _edges = new ArrayList<>();
    private TemporalConjunctiveQuery _tcq = null;

    public DFA()
    {
        super(new ListAlphabet<>(new ArrayList<>()));
    }

    public DFA(Alphabet<String> alphabet) {
        super(alphabet);
    }

    public DFA(CompactDFA<String> dfa)
    {
        super(dfa);
        _createEdges();
    }

    public DFA(CompactDFA<String> dfa, TemporalConjunctiveQuery tcq)
    {
        super(dfa);
        _tcq = tcq;
        _createEdges();
    }
    
    public List<Edge> getEdges(int state)
    {
        return _edges.stream().filter(x -> x.getFromState() == state).toList();

    }

    private void _createEdges()
    {
        for (int state : this.getStates())
        {
            for (String letter : getInputAlphabet())
            {
                Integer successorState = getTransition(state, letter);
                if (successorState != null && getTransition(state, letter) != null && successorState >=0)
                    _edges.add(new Edge(letter, state, successorState, _tcq));
            }
        }
    }

    public Collection<Integer> getStatesReachableInNSteps(int n)
    {
        Collection<Integer> reachableStates = getInitialStates();
        for (int i = 0; i < n; i++)
        {
            Collection<Integer> reachableStatesNext = new HashSet<>();
            for (Integer state : reachableStates)
                for (Edge edge : getEdges(state))
                    reachableStatesNext.add(edge.getToState());
            reachableStates = reachableStatesNext;
        }
        return reachableStates;
    }
}
