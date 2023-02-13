package openllet.query.sparqldl.model.cncq;

import openllet.query.sparqldl.model.CompositeQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

import java.util.List;

public interface CNCQQuery extends CompositeQuery<ConjunctiveQuery, CNCQQuery>
{
    List<ConjunctiveQuery> getPositiveQueries();

    List<ConjunctiveQuery> getNegativeQueries();

    void addPositiveQuery(ConjunctiveQuery q);

    void addNegativeQuery(ConjunctiveQuery q);

    void setPositiveQueries(List<ConjunctiveQuery> positiveQueries);

    void setNegativeQueries(List<ConjunctiveQuery> negativeQueries);
}
