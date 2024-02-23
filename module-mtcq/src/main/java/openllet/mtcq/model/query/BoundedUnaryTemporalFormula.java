package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public abstract class BoundedUnaryTemporalFormula extends BoundedTemporalFormula
{
    private final MetricTemporalConjunctiveQuery _subFormula;

    public BoundedUnaryTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery subFormula,
                                       int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, lowerBound, upperBound);
        _subFormula = subFormula;
        for (ConjunctiveQuery cq : subFormula.getQueries())
            if (!getQueries().contains(cq))
                addQuery(cq);
    }

    public BoundedUnaryTemporalFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery subFormula, int lowerBound,
                                       int upperBound)
    {
        this(parentFormula.getTemporalKB(), parentFormula.isDistinct(), subFormula, lowerBound, upperBound);
    }

    public MetricTemporalConjunctiveQuery getSubFormula()
    {
        return _subFormula;
    }

    public boolean isTemporal()
    {
        return true;
    }
}
