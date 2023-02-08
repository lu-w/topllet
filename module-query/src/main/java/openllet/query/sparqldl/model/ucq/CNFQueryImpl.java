package openllet.query.sparqldl.model.ucq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.ATermUtils;
import openllet.query.sparqldl.model.AbstractQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.results.ResultBinding;

import java.util.ArrayList;
import java.util.List;

public class CNFQueryImpl extends AbstractQuery implements CNFQuery
{
    List<DisjunctiveQuery> _queries = new ArrayList<>();

    public CNFQueryImpl(KnowledgeBase kb, boolean distinct)
    {
        super(kb, distinct);
    }

    @Override
    public CNFQuery apply(ResultBinding binding)
    {
        final CNFQuery query = copy();
        query.setQueries(new ArrayList<>());
        for (DisjunctiveQuery disjunct : _queries)
        {
            DisjunctiveQuery boundDisjunct = (DisjunctiveQuery) disjunct.apply(binding);
            query.addQuery(boundDisjunct);
        }
        return query;
    }

    @Override
    public CNFQuery copy()
    {
        CNFQuery copy = new CNFQueryImpl(getKB(), isDistinct());
        for (DisjunctiveQuery q : _queries)
            copy.addQuery((DisjunctiveQuery) q.copy());
        return copy;
    }

    @Override
    public void setQueries(List<DisjunctiveQuery> queries)
    {
        _queries = queries;
    }

    @Override
    public void addQuery(DisjunctiveQuery query)
    {
        _queries.add(query);
    }

    @Override
    public List<DisjunctiveQuery> getQueries()
    {
        return _queries;
    }

    @Override
    public List<Query> split()
    {
        // TODO Lukas
        return List.of(this);
    }

    @Override
    public boolean hasCycle()
    {
        boolean hasCycle = false;
        for (DisjunctiveQuery q : _queries)
            hasCycle |= q.hasCycle();
        return hasCycle;
    }

    @Override
    public String toString()
    {
        return toString(false);
    }

    public String toString(final boolean multiLine)
    {
        final String indent = multiLine ? "     " : " ";
        final StringBuilder sb = new StringBuilder();

        sb.append(ATermUtils.toString(_name)).append("(");
        for (int i = 0; i < _resultVars.size(); i++)
        {
            final ATermAppl var = _resultVars.get(i);
            if (i > 0)
                sb.append(", ");
            sb.append(ATermUtils.toString(var));
        }
        sb.append(")");

        sb.append(" :-");

        List<DisjunctiveQuery> queries = _queries;
        if (_queries.size() == 0)
            queries = List.of((DisjunctiveQuery) this);
        for (int i = 0; i < queries.size(); i++)
        {
            final DisjunctiveQuery query = queries.get(i);
            if (i > 0)
            {
                sb.append(" v");
                if (multiLine)
                    sb.append("\n");
            }
            if (query.getQueries().size() == 1 && query.getQueries().get(0).getAtoms().size() > 0)
            {
                if (multiLine)
                    sb.append("\n");
                for (int j = 0; j < query.getQueries().get(0).getAtoms().size(); j++) {
                    final QueryAtom a = query.getQueries().get(0).getAtoms().get(j);
                    if (j > 0) {
                        sb.append(",");
                        if (multiLine)
                            sb.append("\n");
                    }

                    sb.append(indent);
                    sb.append(a.toString()); // TODO qNameProvider
                }
            }
        }

        sb.append(".");
        if (multiLine)
            sb.append("\n");
        return sb.toString();
    }
}
