package openllet.query.sparqldl.model.ucq;

import openllet.query.sparqldl.model.CompositeQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;

import java.util.List;

/**
 * Class to represent disjunctive queries as a special case of a union query, i.e. of the form a v ... v b.
 */
public interface DisjunctiveQuery extends CompositeQuery<ConjunctiveQuery, DisjunctiveQuery>, Query.AtomQuery
{

}
