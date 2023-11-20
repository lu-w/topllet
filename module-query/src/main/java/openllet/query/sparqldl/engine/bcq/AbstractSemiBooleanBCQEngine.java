package openllet.query.sparqldl.engine.bcq;

import openllet.query.sparqldl.model.bcq.BCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public abstract class AbstractSemiBooleanBCQEngine extends AbstractBCQQueryEngine
{
    @Override
    public boolean supports(BCQQuery q)
    {
        boolean booleanPosPart = true;
        for (ConjunctiveQuery sq : q.getPositiveQueries())
            booleanPosPart &= sq.getDistVars().isEmpty();
        return super.supports(q) && booleanPosPart;
    }
}
