package openllet.query.sparqldl.model.ucq;

import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.results.ResultBinding;

import java.util.List;

public interface UnionQuery extends Query
{

    /**
     * @return The list of queries that represents the union.
     */
    List<ConjunctiveQuery> getQueries();

    /**
     * Sets the list of queries that represents the union.
     *
     * @param queries The list of queries that represents the union
     */
    void setQueries(List<ConjunctiveQuery> queries);


    /**
     * Adds a query to the union query.
     *
     * @param query the query to add
     */
    void addQuery(final ConjunctiveQuery query);

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
     * Checks if one of the disjuncts contains a cycle in its query graph. Note that this function only looks for
     * cycles in property atoms. It ignores other kind of atom types (e.g. same as).
     * @return True iff one of the disjuncts contains a cycle
     */
    boolean hasCycle();
}
