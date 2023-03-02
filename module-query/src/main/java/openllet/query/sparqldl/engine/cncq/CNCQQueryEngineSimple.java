package openllet.query.sparqldl.engine.cncq;

import openllet.aterm.ATermAppl;
import openllet.core.utils.Bool;
import openllet.query.sparqldl.engine.QueryBindingCandidateGenerator;
import openllet.query.sparqldl.engine.QueryCandidateGeneratorNaive;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.*;

import java.util.ArrayList;
import java.util.List;
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
        List<ATermAppl> vars = new ArrayList<>(q.getPositiveResultVars());
        List<ATermAppl> inds = q.getKB().getIndividuals().stream().toList();
        _bindingGenerator = new QueryCandidateGeneratorNaive(inds, vars);
        for (ResultBinding candidateBinding : _bindingGenerator)
        {
            if (_logger.isLoggable(Level.FINE))
                _logger.fine("Trying candidate binding for positive part: " + candidateBinding);
            CNCQQuery partiallyBoundQuery = q.apply(candidateBinding);
            QueryResult partialResult = _semiBooleanEngine.exec(partiallyBoundQuery, _abox);
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
        // Special case: we have result variables that are constrained neither in the negative nor in the positive parts
        // We just add all possible bindings for those.
        List<ATermAppl> unconstrainedVars = q.getUnconstrainedResultVars();
        if (unconstrainedVars.size() > 0)
        {
            List<QueryResult> results = new ArrayList<>();
            results.add(result);
            for (ATermAppl var : unconstrainedVars)
            {
                QueryResult varRes = new QueryResultImpl(q);
                for (ATermAppl ind : inds)
                {
                    ResultBinding newBinding = new ResultBindingImpl();
                    newBinding.setValue(var, ind);
                    varRes.add(newBinding);
                }
                results.add(varRes);
            }
            vars.addAll(unconstrainedVars);
            result = new MultiQueryResults(vars, results);
        }
        return result;
    }
}
