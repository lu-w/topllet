package openllet.query.sparqldl.model.ubcq;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.AbstractCompositeQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.bcq.BCQQuery;

import java.util.*;

public class UBCQQueryImpl extends AbstractCompositeQuery<BCQQuery, UBCQQuery> implements UBCQQuery
{
    public UBCQQueryImpl(KnowledgeBase kb, boolean distinct)
    {
        super(kb, distinct);
    }

    public UBCQQueryImpl(Query<?> q)
    {
        super(q);
    }

    @Override
    protected String getCompositeDelimiter()
    {
        return "|";
    }

    @Override
    public List<UBCQQuery> split()
    {
        return null;
    }

    public UBCQQuery createQuery(KnowledgeBase kb, boolean isDistinct)
    {
        return new UBCQQueryImpl(kb, isDistinct);
    }
}
