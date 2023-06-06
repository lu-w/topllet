package openllet.tcq.engine;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Bool;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.tcq.model.automaton.DFA;
import openllet.tcq.model.automaton.Edge;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class EdgeConstraintChecker
{
    private final DFA _dfa;
    private final TemporalConjunctiveQuery _tcq;
    private final SatisfiabilityKnowledgeManager _cncqSatManager;
    private boolean _useUnderapproximatingSemantics = true;
    private QueryResult _excludeBindings = null;

    public EdgeConstraintChecker(TemporalConjunctiveQuery tcq, DFA dfa)
    {
        _dfa = dfa;
        _tcq = tcq;
        _cncqSatManager = new SatisfiabilityKnowledgeManager(_tcq, _dfa);
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
        _excludeBindings = bindings;
        _cncqSatManager.setGloballyExcludedBindings(bindings);
    }

    public void doNotExcludeBindings()
    {
        _excludeBindings = null;
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
        return checkEdge(edge, timePoint, kb, null, null);
    }

    public Map<Bool, QueryResult> checkEdge(Edge edge, int timePoint, KnowledgeBase kb,
                                            QueryResult restrictSatToBindings, QueryResult restrictUnsatToBindings)
            throws IOException, InterruptedException
    {
        Map<Bool, QueryResult> result = new HashMap<>();
        List<CNCQQuery> cncqs = edge.getCNCQs();
        boolean firstCncq = true;
        for (CNCQQuery cncq : cncqs)
        {
            Map<Bool, QueryResult> cncqResult = _cncqSatManager.computeSatisfiableBindings(cncq, timePoint, kb,
                    _useUnderapproximatingSemantics, restrictSatToBindings, restrictUnsatToBindings);
            if (firstCncq)
            {
                result.put(Bool.TRUE, cncqResult.get(Bool.TRUE));
                QueryResult unsatResult = cncqResult.get(Bool.FALSE);
                if (restrictSatToBindings != null)
                {
                    // TODO copy is inefficient, but probably needed if the result stored and used elsewhere
                    //  (e.g. to keep track of things)
                    unsatResult = unsatResult.copy();
                    unsatResult.retainAll(restrictUnsatToBindings);
                }
                result.put(Bool.FALSE, unsatResult);
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
