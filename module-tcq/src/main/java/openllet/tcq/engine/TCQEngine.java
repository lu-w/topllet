package openllet.tcq.engine;

import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.engine.cncq.CNCQQueryEngineSimple;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.shared.tools.Log;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import java.util.logging.Logger;

public class TCQEngine extends AbstractQueryEngine<TemporalConjunctiveQuery>
        implements QueryExec<TemporalConjunctiveQuery>
{
    public static final Logger _logger = Log.getLogger(BooleanTCQEngine.class);

    private final QueryExec<CNCQQuery> _cncqQueryEngine = new CNCQQueryEngineSimple();

    public TCQEngine() {
        this._booleanEngine = new BooleanTCQEngine();
        super._booleanEngine = this._booleanEngine;
    }

    @Override
    protected QueryResult execABoxQuery(TemporalConjunctiveQuery q)
    {
        // general idea:
        // let CNCQ engine generate a first set of candidates for state 0, then keep track of those and call from then
        // on only the Boolean cncq engine. keep track -> attach sets of individuals to each current state
        return null;
    }
}
