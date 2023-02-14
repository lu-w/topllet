package openllet.query.sparqldl.engine.cncq;

import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;

public class CNCQQueryEngineSimple extends AbstractQueryEngine<CNCQQuery>
{
    @Override
    protected QueryResult execABoxQuery(CNCQQuery q)
    {
        return new QueryResultImpl(q);
    }
}
