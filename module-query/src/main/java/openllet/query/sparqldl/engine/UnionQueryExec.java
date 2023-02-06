package openllet.query.sparqldl.engine;

import openllet.query.sparqldl.model.QueryResult;
import openllet.query.sparqldl.model.UnionQuery;

public interface UnionQueryExec
{
    /**
     * Returns true iff. the given union query is supported by the query engine.
     * @param query The query to check
     * @return true iff. the given union query is supported by the query engine
     */
    boolean supports (UnionQuery query);

    /**
     * Executes the union query by checking its entailment w.r.t. to its knowledge base.
     * @param query The union query to execute
     * @return a QueryResult if the query entailed or null if it is not entailed.
     */
    QueryResult exec(UnionQuery query);
}
