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
    public Iterator<ResultBinding> iterator()
    {
        if (_restrictToBindings == null)
            _restrictToBindings = new QueryResultImpl(_query).invert();
        // TODO this query result based candidate generator is inefficient if _excludeBindings is not empty.
        if (_excludeBindings != null && !_excludeBindings.isEmpty())
            _restrictToBindings.removeAll(_excludeBindings);
        return _restrictToBindings.iterator();
    }
}
