package openllet.query.sparqldl.engine.cncq;

import openllet.aterm.ATermAppl;
import openllet.core.utils.Bool;
import openllet.query.sparqldl.engine.QueryBindingCandidateGenerator;
import openllet.query.sparqldl.engine.QueryCandidateGeneratorNaive;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CNCQQueryEngineSimple extends AbstractCNCQQueryEngine
{
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
    protected QueryResult execABoxQuery(CNCQQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
            throws IOException, InterruptedException
    {
        QueryResult result;

        if (q.getPositiveQueries().size() == 0)
            result = _semiBooleanEngine.exec(q, excludeBindings, restrictToBindings);
        else
        {
            result = new QueryResultImpl(q);
            // FETCH AND APPLY BINDINGS TO POSITIVE PARTS
            List<ATermAppl> vars = new ArrayList<>(q.getPositiveResultVars());
            List<ATermAppl> inds = q.getKB().getIndividuals().stream().toList();
            QueryBindingCandidateGenerator _bindingGenerator = new QueryCandidateGeneratorNaive(inds, vars);
            if (excludeBindings != null)
                _bindingGenerator.excludeBindings(excludeBindings);
            if (restrictToBindings != null)
                _bindingGenerator.restrictToBindings(restrictToBindings);
            for (ResultBinding candidateBinding : _bindingGenerator)
            {
                if (_logger.isLoggable(Level.FINE))
                    _logger.fine("Trying candidate binding for positive part: " + candidateBinding);
                CNCQQuery partiallyBoundQuery = q.apply(candidateBinding);
                QueryResult partialResult = _semiBooleanEngine.exec(partiallyBoundQuery, excludeBindings,
                        restrictToBindings);
                if (_logger.isLoggable(Level.FINE))
                    _logger.fine("Boolean CNCQ engine returned: " + (partialResult.isEmpty() ? "false" : "true"));
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
    public QueryResult exec(CNCQQuery query, QueryResult excludeBindings, QueryResult restrictToBindings)
            throws IOException, InterruptedException
    {
        return execABoxQuery(query, excludeBindings, restrictToBindings);
    }
}
