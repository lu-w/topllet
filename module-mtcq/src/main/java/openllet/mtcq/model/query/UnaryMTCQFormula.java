package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public abstract class UnaryMTCQFormula extends MTCQFormula
{
    private final MetricTemporalConjunctiveQuery _subFormula;

    public UnaryMTCQFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery subFormula)
    {
        super(temporalKb, isDistinct);
        assert(subFormula != null);
        subFormula.setParentFormula(this);
        _subFormula = subFormula;
        for (ConjunctiveQuery cq : subFormula.getQueries())
            if (!getQueries().contains(cq))
                addQuery(cq);
    }

    public UnaryMTCQFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery subFormula)
    {
        this(parentFormula.getTemporalKB(), parentFormula.isDistinct(), subFormula);
    }

    public MetricTemporalConjunctiveQuery getSubFormula()
    {
        return _subFormula;
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + _subFormula.hashCode();
        result = PRIME * result + _distVars.hashCode();
        result = PRIME * result + _resultVars.hashCode();
        result = PRIME * result + getUndistVars().hashCode();
        return result;
    }
}
