package openllet.query.sparqldl.engine.cncq;

import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public abstract class AbstractSemiBooleanCNCQEngine extends AbstractCNCQQueryEngine
{
    @Override
    public boolean supports(CNCQQuery q)
    {
        boolean booleanPosPart = true;
        for (ConjunctiveQuery sq : q.getPositiveQueries())
            booleanPosPart &= sq.isGround();
        return super.supports(q) && booleanPosPart;
    }
}
