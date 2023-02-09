package openllet.query.sparqldl.model;

import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

import java.util.List;

public interface CompositeQuery extends Query
{
    /**
     * @return The list of queries that represents the composite query.
     */
    List<Query> getQueries();

    /**
     * Sets the list of queries that represents the union.
     *
     * @param queries The list of queries that represents the union
     */
    void setQueries(List<Query> queries);

    /**
     * Adds a query to the union query.
     *
     * @param query the query to add
     */
    void addQuery(final Query query);
}
