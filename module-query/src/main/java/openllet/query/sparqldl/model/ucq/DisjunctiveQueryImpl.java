package openllet.query.sparqldl.model.ucq;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.cq.Query;

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