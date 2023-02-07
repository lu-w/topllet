package openllet.query.sparqldl.engine.ucq;

import openllet.core.utils.Bool;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.ucq.CNFQuery;
import openllet.query.sparqldl.model.ucq.UnionQuery;

import java.util.logging.Level;

/**
 * This engine just simply iterates through all possible bindings.
 */
public class BindingIterationUnionQueryEngine extends AbstractUnionQueryEngine
{
    public enum BindingTime { BEFORE_CNF, AFTER_CNF };

    protected BindingTime _bindingTime = BindingTime.BEFORE_CNF;
    protected UnionQueryBindingCandidateGenerator _bindingGenerator;

    @Override
    protected QueryResult execABoxQuery(UnionQuery q)
    {
        return switch (_bindingTime)
        {
            case BEFORE_CNF -> execABoxQueryBindingBeforeCNF(q);
            case AFTER_CNF -> execABoxQueryBindingAfterCNF(q);
        };
    }

    protected QueryResult execABoxQueryBindingBeforeCNF(UnionQuery q)
    {
        _bindingGenerator = new NaiveUnionQueryCandidateGenerator(q);
        QueryResult result = new QueryResultImpl(q);
        // APPLY BINDINGS
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

    protected QueryResult execABoxQueryBindingAfterCNF(UnionQuery q)
    {
        // 1. ROLL-UP UCQ
        UnionQuery rolledUpUnionQuery = q.rollUp();
        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Rolled-up union query: " + rolledUpUnionQuery);

        // 2. CONVERT TO CNF
        CNFQuery cnfQuery = rolledUpUnionQuery.toCNF();
        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Rolled-up union query in CNF is: " + cnfQuery);

        // 3. APPLY BINDINGS
        _bindingGenerator = new NaiveUnionQueryCandidateGenerator(q);
        QueryResult result = new QueryResultImpl(q);
        for (ResultBinding candidateBinding : _bindingGenerator)
        {
            if (_logger.isLoggable(Level.FINE))
                _logger.fine("Trying candidate binding: " + candidateBinding);
            boolean booleanResult = _booleanEngine.execBooleanABoxQuery((CNFQuery) cnfQuery.apply(candidateBinding));
            if (_logger.isLoggable(Level.FINE))
                _logger.fine("Boolean engine returned: " + booleanResult);
            if (booleanResult)
                result.add(candidateBinding);
            _bindingGenerator.informAboutResultForBinding(booleanResult ? Bool.TRUE : Bool.FALSE);
        }
        return result;
    }

    public void setBindingTime(BindingTime bindingTime)
    {
        _bindingTime = bindingTime;
    }

    public BindingTime getBindingTime()
    {
        return _bindingTime;
    }
}
