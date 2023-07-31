package openllet.query.sparqldl.engine.ucq;

import openllet.core.OpenlletOptions;
import openllet.core.utils.Bool;
import openllet.query.sparqldl.engine.QueryBindingCandidateGenerator;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.engine.QueryResultBasedBindingCandidateGenerator;
import openllet.query.sparqldl.engine.cq.CombinedQueryEngine;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.results.MultiQueryResults;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.ucq.CNFQuery;
import openllet.query.sparqldl.model.ucq.UnionQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * This engine just simply iterates through all possible bindings.
 */
public class UnionQueryEngineSimple extends AbstractUnionQueryEngine
{
    public enum BindingTime { BEFORE_CNF, AFTER_CNF }

    protected BindingTime _bindingTime = BindingTime.BEFORE_CNF;
    protected QueryBindingCandidateGenerator _bindingGenerator;
    protected AbstractBooleanUnionQueryEngine _booleanEngine;

    public UnionQueryEngineSimple() {
        this._booleanEngine = new BooleanUnionQueryEngineSimple();
        super._booleanEngine = this._booleanEngine;
        if (!OpenlletOptions.UCQ_ENGINE_BINDING_BEFORE_CNF)
            _bindingTime = BindingTime.AFTER_CNF;
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
    protected QueryResult execABoxQuery(UnionQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
            throws IOException, InterruptedException
    {
        if (q.getQueries().size() == 0)
            return new QueryResultImpl(q);
        else if (q.getQueries().size() == 1)
            return new CombinedQueryEngine().exec(q.getQueries().get(0));
        else if (OpenlletOptions.UCQ_ENGINE_USE_UNDERAPPROXIMATING_SEMANTICS)
        {
            QueryExec<ConjunctiveQuery> cqEngine = new CombinedQueryEngine();
            QueryResult disjunctAnswers = cqEngine.exec(q.getQueries().get(0));
            for (ConjunctiveQuery disjunct : q.getQueries().subList(1, q.getQueries().size()))
                disjunctAnswers.addAll(cqEngine.exec(disjunct));
            QueryResult missingAnswers = disjunctAnswers.invert();
            if (restrictToBindings != null)
                restrictToBindings.addAll(missingAnswers);
        }
        return switch (_bindingTime)
        {
            case BEFORE_CNF -> execABoxQueryBindingBeforeCNF(q, excludeBindings, restrictToBindings);
            case AFTER_CNF -> execABoxQueryBindingAfterCNF(q, excludeBindings, restrictToBindings);
        };
    }

    protected QueryResult execABoxQueryBindingBeforeCNF(UnionQuery q, QueryResult excludeBindings,
                                                        QueryResult restrictToBindings)
            throws IOException, InterruptedException
    {
        // Note: we can not split the query here due to semantics. Queries can only be split after conversion to CNF.
        QueryResult result = new QueryResultImpl(q);
        // FETCH AND APPLY BINDINGS
        _bindingGenerator = new QueryResultBasedBindingCandidateGenerator(q);
        _bindingGenerator.excludeBindings(excludeBindings);
        _bindingGenerator.restrictToBindings(restrictToBindings);
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
        _bindingGenerator.doNotExcludeBindings();
        _bindingGenerator.doNotRestrictToBindings();
        return result;
    }

    protected QueryResult execABoxQueryBindingAfterCNF(UnionQuery q, QueryResult excludeBindings,
                                                       QueryResult restrictToBindings)
            throws IOException, InterruptedException
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
            _bindingGenerator = new QueryResultBasedBindingCandidateGenerator(cnfQueryPart);
            _bindingGenerator.excludeBindings(excludeBindings);
            _bindingGenerator.restrictToBindings(restrictToBindings);
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
            _bindingGenerator.doNotExcludeBindings();
            _bindingGenerator.doNotRestrictToBindings();
        }
        if (results.size() > 1)
            return new MultiQueryResults(q.getResultVars(), results);
        else if (results.size() == 1)
            return results.get(0);
        else
            return new QueryResultImpl(q).invert(); // empty query is trivially entailed
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
