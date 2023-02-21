package openllet.query.sparqldl.engine.cncq;

import openllet.core.utils.Bool;
import openllet.query.sparqldl.engine.QueryBindingCandidateGenerator;
import openllet.query.sparqldl.engine.QueryCandidateGeneratorNaive;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;

import java.util.logging.Level;

public class CNCQQueryEngineSimple extends AbstractCNCQQueryEngine
{
    protected QueryBindingCandidateGenerator _bindingGenerator;
    private AbstractSemiBooleanCNCQEngine _semiBooleanEngine;

    public CNCQQueryEngineSimple()
    {
        super();
        this._semiBooleanEngine = new SemiBooleanCNCQEngineSimple();
    }

    public void setBooleanEngine(AbstractSemiBooleanCNCQEngine booleanEngine)
    {
        this._semiBooleanEngine = booleanEngine;
    }

    @Override
    protected QueryResult execABoxQuery(CNCQQuery q)
    {
        QueryResult result = new QueryResultImpl(q);
        // FETCH AND APPLY BINDINGS TO POSITIVE PARTS
        _bindingGenerator = new QueryCandidateGeneratorNaive(q.getKB().getIndividuals().stream().toList(),
                q.getPositiveResultVars());
        for (ResultBinding candidateBinding : _bindingGenerator)
        {
            if (_logger.isLoggable(Level.FINE))
                _logger.fine("Trying candidate binding for positive part: " + candidateBinding);
            CNCQQuery partiallyBoundQuery = q.applyToPositivePart(candidateBinding);
            QueryResult partialResult = _semiBooleanEngine.exec(partiallyBoundQuery);
            if (_logger.isLoggable(Level.FINE))
                _logger.fine("Boolean CNCQ engine returned: " + (partialResult.isEmpty() ? "false" : "true"));
            // We may have gotten n > 0 bindings from the semi-Boolean engine -> create a copy and merge current binding
            for (ResultBinding binding : partialResult)
            {
                ResultBinding copyBinding = candidateBinding.duplicate();
                copyBinding.merge(binding);
                result.add(copyBinding);
            }
            _bindingGenerator.informAboutResultForBinding(partialResult.isEmpty() ? Bool.FALSE : Bool.TRUE);
        }
        return result;
    }
}
