package openllet.tcq.model.automaton;

import net.automatalib.words.Alphabet;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.cncq.CNCQQueryImpl;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.tcq.model.query.Proposition;

import java.util.*;

public class Edge
{
    private final Alphabet<String> _alphabet;
    private final String _label;
    private final int _from;
    private final int _to;
    private List<CNCQQuery> _cncqs = null;

    Edge(String label, int fromState, int toState, Alphabet<String> alphabet)
    {
        _label = label;
        _to = toState;
        _from = fromState;
        _alphabet = alphabet;
    }

    public List<CNCQQuery> getCNCQs(Map<Proposition, ConjunctiveQuery> propositionalAbstraction)
    {
        if (_cncqs == null && _label.length() > 0)
        {
            _cncqs = new ArrayList<>();
            String[] lines = _label.split("\\\\n");
            if (lines.length > 0)
            {
                int _numOfCNCQs = lines[0].split(" ").length;
                for (int curCNCQIndex = 0; curCNCQIndex < _numOfCNCQs; curCNCQIndex++)
                {
                    CNCQQuery cncq = new CNCQQueryImpl(null, false);
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
