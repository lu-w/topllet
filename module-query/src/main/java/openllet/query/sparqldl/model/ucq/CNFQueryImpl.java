package openllet.query.sparqldl.model.ucq;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.AbstractQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.results.ResultBinding;

import java.util.ArrayList;
import java.util.List;

// TODO Lukas
public class CNFQueryImpl extends AbstractQuery implements CNFQuery
{
    List<DisjunctiveQuery> _queries = new ArrayList<>();

    public CNFQueryImpl(KnowledgeBase kb, boolean distinct)
    {
        super(kb, distinct);
    }

    @Override
    public Query apply(ResultBinding binding)
    {
        return null;
    }

    @Override
    public Query copy()
    {
        return null;
    }

    @Override
    public void setQueries(List<DisjunctiveQuery> queries)
    {
        _queries = queries;
    }

    @Override
    public List<DisjunctiveQuery> getQueries()
    {
        return _queries;
    }

    @Override
    public boolean hasCycle()
    {
        boolean hasCycle = false;
        for (DisjunctiveQuery q : _queries)
            hasCycle |= q.hasCycle();
        return hasCycle;
    }
}
