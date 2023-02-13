package openllet.query.sparqldl.model;

import java.util.List;

public interface CompositeQuery<SubQueryType extends Query<? extends SubQueryType>,
        QueryType extends CompositeQuery<SubQueryType, QueryType>> extends Query<QueryType>
{
    /**
     * @return The list of queries that represents the composite query.
     */
    List<SubQueryType> getQueries();

    /**
     * Sets the list of queries that represents the composition.
     * Note: This does not reset the variables of this query - new ones from the new queries are added, however.
     *
     * @param queries The list of queries that represents the composition
     */
    void setQueries(List<SubQueryType> queries);

    /**
     * Adds the list of queries that represents the composition to the existing queries.
     *
     * @param queries The list of queries that represents the composition
     */
    void addQueries(List<SubQueryType> queries);

    /**
     * Adds a query to the composite query.
     *
     * @param query the query to add
     */
    void addQuery(final SubQueryType query);
}
