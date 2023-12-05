package openllet.mtcq.model.automaton;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.bcq.BCQQuery;
import openllet.query.sparqldl.model.bcq.BCQQueryImpl;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.mtcq.model.query.Proposition;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;

import java.util.*;

/**
 * Represents an edge in the DFA. An edge consists of a union (list) of BCQs, and a predecessor and successor state.
 */
public class Edge
{
    private final MetricTemporalConjunctiveQuery _mtcq;
    private final String _label;
    private final int _from;
    private final int _to;
    private List<BCQQuery> _bcqs = null;

    Edge(String label, int fromState, int toState, MetricTemporalConjunctiveQuery mtcq)
    {
        _label = label;
        _to = toState;
        _from = fromState;
        _mtcq = mtcq;
    }

    /**
     * Constructs the BCQs based on the symbolic representation of the edge from the dot file.
     * That is, it implements a parser for the vector representation and checks the propsitional abstraction of the MTCQ
     * to fetch the appropriate CQ. The BCQ is built according to the Boolean combination (i.e., adding negations
     * appropriately).
     * @return An unmodifiable view on the BCQs for the edge.
     */
    public List<BCQQuery> getBCQs()
    {
        if (_mtcq == null)
        {
            _bcqs = new ArrayList<>();
        }
        if (_bcqs == null)
        {
            Map<Proposition, ConjunctiveQuery> propositionalAbstraction = _mtcq.getPropositionalAbstraction();
            KnowledgeBase defaultKb = !_mtcq.getTemporalKB().isEmpty() ? _mtcq.getTemporalKB().get(0) : null;
            _bcqs = new ArrayList<>();
            if (!_label.isEmpty())
            {
                String[] lines = _label.split("\\\\n");
                if (lines.length > 0)
                {
                    int _numOfBCQs = lines[0].split(" ").length;
                    for (int curBCQIndex = 0; curBCQIndex < _numOfBCQs; curBCQIndex++)
                    {
                        BCQQuery bcq = new BCQQueryImpl(defaultKb, true);
                        for (int curPropositionIndex = 0; curPropositionIndex < lines.length; curPropositionIndex++)
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
        }
        return Collections.unmodifiableList(_bcqs);
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
