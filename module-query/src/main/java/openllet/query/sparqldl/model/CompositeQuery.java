package openllet.query.sparqldl.model;

import java.util.List;

public interface CompositeQuery<SubQueryType extends Query<SubQueryType>, QueryType extends CompositeQuery<SubQueryType, QueryType>> extends Query<QueryType>
{
    /**
     * @return The list of queries that represents the composite query.
     */
    List<SubQueryType> getQueries();

    /**
     * Sets the list of queries that represents the union.
     *
     * @param queries The list of queries that represents the union
     */
    void setQueries(List<SubQueryType> queries);

    /**
     * Adds a query to the union query.
     *
     * @param query the query to add
     */
    void addQuery(final SubQueryType query);
}
