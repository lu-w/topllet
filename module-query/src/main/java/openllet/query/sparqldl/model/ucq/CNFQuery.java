package openllet.query.sparqldl.model.ucq;

import openllet.query.sparqldl.model.CompositeQuery;

/**
 * A CNF query is a conjunction of disjunctive queries.
 */
public interface CNFQuery extends CompositeQuery<DisjunctiveQuery, CNFQuery>
{

}
