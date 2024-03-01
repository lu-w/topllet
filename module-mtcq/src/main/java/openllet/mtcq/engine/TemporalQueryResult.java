package openllet.mtcq.engine;

import openllet.core.utils.Pair;
import openllet.query.sparqldl.model.results.QueryResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TemporalQueryResult
{
    private final List<Pair<QueryResult, TemporalQueryResult>> _conjunctionOfDisjunctions = new ArrayList<>();

    public void addNewConjunct(QueryResult atemporalResult)
    {
        _conjunctionOfDisjunctions.add(new Pair<>(atemporalResult, null));
    }

    public void addNewConjunct(TemporalQueryResult temporalResult)
    {
        _conjunctionOfDisjunctions.add(new Pair<>(null, temporalResult));
    }

    public void addNewConjunct(QueryResult atemporalResult, TemporalQueryResult temporalResult)
    {
        _conjunctionOfDisjunctions.add(new Pair<>(atemporalResult, temporalResult));
    }

    @Nullable
    public QueryResult collapse()
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
        return finalResult;
    }
}
