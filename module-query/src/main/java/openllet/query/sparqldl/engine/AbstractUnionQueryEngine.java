package openllet.query.sparqldl.engine;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Timer;
import openllet.query.sparqldl.model.QueryResult;
import openllet.query.sparqldl.model.QueryResultImpl;
import openllet.query.sparqldl.model.ResultBindingImpl;
import openllet.query.sparqldl.model.UnionQuery;
import openllet.shared.tools.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class AbstractUnionQueryEngine implements UnionQueryExec
{
    public static final Logger _logger = Log.getLogger(SimpleBooleanUnionQueryEngine.class);
    protected AbstractBooleanUnionQueryEngine _booleanEngine;

    protected AbstractUnionQueryEngine()
    {
        _booleanEngine = new SimpleBooleanUnionQueryEngine();
    }

    @Override
    public boolean supports(UnionQuery q)
    {
        return !q.hasCycle() && _booleanEngine.supports(q);
    }

    @Override
    public QueryResult exec(UnionQuery q)
    {
        // Implements some organizational features (logging, timing, etc.) around the actual union query engines
        assert(supports(q));

        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Exec union query query: " + q);

        if (_logger.isLoggable(Level.INFO) && q.disjunctsShareUndistVars())
            _logger.info("Union query " + q + " contains disjuncts that share undistinguished variables. Will treat " +
                    "them as different variables.");

        KnowledgeBase kb = q.getKB();
        final long satCount = kb.getABox().getStats()._satisfiabilityCount;
        final long consCount = kb.getABox().getStats()._consistencyCount;

        final Timer timer = new Timer("UnionQueryEngine");
        timer.start();
        QueryResult results;
        if (q.getDistVars().isEmpty())
            results = _booleanEngine.exec(q);
        else
            results = execABoxQuery(q);
        timer.stop();

        if (_logger.isLoggable(Level.FINE))
        {
            _logger.fine("Total time: " + timer.getLast() + " ms.");
            _logger.fine("Total satisfiability operations: " + (kb.getABox().getStats()._satisfiabilityCount - satCount));
            _logger.fine("Total consistency operations: " + (kb.getABox().getStats()._consistencyCount - consCount));
            _logger.fine("Result of Boolean union query : " + results);
        }

        return results;
    }

    protected abstract QueryResult execABoxQuery(UnionQuery q);
}
