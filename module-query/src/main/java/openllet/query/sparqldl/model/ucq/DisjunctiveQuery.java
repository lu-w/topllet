package openllet.query.sparqldl.model.ucq;

import openllet.query.sparqldl.model.AtomQuery;
import openllet.query.sparqldl.model.CompositeQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

import java.util.List;

/**
 * Class to represent disjunctive queries as a special case of a union query, i.e. of the form a v ... v b.
 */
public interface DisjunctiveQuery extends AtomQuery<DisjunctiveQuery>
{

}
