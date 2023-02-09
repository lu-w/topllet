package openllet.query.sparqldl.model.ucq;

import openllet.query.sparqldl.model.CompositeQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

import java.util.List;

public interface UnionQuery extends CompositeQuery
{
    /**
     * Converts this union query to conjunctive normal form, i.e. of the form (a v ... v b) ^ ... ^ (c v ... v d).
     * Does not change this query.
     * @return A list of union queries, where each conjunctive query of each union query contains only one atom.
     */
    CNFQuery toCNF();

    /**
     * Creates a subquery from the given query. Queries are listed according to the 'queries' parameter.
     *
     * @param queries selected query indices
     * @return subquery
     */
    UnionQuery reorder(int[] queries);

    /**
     * Checks if there are two disjuncts that share a common undistinguished variable.
     * @return True iff there are two disjuncts that share a common undistinguished variable
     */
    boolean disjunctsShareUndistVars();

    /**
     * Rolls this query up by applying the rolling-up procedure to each disjunct separately.
     * @return A new rolled-up union query.
     */
    UnionQuery rollUp();

    /**
     * Rolls this query up by applying the rolling-up procedure to each disjunct separately.
     * @param stopRollingOnDistVars Whether to stop rolling up on distinguished variables
     * @return A new rolled-up union query.
     */
    UnionQuery rollUp(boolean stopRollingOnDistVars);
}
