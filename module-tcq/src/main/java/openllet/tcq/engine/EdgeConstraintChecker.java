package openllet.tcq.engine;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Bool;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.shared.tools.Log;
import openllet.tcq.model.automaton.DFA;
import openllet.tcq.model.automaton.Edge;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Checks a so-called edge constraint, i.e., a union of CNCQs occurring on the edge of a DFA. Checking means that a set
 * of answers is assembled that satisfy resp. not satisfy the given edge, using `checkEdge`.
 * Internally, it relies on the SatisfiabilityKnowledgeManager to perform checking and caching of satisfiability of
 * CNCQs.
 */
public class EdgeConstraintChecker
{
    public static final Logger _logger = Log.getLogger(EdgeConstraintChecker.class);
    private final SatisfiabilityKnowledgeManager _cncqSatManager;
    private boolean _useUnderapproximatingSemantics = true;

    public EdgeConstraintChecker(TemporalConjunctiveQuery tcq, DFA dfa)
    {
        _cncqSatManager = new SatisfiabilityKnowledgeManager(tcq, dfa);
    }

    /**
     * If set, the edge constraints are purely checked by Openllet's CQ engine, leading to under-approximating results.
     * @param underapproximatingSemantics True iff. the CQ engine shall be used.
     */
    public void setUnderapproximatingSemantics(boolean underapproximatingSemantics)
    {
        _useUnderapproximatingSemantics = underapproximatingSemantics;
    }

    /**
     * @return True iff. the edge constraint checker is in underapproximating semantics mode.
     */
    public boolean isUnderapproximatingSemantics()
    {
        return _useUnderapproximatingSemantics;
    }

    /**
     * @return A pointer to the manager for satisfiability knowledge.
     */
    public SatisfiabilityKnowledgeManager getSatisfiabilityKnowledgeManager()
    {
        return _cncqSatManager;
    }

    /**
     * Instructs the edge constraint checker to exclude the given bindings from all further considerations.
     * @param bindings The bindings to exclude.
     */
    public void excludeBindings(QueryResult bindings)
    {
        _cncqSatManager.setGloballyExcludedBindings(bindings);
    }

    /**
     * @param edge Edge to check completeness for
     * @param timePoint Time point at which the edge shall be checked
     * @return True iff. both satisfiability and unsatisfiability knowledge w.r.t. the given edge and time is complete.
     */
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

    /**
     * Checks an edge of some DFA (which we do not need to know).
     * @param edge The edge to check
     * @param timePoint The time point we are located at
     * @param kb The atemporal knowledge base for the given time point
     * @param restrictSatToBindings If not null, satisfiability results are restricted to this set of bindings
     * @return A query result for satisfiable and unsatisfiable knowledge, as a mapping from true (satisfiable) and
     * false (unsatisfiable) to a query result.
     * @throws IOException If CNCQ query engine encountered an IO exception.
     * @throws InterruptedException If CNCQ query engine was interrupted.
     */
    public Map<Bool, QueryResult> checkEdge(Edge edge, int timePoint, KnowledgeBase kb,
                                            QueryResult restrictSatToBindings)
            throws IOException, InterruptedException
    {
        Map<Bool, QueryResult> result = new HashMap<>();
        List<CNCQQuery> cncqs = edge.getCNCQs();
        boolean firstCncq = true;
        _logger.finer("Checking DFA edge " + edge);
        // Iterates over all CNCQs on the edge and combines their results.
        for (CNCQQuery cncq : cncqs)
        {
            // Check CNCQ on the edge by using the CNCQ satisfiability manager (which allows caching of results).
            Map<Bool, QueryResult> cncqResult = _cncqSatManager.computeSatisfiableBindings(cncq, timePoint, kb,
                    _useUnderapproximatingSemantics, restrictSatToBindings);
            if (firstCncq)
            {
                result.put(Bool.TRUE, cncqResult.get(Bool.TRUE));
                result.put(Bool.FALSE, cncqResult.get(Bool.FALSE));
                firstCncq = false;
            }
            // If we have multiple CNCQs on the edge, we take the union for satisfiability results, and intersection for
            // unsatisfiability results.
            else
            {
                result.get(Bool.TRUE).addAll(cncqResult.get(Bool.TRUE), restrictSatToBindings);
                result.get(Bool.FALSE).retainAll(cncqResult.get(Bool.FALSE));
            }
            // Early escape possible (only for satisfiable bindings)
            if (result.get(Bool.TRUE).size() >= result.get(Bool.TRUE).getMaxSize())
                break;
        }
        return result;
    }
}
