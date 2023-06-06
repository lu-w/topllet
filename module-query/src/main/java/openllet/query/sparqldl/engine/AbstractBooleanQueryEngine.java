package openllet.query.sparqldl.engine;

import openllet.core.boxes.abox.ABox;
import openllet.core.utils.Timer;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBindingImpl;
import openllet.shared.tools.Log;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractBooleanQueryEngine<QueryType extends Query<QueryType>> implements QueryExec<QueryType>
{
    public static final Logger _logger = Log.getLogger(AbstractBooleanQueryEngine.class);

    protected ABox _abox = null;
    protected Timer _timer = null;

    @Override
    public boolean supports(QueryType q)
    {
        return !q.hasCycle() && q.hasOnlyClassesOrPropertiesInKB() && q.getDistVars().isEmpty();
    }

    @Override
    public QueryResult exec(QueryType q, ABox abox, Timer timer) throws IOException, InterruptedException
    {
        _timer = timer;
        _timer.start();
        QueryResult result = exec(q, abox);
        _timer.stop();
        return result;
    }

    @Override
    public QueryResult exec(QueryType q, ABox abox) throws IOException, InterruptedException
    {
        _abox = abox;
        return exec(q);
    }

    @Override
    public QueryResult exec(QueryType q) throws IOException, InterruptedException
    {
        if (_abox == null)
            _abox = q.getKB().getABox();

        // Implements some organizational features (logging, timing, etc.) around the actual Boolean CNC query engines
        if (!supports(q))
            throw new UnsupportedOperationException("Unsupported query " + q);

        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Exec Boolean ABox query: " + q);

        final long satCount = _abox.getStats()._satisfiabilityCount;
        final long consCount = _abox.getStats()._consistencyCount;

        final Timer timer = new Timer();
        timer.start();
        _logger.finest("Starting prerequisite consistency check.");
        q.getKB().ensureConsistency();
        _logger.finest("Consistency check passed; starting query engine.");
        boolean isEntailed = true;
        if (!q.isEmpty())
            isEntailed = execBooleanABoxQuery(q);
        QueryResult results = new QueryResultImpl(q);
        if (isEntailed)
            results.add(new ResultBindingImpl());
        timer.stop();

        if (_logger.isLoggable(Level.FINE))
        {
            _logger.fine("Total time: " + timer.getLast() + " ms.");
            _logger.fine("Total satisfiability operations: " + (_abox.getStats()._satisfiabilityCount - satCount));
            _logger.fine("Total consistency operations: " + (_abox.getStats()._consistencyCount - consCount));
            _logger.fine("Result of Boolean union query: " + (isEntailed ? "entailed" : "not entailed"));
        }

        return results;
    }

    /**
     * Executes the entailment check of the given Boolean query type. This is the core functionality each query
     * engine shall support.
     * @param q The Boolean query to execute
     * @return True iff. the Boolean query is entailed in its knowledge base
     */
    abstract protected boolean execBooleanABoxQuery(QueryType q) throws IOException, InterruptedException;
}
