package openllet.query.sparqldl.engine.cncq;

import openllet.query.sparqldl.engine.AbstractBooleanQueryEngine;
import openllet.query.sparqldl.model.cncq.CNCQQuery;

public class BooleanCNCQQueryEngineSimple extends AbstractBooleanQueryEngine<CNCQQuery>
{
    @Override
    protected boolean execBooleanABoxQuery(CNCQQuery q)
    {
        return false;
    }
}
