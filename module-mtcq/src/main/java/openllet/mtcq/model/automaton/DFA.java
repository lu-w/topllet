package openllet.mtcq.model.automaton;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Extends the AutomataLib DFA with functionality required for checking satisfiability of MTCQs.
 * Specifically, it allows to handle edges differently, since we need to store unions of BCQs for each edge.
 */
public class DFA extends CompactDFA<String>
{
    private final List<Edge> _edges = new ArrayList<>();
    private MetricTemporalConjunctiveQuery _mtcq = null;

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

    public DFA(CompactDFA<String> dfa, MetricTemporalConjunctiveQuery mtcq)
    {
        super(dfa);
        _mtcq = mtcq;
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
                    _edges.add(new Edge(letter, state, successorState, _mtcq));
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
