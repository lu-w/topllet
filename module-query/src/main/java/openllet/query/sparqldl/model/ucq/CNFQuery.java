package openllet.query.sparqldl.model.ucq;

import openllet.query.sparqldl.model.Query;

import java.util.List;

/**
 * A CNF query is a conjunction of disjunctive queries.
 */
public interface CNFQuery extends Query
{
    public void setQueries(List<DisjunctiveQuery> queries);

    public List<DisjunctiveQuery> getQueries();
}
