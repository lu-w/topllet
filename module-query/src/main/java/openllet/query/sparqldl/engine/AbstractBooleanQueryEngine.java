package openllet.query.sparqldl.engine;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Timer;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBindingImpl;
import openllet.shared.tools.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractBooleanQueryEngine<QueryType extends Query<QueryType>> implements QueryExec<QueryType>
{
    public static final Logger _logger = Log.getLogger(AbstractBooleanQueryEngine.class);

    @Override
    public boolean supports(QueryType q)
    {
        return !q.hasCycle() && q.getDistVars().isEmpty();
    }

    @Override
    public QueryResult exec(QueryType q)
    {
        // Implements some organizational features (logging, timing, etc.) around the actual Boolean CNC query engines
        assert(supports(q));

        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Exec Boolean ABox query: " + q);

        KnowledgeBase kb = q.getKB();
        final long satCount = kb.getABox().getStats()._satisfiabilityCount;
        final long consCount = kb.getABox().getStats()._consistencyCount;

        final Timer timer = new Timer("BooleanCNCQueryEngine");
        timer.start();
        boolean isEntailed = execBooleanABoxQuery(q);
        QueryResult results = new QueryResultImpl(q);
        if (isEntailed)
            results.add(new ResultBindingImpl());
        timer.stop();

        if (_logger.isLoggable(Level.FINE))
        {
            _logger.fine("Total time: " + timer.getLast() + " ms.");
            _logger.fine("Total satisfiability operations: " + (kb.getABox().getStats()._satisfiabilityCount - satCount));
            _logger.fine("Total consistency operations: " + (kb.getABox().getStats()._consistencyCount - consCount));
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
    abstract protected boolean execBooleanABoxQuery(QueryType q);
}
