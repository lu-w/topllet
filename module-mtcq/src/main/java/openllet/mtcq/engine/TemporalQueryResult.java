package openllet.mtcq.engine;

import openllet.core.utils.Pair;
import openllet.mtcq.model.query.ConjunctiveQueryFormula;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.mtcq.model.query.OrFormula;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.ucq.DisjunctiveQuery;

import javax.annotation.Nullable;
import java.util.*;

public class TemporalQueryResult
{
    private List<Pair<QueryResult, TemporalQueryResult>> _conjunctionOfDisjunctions = new ArrayList<>();
    private QueryResult _collapsed = null;
    private final MetricTemporalConjunctiveQuery _query;

    public TemporalQueryResult(MetricTemporalConjunctiveQuery query)
    {
        _query = query;
    }

    public void addNewConjunct(QueryResult atemporalResult)
    {
        _conjunctionOfDisjunctions.add(new Pair<>(atemporalResult, null));
        _collapsed = null;
    }

    public void addNewConjunct(QueryResult atemporalResult, TemporalQueryResult temporalResult)
    {
        _conjunctionOfDisjunctions.add(new Pair<>(atemporalResult, temporalResult));
        _collapsed = null;
    }

    public void removeConjunct(QueryResult result)
    {
        Set<Pair<QueryResult, TemporalQueryResult>> toRemove = new HashSet<>();
        for (Pair<QueryResult, TemporalQueryResult> conjunction : _conjunctionOfDisjunctions)
            if (conjunction.second != null && conjunction.second.equals(result))
                toRemove.add(conjunction);
        _conjunctionOfDisjunctions.removeAll(toRemove);
        _collapsed = null;
    }

    public MetricTemporalConjunctiveQuery getQuery()
    {
        return _query;
    }

    @Nullable
    public QueryResult collapse()
    {
        if (_collapsed == null)
        {
            QueryResult finalResult = null;
            for (Pair<QueryResult, TemporalQueryResult> conjunction : _conjunctionOfDisjunctions)
            {
                QueryResult conjunctionRes;
                if (conjunction.first != null)
                {
                    conjunctionRes = conjunction.first;
                    if (conjunction.second != null)
                        conjunctionRes.addAll(conjunction.second.collapse());
                }
                else
                {
                    conjunctionRes = conjunction.second.collapse();
                }
                if (finalResult == null)
                    finalResult = conjunctionRes;
                else
                    finalResult.retainAll(conjunctionRes);
            }
            _collapsed = finalResult;
            //_conjunctionOfDisjunctions = null;
        }
        return  _collapsed;
    }
}
