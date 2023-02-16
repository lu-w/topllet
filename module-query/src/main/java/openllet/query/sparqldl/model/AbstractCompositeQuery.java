package openllet.query.sparqldl.model;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.ATermUtils;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.query.sparqldl.model.ucq.CNFQuery;
import openllet.query.sparqldl.model.ucq.CNFQueryImpl;
import openllet.query.sparqldl.model.ucq.DisjunctiveQuery;

import java.util.*;

abstract public class AbstractCompositeQuery<SubQueryType extends Query<SubQueryType>,
        QueryType extends CompositeQuery<SubQueryType, QueryType>> extends AbstractQuery<QueryType>
        implements CompositeQuery<SubQueryType, QueryType>
{
    protected List<SubQueryType> _queries = new ArrayList<>();

    public AbstractCompositeQuery(KnowledgeBase kb, boolean distinct)
    {
        super(kb, distinct);
    }

    public AbstractCompositeQuery(Query<?> q)
    {
        super(q.getKB(), q.isDistinct());
    }

    @Override
    public List<SubQueryType> getQueries()
    {
        return _queries;
    }

    @Override
    public void setQueries(List<SubQueryType> queries)
    {
        _queries = new ArrayList<>();
        for (SubQueryType q : queries)
            addQuery(q);
    }

    @Override
    public void addQueries(List<SubQueryType> queries)
    {
        for (SubQueryType q : queries)
            addQuery(q);
    }

    @Override
    public void addQuery(final SubQueryType query)
    {
        _queries.add(query);
        // Propagates variables to the union query
        _individualsAndLiterals.addAll(query.getConstants());
        _allVars.addAll(query.getVars());
        for (ATermAppl resVar : query.getResultVars())
            if (!_resultVars.contains(resVar))
                _resultVars.add(resVar);
        for (final VarType type : VarType.values())
            _distVars.get(type).addAll(query.getDistVarsForType(type));
        // Updates the ground information (this may have changed due to the new disjunct)
        _ground &= query.isGround();
    }

    @Override
    public QueryType apply(final ResultBinding binding)
    {
        final QueryType query = copy();
        query.setQueries(new ArrayList<>());
        for (SubQueryType subQuery : _queries)
            query.addQuery(subQuery.apply(binding));
        return query;
    }

    @Override
    public QueryType copy()
    {
        QueryType copy = super.copy();
        for (SubQueryType q : _queries)
            copy.addQuery(q.copy());
        copy.setDistVars(new EnumMap<>(getDistVarsWithVarType()));
        copy.setResultVars(getResultVars());
        return copy;
    }

    @Override
    public boolean hasCycle()
    {
        boolean hasCycle = false;
        for (SubQueryType q : _queries)
            hasCycle |= q.hasCycle();
        return hasCycle;
    }

    @Override
    public boolean isEmpty()
    {
        boolean isEmpty = true;
        for (Query<?> q : _queries)
            if (!q.isEmpty())
            {
                isEmpty = false;
                break;
            }
        return isEmpty;
    }

    /**
     * @return A delimiter string representing the operator between the composited queries
     */
    protected abstract String getCompositeDelimiter();

    @Override
    public String toString()
    {
        return toString(false, false);
    }

    @Override
    public String toString(final boolean multiLine, final boolean onlyQueryBody)
    {
        final StringBuilder sb = new StringBuilder();

        if (!onlyQueryBody)
        {
            sb.append(ATermUtils.toString(_name)).append("(");
            for (int i = 0; i < _resultVars.size(); i++)
            {
                final ATermAppl var = _resultVars.get(i);
                if (i > 0)
                    sb.append(", ");
                sb.append(ATermUtils.toString(var));
            }
            sb.append(")").append(" :- ");
        }

        for (int i = 0; i < _queries.size(); i++)
        {
            final SubQueryType query = _queries.get(i);
            if (i > 0)
            {
                sb.append(" ").append(getCompositeDelimiter()).append(" ");
                if (multiLine)
                    sb.append("\n");
            }
            if (_queries.size() > 1)
                sb.append("(");
            sb.append(query.toString(multiLine, true));
            if (_queries.size() > 1)
                sb.append(")");
        }

        if (!onlyQueryBody)
            sb.append(".");
        if (multiLine)
            sb.append("\n");
        return sb.toString();
    }
}
