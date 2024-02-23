package openllet.mtcq.engine;

import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.model.results.QueryResult;

public class DXNFEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    @Override
    protected QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        return null;
    }
}
