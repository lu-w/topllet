package openllet.mtcq.ui;

import openllet.core.KnowledgeBase;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;

import javax.annotation.Nullable;

/**
 * Interface for UIs that can be used by the {@code MTCQNormalFormEngine}. The engine will execute the callbacks at
 * their corresponding times during execution of the query answering algorithm.
 */
public interface StreamingUIHandler {

    /**
     * Sets up the UI for a subsequent evaluation of the query.
     * @param query The query that is soon to be answered.
     */
    void setup(MetricTemporalConjunctiveQuery query);

    /**
     * Clears the UI, allowing to answer a new query.
     */
    void clear();

    /**
     * Exits the UI fully.
     */
    void tearDown();

    /**
     * Informs the UI about a new time point that is to be examined.
     * @param timePoint The time point that will be examined.
     */
    void informAboutStartOfIteration(int timePoint);

    /**
     * Informs the UI about finishing a time point that.
     * @param timePoint The time point that was examined.
     */
    void informAboutEndOfIteration(int timePoint);

    /**
     * Informs the UI about gathered results to the query at the given time.
     * @param timePoint The time point in the data that the results were computed for.
     * @param kb The non-temporal knowledge base the results were gathered from.
     * @param query The query that was evaluated (note: can also be a conjunctive query).
     * @param result The query result for the query and knowledge base. Can be {@code null} if no result is available
     *               yet.
     */
    void informAboutResults(int timePoint, KnowledgeBase kb, Query<?> query, @Nullable QueryResult result);
}
