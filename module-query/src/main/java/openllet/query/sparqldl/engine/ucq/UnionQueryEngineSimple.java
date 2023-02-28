package openllet.query.sparqldl.engine.ucq;

import openllet.core.utils.Bool;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.engine.QueryBindingCandidateGenerator;
import openllet.query.sparqldl.engine.QueryCandidateGeneratorNaive;
import openllet.query.sparqldl.model.results.MultiQueryResults;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.ucq.CNFQuery;
import openllet.query.sparqldl.model.ucq.UnionQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * This engine just simply iterates through all possible bindings.
 */
public class UnionQueryEngineSimple extends AbstractUnionQueryEngine
{
    public enum BindingTime { BEFORE_CNF, AFTER_CNF };

    protected BindingTime _bindingTime = BindingTime.BEFORE_CNF;
    protected QueryBindingCandidateGenerator _bindingGenerator;
    protected AbstractBooleanUnionQueryEngine _booleanEngine;

    public UnionQueryEngineSimple() {
        this._booleanEngine = new BooleanUnionQueryEngineSimple();
        super._booleanEngine = this._booleanEngine;
    }

    public UnionQueryEngineSimple(BindingTime bindingTime)
    {
        this();
        setBindingTime(bindingTime);
    }

    public void setBooleanEngine(AbstractBooleanUnionQueryEngine booleanEngine)
    {
        this._booleanEngine = booleanEngine;
        super._booleanEngine = this._booleanEngine;
    }

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
        // Note: we can not split the query here due to semantics. Queries can only be split after conversion to CNF.
        QueryResult result = new QueryResultImpl(q);
        // FETCH AND APPLY BINDINGS
        _bindingGenerator = new QueryCandidateGeneratorNaive(q);
        for (ResultBinding candidateBinding : _bindingGenerator)
        {
            if (_logger.isLoggable(Level.FINE))
                _logger.fine("Trying candidate binding: " + candidateBinding);
            UnionQuery boundQuery = q.apply(candidateBinding);
            QueryResult booleanResult = _booleanEngine.exec(boundQuery, _abox);
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
        UnionQuery rolledUpUnionQuery = q.rollUp(true);
        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Rolled-up union query: " + rolledUpUnionQuery);

        // 2. CONVERT TO CNF
        CNFQuery cnfQuery = rolledUpUnionQuery.toCNF();
        if (_logger.isLoggable(Level.FINER))
            _logger.finer("Rolled-up union query in CNF is: " + cnfQuery);

        // 3. SPLIT CNF QUERY INTO DISJOINT PARTS
        List<CNFQuery> queries = cnfQuery.split();
        List<QueryResult> results = new ArrayList<>(queries.size());

        for (CNFQuery cnfQueryPart : queries)
        {
            // 4. APPLY BINDINGS
            QueryResult result = new QueryResultImpl(cnfQueryPart);
            _bindingGenerator = new QueryCandidateGeneratorNaive(cnfQueryPart);
            for (ResultBinding candidateBinding : _bindingGenerator)
            {
                if (_logger.isLoggable(Level.FINE))
                    _logger.fine("Trying candidate binding: " + candidateBinding);
                CNFQuery boundQuery = cnfQueryPart.apply(candidateBinding);
                boolean booleanResult = _booleanEngine.execBooleanABoxQuery(boundQuery, _abox);
                if (_logger.isLoggable(Level.FINE))
                    _logger.fine("Boolean engine returned: " + booleanResult);
                if (booleanResult)
                    result.add(candidateBinding);
                _bindingGenerator.informAboutResultForBinding(booleanResult ? Bool.TRUE : Bool.FALSE);
            }
            results.add(result);
        }
        return new MultiQueryResults(q.getResultVars(), results);
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
