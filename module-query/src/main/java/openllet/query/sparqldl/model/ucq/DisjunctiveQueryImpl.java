package openllet.query.sparqldl.model.ucq;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public class DisjunctiveQueryImpl extends UnionQueryImpl implements DisjunctiveQuery
{

    public DisjunctiveQueryImpl(KnowledgeBase kb, boolean distinct)
    {
        super(kb, distinct);
    }

    public DisjunctiveQueryImpl(DisjunctiveQuery q)
    {
        super(q);
    }

    @Override
    public void addQuery(ConjunctiveQuery query)
    {
        assert(query.getAtoms().size() <= 1);
        super.addQuery(query);
    }

    @Override
    public DisjunctiveQuery copy()
    {
        DisjunctiveQuery copy = new DisjunctiveQueryImpl(this);
        for (ConjunctiveQuery q : _queries)
            copy.addQuery((ConjunctiveQuery) q.copy());
        return copy;
    }
}
