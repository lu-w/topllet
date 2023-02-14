package openllet.query.sparqldl.model.cncq;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.AbstractCompositeQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

import java.util.ArrayList;
import java.util.List;

public class CNCQQueryImpl extends AbstractCompositeQuery<ConjunctiveQuery, CNCQQuery> implements CNCQQuery
{
    List<ConjunctiveQuery> _positiveQueries = new ArrayList<>();
    List<ConjunctiveQuery> _negativeQueries = new ArrayList<>();

    public CNCQQueryImpl(KnowledgeBase kb, boolean distinct)
    {
        super(kb, distinct);
    }

    public CNCQQueryImpl(Query<?> q)
    {
        super(q);
    }

    @Override
    protected String getCompositeDelimiter()
    {
        return "^";
    }

    @Override
    public List<CNCQQuery> split()
    {
        // TODO Lukas: implement - if splitting makes sense for CNCQQueries
        return List.of(this);
    }

    @Override
    public List<ConjunctiveQuery> getPositiveQueries()
    {
        return _positiveQueries;
    }

    @Override
    public List<ConjunctiveQuery> getNegativeQueries()
    {
        return _negativeQueries;
    }

    @Override
    public void addPositiveQuery(ConjunctiveQuery q)
    {
        super.addQuery(q);
        _positiveQueries.add(q);
    }

    @Override
    public void addNegativeQuery(ConjunctiveQuery q)
    {
        super.addQuery(q);
        _negativeQueries.add(q);
    }

    @Override
    public void setPositiveQueries(List<ConjunctiveQuery> positiveQueries)
    {
        super.addQueries(positiveQueries);
        _positiveQueries = positiveQueries;
    }

    @Override
    public void setNegativeQueries(List<ConjunctiveQuery> negativeQueries)
    {
        super.addQueries(negativeQueries);
        _negativeQueries = negativeQueries;
    }

    public CNCQQuery createQuery(KnowledgeBase kb, boolean isDistinct)
    {
        return new CNCQQueryImpl(kb, isDistinct);
    }
}
