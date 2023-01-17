package openllet.query.sparqldl.model;

/**
 * Class to represent disjunctive queries as a special case of a union query, i.e. of the form a v ... v b.
 */
public interface DisjunctiveQuery extends UnionQuery
{
    /**
     * We only allow to add queries that have at most one conjunct (i.e. are atomic).
     * @param query the query to add
     */
    @Override
    void addQuery(Query query);
}
