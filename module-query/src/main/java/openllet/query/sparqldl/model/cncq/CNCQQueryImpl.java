package openllet.query.sparqldl.model.cncq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.AbstractCompositeQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQueryImpl;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.results.ResultBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

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
    public CNCQQuery copy()
    {
        CNCQQuery copy = new CNCQQueryImpl(this);
        for (ConjunctiveQuery q : getPositiveQueries())
            copy.addPositiveQuery(q.copy());
        for (ConjunctiveQuery q : getNegativeQueries())
            copy.addNegativeQuery(q.copy());
        copy.setDistVars(new EnumMap<>(getDistVarsWithVarType()));
        copy.setResultVars(getResultVars());
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
                    q.addResultVar(var);
            // Adds distinguished variables and their types
            for (VarType varType : VarType.values())
                for (ATermAppl var : q.getDistVarsForType(varType))
                    if (!newQuery.getDistVarsForType(varType).contains(var))
                        q.addResultVar(var);
        }
        return newQuery;
    }

    @Override
    public CNCQQuery applyToPositivePart(ResultBinding binding)
    {
        CNCQQuery copy = new CNCQQueryImpl(this);
        for (ConjunctiveQuery q : getNegativeQueries())
            copy.addNegativeQuery(q.copy());
        for (ConjunctiveQuery q : getPositiveQueries())
            copy.addPositiveQuery(q.apply(binding));
        copy.setDistVars(new EnumMap<>(getDistVarsWithVarType()));
        copy.setResultVars(getResultVars());
        return copy;
    }

    @Override
    public List<ATermAppl> getPositiveResultVars()
    {
        List<ATermAppl> posVars = new ArrayList<>();
        for (ConjunctiveQuery q : getPositiveQueries())
            posVars.addAll(q.getResultVars());
        return posVars;
    }

    public CNCQQuery createQuery(KnowledgeBase kb, boolean isDistinct)
    {
        return new CNCQQueryImpl(kb, isDistinct);
    }
}
