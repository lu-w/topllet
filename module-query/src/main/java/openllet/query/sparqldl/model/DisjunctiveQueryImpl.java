package openllet.query.sparqldl.model;

import openllet.core.KnowledgeBase;

public class DisjunctiveQueryImpl extends UnionQueryImpl implements DisjunctiveQuery
{

    public DisjunctiveQueryImpl(KnowledgeBase kb, boolean distinct)
    {
        super(kb, distinct);
    }

    public DisjunctiveQueryImpl(DisjunctiveQuery query)
    {
        super(query);
    }

    @Override
    public void addQuery(Query query)
    {
        assert(query.getAtoms().size() <= 1);
        super.addQuery(query);
    }
}
