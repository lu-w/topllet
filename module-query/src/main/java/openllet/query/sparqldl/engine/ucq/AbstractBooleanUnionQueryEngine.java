package openllet.query.sparqldl.engine.ucq;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Timer;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBindingImpl;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import openllet.query.sparqldl.model.ucq.CNFQuery;
import openllet.shared.tools.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class AbstractBooleanUnionQueryEngine implements QueryExec
{
    public static final Logger _logger = Log.getLogger(BooleanUnionQueryEngineSimple.class);

    @Override
    public boolean supports(Query<?> q)
    {
        return q instanceof UnionQuery && !q.hasCycle() && q.getDistVars().isEmpty();
    }

    @Override
    public QueryResult exec(Query<?> q)
    {
        // Implements some organizational features (logging, timing, etc.) around the actual Boolean union query engines
        assert(supports(q));
        UnionQuery unionQuery = (UnionQuery) q;

        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Exec Boolean ABox query: " + unionQuery);

        KnowledgeBase kb = unionQuery.getKB();
        final long satCount = kb.getABox().getStats()._satisfiabilityCount;
        final long consCount = kb.getABox().getStats()._consistencyCount;

        final Timer timer = new Timer("BooleanUnionQueryEngine");
        timer.start();
        boolean isEntailed = execBooleanABoxQuery(unionQuery);
        QueryResult results = new QueryResultImpl(unionQuery);
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
     * Executes the entailment check of the given Boolean union query. This is the core functionality each Boolean union
     * query engine shall support.
     * @param q The union query to execute
     * @return True iff. the Boolean union query is entailed in its knowledge base
     */
    abstract protected boolean execBooleanABoxQuery(UnionQuery q);

    /**
     * Executes the entailment check of the given Boolean query in CNF. Complementary to execBooleanABoxQuery for
     * UnionQuery, but directly runs on the CNF.
     * @param q The CNF query to execute
     * @return True iff. the Boolean CNF query is entailed in its knowledge base
     */
    abstract protected boolean execBooleanABoxQuery(CNFQuery q);
}
