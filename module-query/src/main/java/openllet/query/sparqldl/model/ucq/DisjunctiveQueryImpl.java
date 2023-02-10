package openllet.query.sparqldl.model.ucq;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQueryImpl;
import openllet.query.sparqldl.model.cq.QueryAtom;

import java.util.ArrayList;
import java.util.List;

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
        copy.setDistVars(getDistVarsWithVarType());
        copy.setResultVars(getResultVars());
        return copy;
    }

    @Override
    public void add(QueryAtom atom)
    {
        ConjunctiveQuery wrappingQuery = new ConjunctiveQueryImpl(_kb, _distinct);
        wrappingQuery.add(atom);
        addQuery(wrappingQuery);
    }

    @Override
    public List<QueryAtom> getAtoms()
    {
        List<QueryAtom> unwrappedList = new ArrayList<>();
        for (Query q : _queries)
        {
            List<QueryAtom> atoms = ((ConjunctiveQuery) q).getAtoms();
            if (atoms.size() == 1)
                unwrappedList.add(atoms.get(0));

        }
        return unwrappedList;
    }
}
