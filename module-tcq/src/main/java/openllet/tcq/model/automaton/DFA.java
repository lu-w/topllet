package openllet.tcq.model.automaton;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;

import java.util.ArrayList;
import java.util.List;

public class DFA extends CompactDFA<String>
{
    private final List<Edge> _edges = new ArrayList<>();

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
                if (getTransition(state, letter) != null && successorState >=0)
                {
                    _edges.add(new Edge(letter, state, successorState, getInputAlphabet()));
                }
            }
        }
    }
}
