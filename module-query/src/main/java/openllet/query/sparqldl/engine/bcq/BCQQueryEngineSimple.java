package openllet.query.sparqldl.engine.bcq;

import openllet.core.utils.Bool;
import openllet.query.sparqldl.engine.QueryBindingCandidateGenerator;
import openllet.query.sparqldl.engine.QueryResultBasedBindingCandidateGenerator;
import openllet.query.sparqldl.model.bcq.BCQQuery;
import openllet.query.sparqldl.model.results.*;

import java.io.IOException;
import java.util.logging.Level;

public class BCQQueryEngineSimple extends AbstractBCQQueryEngine
{
    private AbstractSemiBooleanBCQEngine _semiBooleanEngine;

    public BCQQueryEngineSimple()
    {
        super();
        this._semiBooleanEngine = new SemiBooleanBCQEngineSimple();
    }

    public void setBooleanEngine(AbstractSemiBooleanBCQEngine booleanEngine)
    {
        this._semiBooleanEngine = booleanEngine;
    }

    @Override
    protected QueryResult execABoxQuery(BCQQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        QueryResult result;

        if (q.getPositiveQueries().isEmpty())
            result = _semiBooleanEngine.exec(q, excludeBindings, restrictToBindings);
        else
        {
            result = new QueryResultImpl(q);
            // FETCH AND APPLY BINDINGS TO POSITIVE PARTS
            QueryBindingCandidateGenerator _bindingGenerator =
                    new QueryResultBasedBindingCandidateGenerator(q.mergePositiveQueries());
            _bindingGenerator.excludeBindings(excludeBindings);
            _bindingGenerator.restrictToBindings(restrictToBindings);
            for (ResultBinding candidateBinding : _bindingGenerator)
            {
                if (_logger.isLoggable(Level.FINE))
                    _logger.fine("Trying candidate binding for positive part: " + candidateBinding);
                BCQQuery partiallyBoundQuery = q.apply(candidateBinding);
                QueryResult partialResult = _semiBooleanEngine.exec(partiallyBoundQuery, excludeBindings,
                        restrictToBindings);
                if (_logger.isLoggable(Level.FINE))
                    _logger.fine("Boolean BCQ engine returned: " + (partialResult.isEmpty() ? "false" : "true"));
                // We may have gotten n > 0 bindings from the semi-Boolean engine, create a copy and merge curr. binding
                for (ResultBinding binding : partialResult)
                {
                    ResultBinding copyBinding = candidateBinding.duplicate();
                    copyBinding.merge(binding);
                    result.add(copyBinding);
                }
                _bindingGenerator.informAboutResultForBinding(partialResult.isEmpty() ? Bool.FALSE : Bool.TRUE);
            }
            _bindingGenerator.doNotExcludeBindings();
            _bindingGenerator.doNotRestrictToBindings();
        }
        return result;
    }

    @Override
    public QueryResult exec(BCQQuery query, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        return execABoxQuery(query, excludeBindings, restrictToBindings);
    }
}
