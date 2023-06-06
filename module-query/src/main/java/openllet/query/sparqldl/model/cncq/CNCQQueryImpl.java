package openllet.query.sparqldl.model.cncq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.AbstractCompositeQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQueryImpl;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.results.ResultBinding;

import java.util.*;

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
    public void addQuery(ConjunctiveQuery q)
    {
        if (q.isNegated())
            addNegativeQuery(q);
        else
            addPositiveQuery(q);
    }

    @Override
    public List<ConjunctiveQuery> getPositiveQueries()
    {
        return Collections.unmodifiableList(_positiveQueries);
    }

    @Override
    public List<ConjunctiveQuery> getNegativeQueries()
    {
        return Collections.unmodifiableList(_negativeQueries);
    }

    @Override
    public void addPositiveQuery(ConjunctiveQuery q)
    {
        super.addQuery(q);
        q.setNegation(false);
        _positiveQueries.add(q);
    }

    @Override
    public void addNegativeQuery(ConjunctiveQuery q)
    {
        super.addQuery(q);
        q.setNegation(true);
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

    @Override
    public CNCQQuery apply(final ResultBinding binding)
    {
        final CNCQQuery query = copy();
        for (ATermAppl var : binding.getAllVariables())
            query.removeDistVar(var);
        query.setNegativeQueries(new ArrayList<>());
        query.setPositiveQueries(new ArrayList<>());
        query.setQueries(new ArrayList<>());
        for (ConjunctiveQuery q : getPositiveQueries())
            query.addPositiveQuery(q.apply(binding));
        for (ConjunctiveQuery q : getNegativeQueries())
            query.addNegativeQuery(q.apply(binding));
        return query;
    }

    @Override
    public CNCQQuery copy()
    {
        CNCQQuery copy = new CNCQQueryImpl(this);
        for (ConjunctiveQuery q : getPositiveQueries())
            copy.addPositiveQuery(q.copy());
        for (ConjunctiveQuery q : getNegativeQueries())
            copy.addNegativeQuery(q.copy());
        copy.setDistVars(new EnumMap<>(getDistVarsWithVarType()));
        copy.setResultVars(new ArrayList<>(getResultVars()));
        return copy;
    }

    @Override
    public ConjunctiveQuery mergePositiveQueries()
    {
        ConjunctiveQuery newQuery = new ConjunctiveQueryImpl(this);
        for (ConjunctiveQuery q : _positiveQueries)
        {
            // Copies atoms
            for (QueryAtom a : q.getAtoms())
                if (!newQuery.getAtoms().contains(a))
                    newQuery.add(a.copy());
            // Adds result variables
            for (ATermAppl var : q.getResultVars())
                if (!newQuery.getResultVars().contains(var))
                    newQuery.addResultVar(var);
            // Adds distinguished variables and their types
            for (VarType varType : VarType.values())
                for (ATermAppl var : q.getDistVarsForType(varType))
                    if (!newQuery.getDistVarsForType(varType).contains(var))
                        newQuery.addDistVar(var, varType);
        }
        return newQuery;
    }

    @Override
    public List<ATermAppl> getPositiveResultVars()
    {
        Set<ATermAppl> posVars = new HashSet<>();
        for (ConjunctiveQuery q : getPositiveQueries())
            posVars.addAll(q.getResultVars());
        return posVars.stream().toList();
    }

    @Override
    public List<ATermAppl> getUnconstrainedResultVars()
    {
        List<ATermAppl> unconstrainedVars = new ArrayList<>();
        for (ATermAppl remainingVar : getResultVars())
        {
            boolean notContained = true;
            for (ConjunctiveQuery q : getQueries())
                if (q.getResultVars().contains(remainingVar))
                    notContained = false;
            if (notContained)
                unconstrainedVars.add(remainingVar);
        }
        return unconstrainedVars;
    }

    public CNCQQuery createQuery(KnowledgeBase kb, boolean isDistinct)
    {
        return new CNCQQueryImpl(kb, isDistinct);
    }
}
