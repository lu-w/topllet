package openllet.query.sparqldl.model.ucq;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.AbstractAtomQuery;
import openllet.query.sparqldl.model.Query;

import java.util.List;

public class DisjunctiveQueryImpl extends AbstractAtomQuery<DisjunctiveQuery> implements DisjunctiveQuery
{

    public DisjunctiveQueryImpl(KnowledgeBase kb, boolean distinct)
    {
        super(kb, distinct);
    }

    public DisjunctiveQueryImpl(Query<?> q)
    {
        this(q.getKB(), q.isDistinct());
    }

    @Override
    public List<DisjunctiveQuery> split() {
        // Disjunctive queries shall not be split due to their semantics.
        _logger.fine("Tried to split a disjunctive query, but disjunctive queries shall not be split.");
        return List.of(this);
    }

    @Override
    public String getAtomDelimiter()
    {
        return "v";
    }

    public DisjunctiveQuery createQuery(KnowledgeBase kb, boolean isDistinct)
    {
        return new DisjunctiveQueryImpl(kb, isDistinct);
    }
}
