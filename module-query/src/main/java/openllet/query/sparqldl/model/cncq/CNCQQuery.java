package openllet.query.sparqldl.model.cncq;

import openllet.query.sparqldl.model.CompositeQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

import java.util.List;

public interface CNCQQuery extends CompositeQuery<ConjunctiveQuery, CNCQQuery>
{
    /**
     * @return an unmodifiable view on the positive queries (i.e. those that are not negated)
     */
    List<ConjunctiveQuery> getPositiveQueries();

    /**
     * @return an unmodifiable view on the negative queries (i.e. those that are negated)
     */
    List<ConjunctiveQuery> getNegativeQueries();

    /**
     * Adds a positive (non-negated) query to this composite query To be sure, it sets the negation flag of the given
     * query to false.
     * @param q the positive query to add
     */
    void addPositiveQuery(ConjunctiveQuery q);

    /**
     * Adds a negative (negated) query to this composite query. To be sure, it sets the negation flag of the given
     * query to true.
     * @param q the negative query to add
     */
    void addNegativeQuery(ConjunctiveQuery q);

    /**
     * Sets all positive queries (deletes the previous positive queries).
     * @param positiveQueries the new positive query list
     */
    void setPositiveQueries(List<ConjunctiveQuery> positiveQueries);

    /**
     * Sets all negative queries (deletes the previous negative queries).
     * @param negativeQueries the new negative query list
     */
    void setNegativeQueries(List<ConjunctiveQuery> negativeQueries);

    /**
     * Merges all the positive queries into a single, newly copied query.
     * @return a copy representing a single query of the positive subqueries of this query
     */
    ConjunctiveQuery mergePositiveQueries();
}
