package openllet.tcq.model.automaton;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.cncq.CNCQQueryImpl;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.tcq.model.query.Proposition;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import java.util.*;

/**
 * Represents an edge in the DFA. An edge consists of a union (list) of CNCQs, and a predecessor and successor state.
 */
public class Edge
{
    private final TemporalConjunctiveQuery _tcq;
    private final String _label;
    private final int _from;
    private final int _to;
    private List<CNCQQuery> _cncqs = null;

    Edge(String label, int fromState, int toState, TemporalConjunctiveQuery tcq)
    {
        _label = label;
        _to = toState;
        _from = fromState;
        _tcq = tcq;
    }

    /**
     * Constructs the CNCQs based on the symbolic representation of the edge from the dot file.
     * That is, it implements a parser for the vector representation and checks the propsitional abstraction of the TCQ
     * to fetch the appropriate CQ. The CNCQ is built according to the Boolean combination (i.e., adding negations
     * appropriately).
     * @return A list of CNCQs for the edge.
     */
    public List<CNCQQuery> getCNCQs()
    {
        if (_tcq == null)
        {
            return new ArrayList<>();
        }
        else if (_cncqs == null && !_label.isEmpty())
        {
            Map<Proposition, ConjunctiveQuery> propositionalAbstraction = _tcq.getPropositionalAbstraction();
            KnowledgeBase defaultKb = !_tcq.getTemporalKB().isEmpty() ? _tcq.getTemporalKB().get(0) : null;
            _cncqs = new ArrayList<>();
            String[] lines = _label.split("\\\\n");
            if (lines.length > 0)
            {
                int _numOfCNCQs = lines[0].split(" ").length;
                for (int curCNCQIndex = 0; curCNCQIndex < _numOfCNCQs; curCNCQIndex++)
                {
                    CNCQQuery cncq = new CNCQQueryImpl(defaultKb, true);
                    for(int curPropositionIndex = 0; curPropositionIndex < lines.length; curPropositionIndex++)
                    {
                        String line = lines[curPropositionIndex];
                        String val = line.split("[ ,]")[curCNCQIndex];
                        if ("1".equals(val))
                            cncq.addPositiveQuery(
                                    propositionalAbstraction.values().stream().toList().get(curPropositionIndex).
                                            copy());
                        else if ("0".equals(val))
                            cncq.addNegativeQuery(
                                    propositionalAbstraction.values().stream().toList().get(curPropositionIndex).
                                            copy());
                    }
                    _cncqs.add(cncq);
                }
            }
        }
        return _cncqs;
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
        if (_cncqs == null)
            return _label;
        else
            return _cncqs.toString();
    }
}
