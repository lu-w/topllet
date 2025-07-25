package openllet.query.sparqldl.engine;

import openllet.core.boxes.abox.ABox;
import openllet.core.utils.Timer;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;

import java.io.IOException;

/**
 * Central interface for query engines.
 * @param <QueryType> The class of queries that are answered by this engine.
 */
public interface QueryExec<QueryType extends Query<QueryType>>
{
    /**
     * Returns true iff. the given query is supported by the query engine.
     * @param query The query to check
     * @return true iff. the given union query is supported by the query engine
     */
    boolean supports (QueryType query);

    /**
     * Executes the query by checking its entailment w.r.t. to its knowledge base.
     * @param query The query to execute
     * @return a non-empty QueryResult (i.e. of size > 0) if the query entailed or an empty query result (i.e. of
     * size 0) if it is not entailed. In case an entailed Boolean query is given, it returns a QueryResult of size 1
     * containing a single, empty ResultBinding.
     */
    QueryResult exec(QueryType query);

    QueryResult exec(QueryType q, ABox abox);

    QueryResult exec(QueryType q, ABox abox, Timer timer);

    QueryResult exec(QueryType q, QueryResult excludeBindings, QueryResult restrictToBindings);
}
