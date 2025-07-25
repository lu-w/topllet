package openllet.query.sparqldl.model.ucq;

import openllet.query.sparqldl.model.CompositeQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public interface UnionQuery extends CompositeQuery<ConjunctiveQuery, UnionQuery>
{
    /**
     * Converts this union query to conjunctive normal form, i.e. of the form (a v ... v b) ^ ... ^ (c v ... v d).
     * Does not change this query.
     * @return A new list of union queries, where each conjunctive query of each union query contains only one atom.
     */
    CNFQuery toCNF();

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

    boolean isOverDisjointResultVars();
}
