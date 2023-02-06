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

abstract public class AbstractBooleanUnionQueryEngine implements UnionQueryExec
{
    public static final Logger _logger = Log.getLogger(SimpleBooleanUnionQueryEngine.class);

    @Override
    public boolean supports(UnionQuery q)
    {
        return !q.hasCycle() && q.getUndistVars().isEmpty();
    }

    @Override
    public QueryResult exec(UnionQuery q)
    {
        // Implements some organizational features (logging, timing, etc.) around the actual Boolean union query engines
        assert(supports(q));

        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Exec Boolean ABox query: " + q);

        if (_logger.isLoggable(Level.INFO) && q.disjunctsShareUndistVars())
            _logger.info("Union query " + q + " contains disjuncts that share undistinguished variables. Will treat " +
                    "them as different variables.");

        KnowledgeBase kb = q.getKB();
        final long satCount = kb.getABox().getStats()._satisfiabilityCount;
        final long consCount = kb.getABox().getStats()._consistencyCount;

        final Timer timer = new Timer("BooleanUnionQueryEngine");
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
            _logger.fine("Result of Boolean union query : " + (isEntailed ? "entailed" : "not entailed"));
        }

        return results;
    }

    /**
     * Executes the entailment check of the given Boolean union query. This is the core functionality each Boolean union
     * query engine shall support.
     * @param q The union query to execute
     * @return True iff. the Boolean union query is entailed in its knowledge base
     */
    abstract protected boolean execBooleanABoxQuery(UnionQuery q);
}
