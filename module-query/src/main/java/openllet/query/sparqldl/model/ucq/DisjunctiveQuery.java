package openllet.query.sparqldl.model.ucq;

import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

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
    void addQuery(ConjunctiveQuery query);
}
