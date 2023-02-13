package openllet.query.sparqldl.engine;

import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;

public interface QueryExec
{
    /**
     * Returns true iff. the given query is supported by the query engine.
     * @param query The query to check
     * @return true iff. the given union query is supported by the query engine
     */
    boolean supports (Query<?> query);

    /**
     * Executes the query by checking its entailment w.r.t. to its knowledge base.
     * @param query The query to execute
     * @return a QueryResult if the query entailed or null if it is not entailed.
     */
    QueryResult exec(Query<?> query);
}
