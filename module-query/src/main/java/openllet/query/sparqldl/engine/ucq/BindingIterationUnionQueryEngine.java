package openllet.query.sparqldl.engine.ucq;

import openllet.core.utils.Bool;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.ucq.UnionQuery;

import java.util.logging.Level;

/**
 * This engine just simply iterates through all possible bindings.
 */
public class BindingIterationUnionQueryEngine extends AbstractUnionQueryEngine
{
    protected UnionQueryBindingCandidateGenerator _bindingGenerator;

    @Override
    protected QueryResult execABoxQuery(UnionQuery q)
    {
        _bindingGenerator = new NaiveUnionQueryCandidateGenerator(q);
        QueryResult result = new QueryResultImpl(q);
        for (ResultBinding candidateBinding : _bindingGenerator)
        {
            if (_logger.isLoggable(Level.FINE))
                _logger.fine("Trying candidate binding: " + candidateBinding);
            QueryResult booleanResult = _booleanEngine.exec((UnionQuery) q.apply(candidateBinding));
            if (_logger.isLoggable(Level.FINE))
                _logger.fine("Boolean engine returned: " + (booleanResult.isEmpty() ? "false" : "true"));
            if (!booleanResult.isEmpty())
                result.add(candidateBinding);
            _bindingGenerator.informAboutResultForBinding(booleanResult.isEmpty() ? Bool.FALSE : Bool.TRUE);
        }
        return result;
    }
}
