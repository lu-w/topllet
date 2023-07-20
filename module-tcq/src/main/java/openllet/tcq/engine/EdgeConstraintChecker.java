package openllet.tcq.engine;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Bool;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.tcq.model.automaton.DFA;
import openllet.tcq.model.automaton.Edge;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import java.io.IOException;
import java.util.*;

public class EdgeConstraintChecker
{
    private final SatisfiabilityKnowledgeManager _cncqSatManager;
    private boolean _useUnderapproximatingSemantics = true;

    public EdgeConstraintChecker(TemporalConjunctiveQuery tcq, DFA dfa)
    {
        _cncqSatManager = new SatisfiabilityKnowledgeManager(tcq, dfa);
    }

    public void setUnderapproximatingSemantics(boolean underapproximatingSemantics)
    {
        _useUnderapproximatingSemantics = underapproximatingSemantics;
    }

    public boolean isUnderapproximatingSemantics()
    {
        return _useUnderapproximatingSemantics;
    }

    public SatisfiabilityKnowledgeManager getSatisfiabilityKnowledgeManager()
    {
        return _cncqSatManager;
    }

    public void excludeBindings(QueryResult bindings)
    {
        _cncqSatManager.setGloballyExcludedBindings(bindings);
    }

    public void doNotExcludeBindings()
    {
        _cncqSatManager.doNotGloballyExcludeBindings();
    }

    public boolean isEdgeCompletelyChecked(Edge edge, int timePoint)
    {
        boolean allCncqsChecked = true;
        for (CNCQQuery cncq : edge.getCNCQs())
            allCncqsChecked &= _cncqSatManager.getKnowledgeOnQuery(cncq).isComplete(timePoint);
        return allCncqsChecked;
    }

    public Map<Bool, QueryResult> checkEdge(Edge edge, int timePoint, KnowledgeBase kb)
            throws IOException, InterruptedException
    {
        return checkEdge(edge, timePoint, kb, null);
    }

    public Map<Bool, QueryResult> checkEdge(Edge edge, int timePoint, KnowledgeBase kb,
                                            QueryResult restrictSatToBindings)
            throws IOException, InterruptedException
    {
        Map<Bool, QueryResult> result = new HashMap<>();
        List<CNCQQuery> cncqs = edge.getCNCQs();
        boolean firstCncq = true;
        for (CNCQQuery cncq : cncqs)
        {
            Map<Bool, QueryResult> cncqResult = _cncqSatManager.computeSatisfiableBindings(cncq, timePoint, kb,
                    _useUnderapproximatingSemantics, restrictSatToBindings);
            if (firstCncq)
            {
                result.put(Bool.TRUE, cncqResult.get(Bool.TRUE));
                result.put(Bool.FALSE, cncqResult.get(Bool.FALSE));
                firstCncq = false;
            }
            else
            {
                result.get(Bool.TRUE).addAll(cncqResult.get(Bool.TRUE), restrictSatToBindings);
                result.get(Bool.FALSE).retainAll(cncqResult.get(Bool.FALSE));
            }
            // Early escape possible (only for satisfiable bindings)
            if (result.get(Bool.TRUE).size() >= result.get(Bool.TRUE).getMaxSize())
                break;
        }
        // TODO Copy -- can it be removed?
        if (result.get(Bool.TRUE) != null)
            result.put(Bool.TRUE, result.get(Bool.TRUE).copy());
        return result;
    }
}
