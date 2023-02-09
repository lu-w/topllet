package openllet.query.sparqldl.model.ucq;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.Query;
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

    /**
     * We only allow to add queries that have at most one conjunct (i.e. are atomic).
     * @param query the (conjunctive) query to add
     */
    @Override
    public void addQuery(Query query)
    {
        assert(query instanceof ConjunctiveQuery && ((ConjunctiveQuery) query).getAtoms().size() <= 1);
        super.addQuery(query);
    }

    @Override
    public DisjunctiveQuery copy()
    {
        DisjunctiveQuery copy = new DisjunctiveQueryImpl(this);
        for (Query q : _queries)
            copy.addQuery(q.copy());
        return copy;
    }
}
