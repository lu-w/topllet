package openllet.tcq.model.automaton;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.bcq.BCQQuery;
import openllet.query.sparqldl.model.bcq.BCQQueryImpl;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.tcq.model.query.Proposition;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import java.util.*;

/**
 * Represents an edge in the DFA. An edge consists of a union (list) of BCQs, and a predecessor and successor state.
 */
public class Edge
{
    private final TemporalConjunctiveQuery _tcq;
    private final String _label;
    private final int _from;
    private final int _to;
    private List<BCQQuery> _bcqs = null;

    Edge(String label, int fromState, int toState, TemporalConjunctiveQuery tcq)
    {
        _label = label;
        _to = toState;
        _from = fromState;
        _tcq = tcq;
    }

    /**
     * Constructs the BCQs based on the symbolic representation of the edge from the dot file.
     * That is, it implements a parser for the vector representation and checks the propsitional abstraction of the TCQ
     * to fetch the appropriate CQ. The BCQ is built according to the Boolean combination (i.e., adding negations
     * appropriately).
     * @return A list of BCQs for the edge.
     */
    public List<BCQQuery> getBCQs()
    {
        if (_tcq == null)
        {
            return new ArrayList<>();
        }
        else if (_bcqs == null && !_label.isEmpty())
        {
            Map<Proposition, ConjunctiveQuery> propositionalAbstraction = _tcq.getPropositionalAbstraction();
            KnowledgeBase defaultKb = !_tcq.getTemporalKB().isEmpty() ? _tcq.getTemporalKB().get(0) : null;
            _bcqs = new ArrayList<>();
            String[] lines = _label.split("\\\\n");
            if (lines.length > 0)
            {
                int _numOfBCQs = lines[0].split(" ").length;
                for (int curBCQIndex = 0; curBCQIndex < _numOfBCQs; curBCQIndex++)
                {
                    BCQQuery bcq = new BCQQueryImpl(defaultKb, true);
                    for(int curPropositionIndex = 0; curPropositionIndex < lines.length; curPropositionIndex++)
                    {
                        String line = lines[curPropositionIndex];
                        String val = line.split("[ ,]")[curBCQIndex];
                        if ("1".equals(val))
                            bcq.addPositiveQuery(
                                    propositionalAbstraction.values().stream().toList().get(curPropositionIndex).
                                            copy());
                        else if ("0".equals(val))
                            bcq.addNegativeQuery(
                                    propositionalAbstraction.values().stream().toList().get(curPropositionIndex).
                                            copy());
                    }
                    _bcqs.add(bcq);
                }
            }
        }
        return _bcqs;
    }

    public int getToState()
    {
        return _to;
    }

    public int getFromState()
    {
        return _from;
    }

    @Override
    public String toString()
    {
        if (_bcqs == null)
            return _label;
        else
            return _bcqs.toString();
    }
}
