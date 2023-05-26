package openllet.query.sparqldl.engine;

import openllet.core.boxes.abox.ABox;
import openllet.core.utils.Timer;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.shared.tools.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractQueryEngine<QueryType extends Query<QueryType>> implements QueryExec<QueryType>
{
    public static final Logger _logger = Log.getLogger(AbstractQueryEngine.class);

    protected AbstractBooleanQueryEngine<QueryType> _booleanEngine;

    protected ABox _abox = null;
    protected Timer _timer = null;

    /**
     * Sets the Boolean engine to be internally used for checking Boolean queries after binding.
     * @param booleanEngine the boolean query engine to use
     */
    public void setBooleanEngine(AbstractBooleanQueryEngine<QueryType> booleanEngine)
    {
        assert(booleanEngine != null);
        _booleanEngine = booleanEngine;
    }

    @Override
    public boolean supports(QueryType q)
    {
        return !q.hasCycle() && q.hasOnlyClassesOrPropertiesInKB();
    }

    @Override
    public QueryResult exec(QueryType q, ABox abox, Timer timer)
    {
        _timer = timer;
        timer.start();
        QueryResult result = exec(q, abox);
        timer.stop();
        return result;
    }

    @Override
    public QueryResult exec(QueryType q, ABox abox)
    {
        _abox = abox;
        return exec(q);
    }

    @Override
    public QueryResult exec(QueryType q)
    {
        if (_abox == null)
            _abox = q.getKB().getABox();

        // Implements some organizational features (logging, timing, etc.) around the actual Boolean query engines
        if (!supports(q))
            throw new UnsupportedOperationException("Unsupported query " + q);

        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Exec Boolean ABox query: " + q);

        final long satCount = _abox.getStats()._satisfiabilityCount;
        final long consCount = _abox.getStats()._consistencyCount;

        final Timer timer = new Timer();
        timer.start();
        QueryResult results = new QueryResultImpl(q);
        // We can only check the query if it is non-empty and the KB is consistent
        _logger.finest("Starting prerequisite consistency check.");
        q.getKB().ensureConsistency();
        _logger.finest("Consistency check passed; starting query engine.");
        if (!q.isEmpty())
        {
            // Use the Boolean engine if we can
            if (q.getResultVars().isEmpty() && _booleanEngine != null)
                results = _booleanEngine.exec(q, _abox);
            else if (q.getKB().getIndividualsCount() > 0)
                results = execABoxQuery(q);
            else
                _logger.warning("Got non-Boolean query on a knowledge base with no individuals. Nothing to do here.");
        }
        timer.stop();

        if (_logger.isLoggable(Level.FINE))
        {
            _logger.fine("Total time: " + timer.getLast() + " ms.");
            _logger.fine("Total satisfiability operations: " + (_abox.getStats()._satisfiabilityCount - satCount));
            _logger.fine("Total consistency operations: " + (_abox.getStats()._consistencyCount - consCount));
            _logger.fine("Result of Boolean union query : " + results);
        }

        return results;
    }

    protected abstract QueryResult execABoxQuery(QueryType q);
}
