package openllet.mtcq.engine;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Bool;
import openllet.query.sparqldl.model.bcq.BCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.shared.tools.Log;
import openllet.mtcq.model.automaton.DFA;
import openllet.mtcq.model.automaton.Edge;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Checks a so-called edge constraint, i.e., a union of BCQs occurring on the edge of a DFA. Checking means that a set
 * of answers is assembled that satisfy resp. not satisfy the given edge, using `checkEdge`.
 * Internally, it relies on the SatisfiabilityKnowledgeManager to perform checking and caching of satisfiability of
 * BCQs.
 */
public class EdgeConstraintChecker
{
    public static final Logger _logger = Log.getLogger(EdgeConstraintChecker.class);
    private final SatisfiabilityKnowledgeManager _bcqSatManager;
    private boolean _useUnderapproximatingSemantics = true;

    public EdgeConstraintChecker(MetricTemporalConjunctiveQuery mtcq, DFA dfa)
    {
        _bcqSatManager = new SatisfiabilityKnowledgeManager(mtcq, dfa);
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
        return _bcqSatManager;
    }

    /**
     * Instructs the edge constraint checker to exclude the given bindings from all further considerations.
     * @param bindings The bindings to exclude.
     */
    public void excludeBindings(QueryResult bindings)
    {
        _bcqSatManager.setGloballyExcludedBindings(bindings);
    }

    /**
     * @param edge Edge to check completeness for
     * @param timePoint Time point at which the edge shall be checked
     * @return True iff. both satisfiability and unsatisfiability knowledge w.r.t. the given edge and time is complete.
     */
    public boolean isEdgeCompletelyChecked(Edge edge, int timePoint)
    {
        boolean allBcqsChecked = true;
        for (BCQQuery bcq : edge.getBCQs())
            allBcqsChecked &= _bcqSatManager.getKnowledgeOnQuery(bcq).isComplete(timePoint);
        return allBcqsChecked;
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
     * @throws IOException If BCQ query engine encountered an IO exception.
     * @throws InterruptedException If BCQ query engine was interrupted.
     */
    public Map<Bool, QueryResult> checkEdge(Edge edge, int timePoint, KnowledgeBase kb,
                                            QueryResult restrictSatToBindings)
            throws IOException, InterruptedException
    {
        Map<Bool, QueryResult> result = new HashMap<>();
        List<BCQQuery> bcqs = edge.getBCQs();
        boolean firstBcq = true;
        _logger.finer("Checking DFA edge " + edge);
        // Iterates over all BCQs on the edge and combines their results.
        for (BCQQuery bcq : bcqs)
        {
            // Check BCQ on the edge by using the BCQ satisfiability manager (which allows caching of results).
            Map<Bool, QueryResult> bcqResult = _bcqSatManager.computeSatisfiableBindings(bcq, timePoint, kb,
                    _useUnderapproximatingSemantics, restrictSatToBindings);
            if (firstBcq)
            {
                result.put(Bool.TRUE, bcqResult.get(Bool.TRUE));
                result.put(Bool.FALSE, bcqResult.get(Bool.FALSE));
                firstBcq = false;
            }
            // If we have multiple BCQs on the edge, we take the union for satisfiability results, and intersection for
            // unsatisfiability results.
            else
            {
                result.get(Bool.TRUE).addAll(bcqResult.get(Bool.TRUE), restrictSatToBindings);
                result.get(Bool.FALSE).retainAll(bcqResult.get(Bool.FALSE));
            }
            // Early escape possible (only for satisfiable bindings)
            if (result.get(Bool.TRUE).size() >= result.get(Bool.TRUE).getMaxSize())
                break;
        }
        return result;
    }
}
