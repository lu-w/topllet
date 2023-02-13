package openllet.query.sparqldl.engine.ucq;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Timer;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.shared.tools.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class AbstractUnionQueryEngine implements QueryExec
{
    public static final Logger _logger = Log.getLogger(BooleanUnionQueryEngineSimple.class);
    protected AbstractBooleanUnionQueryEngine _booleanEngine;

    public AbstractUnionQueryEngine()
    {
        _booleanEngine = new BooleanUnionQueryEngineSimple();
    }

    @Override
    public boolean supports(Query<?> q)
    {
        return q instanceof UnionQuery && !q.hasCycle();
    }

    @Override
    public QueryResult exec(Query<?> q)
    {
        // Implements some organizational features (logging, timing, etc.) around the actual union query engines
        assert(supports(q));
        UnionQuery unionQuery = (UnionQuery) q;

        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Exec union query query: " + unionQuery);

        KnowledgeBase kb = unionQuery.getKB();
        final long satCount = kb.getABox().getStats()._satisfiabilityCount;
        final long consCount = kb.getABox().getStats()._consistencyCount;

        final Timer timer = new Timer("UnionQueryEngine");
        timer.start();
        QueryResult results = new QueryResultImpl(unionQuery);
        if (unionQuery.getResultVars().isEmpty())
            results = _booleanEngine.exec(unionQuery);
        else if (unionQuery.getKB().getIndividualsCount() > 0)
            results = execABoxQuery(unionQuery);
        else
            _logger.warning("Got non-Boolean union query on a knowledge base with no individuals. Nothing to do here.");
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
