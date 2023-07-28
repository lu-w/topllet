package openllet.query.sparqldl.engine;

import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;

import java.util.Iterator;

public class QueryResultBasedBindingCandidateGenerator extends QueryBindingCandidateGenerator
{
    public QueryResultBasedBindingCandidateGenerator(Query<?> query)
    {
        super(query);
    }

    @Override
    public void excludeBindings(QueryResult bindings)
    {
        if (bindings != null)
        {
            _excludeBindings = bindings;
            // If we exclude something non-empty, then we need to copy _restrictToBindings to avoid side effects.
            if (_restrictToBindings != null && _excludeBindings.size() > 0)
                _restrictToBindings = _restrictToBindings.copy();
        }
    }

    @Override
    public void restrictToBindings(QueryResult bindings)
    {
        if (bindings != null)
        {
            if (_excludeBindings != null && _excludeBindings.size() > 0)
                _restrictToBindings = bindings.copy();
            else
                _restrictToBindings = bindings;
        }
    }

    @Override
    public Iterator<ResultBinding> iterator()
    {
        if (_restrictToBindings == null)
            _restrictToBindings = new QueryResultImpl(_query).invert();
        // TODO this query result based candidate generator is inefficient if _excludeBindings is not empty.
        if (_excludeBindings != null && !_excludeBindings.isEmpty())
            _restrictToBindings.removeAll(_excludeBindings);
        return _restrictToBindings.listIterator(); // list iteration is more efficient than set iteration
    }
}
