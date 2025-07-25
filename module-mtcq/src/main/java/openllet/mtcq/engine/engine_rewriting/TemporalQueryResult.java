package openllet.mtcq.engine.engine_rewriting;

import openllet.core.utils.Pair;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.query.sparqldl.model.results.QueryResult;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Data class for MTCQ answering results. Preservers the structure of the formula and attaches a non-temporal query
 * result to each subformula. Designed to defer full evaluation until explicitly requested via {@link #collapse()}.
 * Works only for formulae that are a conjunction of disjunction (as given by the {@code CNFTransformer}.
 */
public class TemporalQueryResult
{
    // Main recursive data structure representing the formula's syntax tree (given as a conjunction of disjunctions).
    private final List<Pair<QueryResult, TemporalQueryResult>> _conjunctionOfDisjunctions = new ArrayList<>();
    // Cached result, if it has already been collapsed before.
    private QueryResult _collapsed = null;
    // Query for which the result was obtained.
    private final MetricTemporalConjunctiveQuery _query;

    /**
     * Creates a temporal query result for a given MTCQ.
     * @param query the temporal query this result corresponds to.
     */
    public TemporalQueryResult(MetricTemporalConjunctiveQuery query)
    {
        _query = query;
    }

    /**
     * Adds a conjunct where the result of the conjunct is already known (it is hence atemporal).
     * Used when no temporal recursion is needed at this level, it is hence the base case of the recursive structure.
     * @param atemporalResult The result to add as a conjunct.
     */
    public void addNewConjunct(QueryResult atemporalResult)
    {
        _conjunctionOfDisjunctions.add(new Pair<>(atemporalResult, null));
        _collapsed = null;
    }

    /**
     * Adds a conjunct containing a disjunction of an atemporal and a nested temporal query result.
     * This is the recursive case of the structure. If a temporal query result is added, empties the cache, hence new
     * computation of {@link #collapse()} is required.
     * @param atemporalResult Non-temporal query result that could already been computed.
     * @param temporalResult Temporal query result that still needs collapsing or further recursive computation.
     */
    public void addNewConjunct(QueryResult atemporalResult, TemporalQueryResult temporalResult)
    {
        _conjunctionOfDisjunctions.add(new Pair<>(atemporalResult, temporalResult));
        if (temporalResult != null)
            _collapsed = null;
    }

    /**
     * @return The associated MTCQ.
     */
    public MetricTemporalConjunctiveQuery getQuery()
    {
        return _query;
    }

    /**
     * Collapses the result tree into a single QueryResult by:
     * - Merging all conjuncts
     * - Combining nested results recursively
     * This is an expensive operation; results are cached after the first collapse.
     * @return The fully evaluated result, or null if no conjuncts exist.
     */
    @Nullable
    public QueryResult collapse()
    {
        if (_collapsed == null)
        {
            QueryResult finalResult = null;
            for (Pair<QueryResult, TemporalQueryResult> conjunction : _conjunctionOfDisjunctions)
            {
                QueryResult conjunctionRes;
                if (conjunction.first != null)
                {
                    conjunctionRes = conjunction.first;
                    if (conjunction.second != null)
                        conjunctionRes.addAll(conjunction.second.collapse());
                }
                else
                {
                    conjunctionRes = conjunction.second.collapse();
                }
                if (finalResult == null)
                    finalResult = conjunctionRes;
                else
                    finalResult.retainAll(conjunctionRes);
            }
            _collapsed = finalResult;
        }
        return _collapsed;
    }
}
