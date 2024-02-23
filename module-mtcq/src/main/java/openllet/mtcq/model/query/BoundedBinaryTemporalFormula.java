package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public abstract class BoundedBinaryTemporalFormula extends BoundedTemporalFormula
{
    private final MetricTemporalConjunctiveQuery _leftSubFormula;
    private final MetricTemporalConjunctiveQuery _rightSubFormula;

    public BoundedBinaryTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct,
                                        MetricTemporalConjunctiveQuery leftSubFormula, MetricTemporalConjunctiveQuery rightSubFormula, int lowerBound,
                                        int upperBound)
    {
        super(temporalKb, isDistinct, lowerBound, upperBound);
        _leftSubFormula = leftSubFormula;
        _rightSubFormula = rightSubFormula;
        for (ConjunctiveQuery cq : leftSubFormula.getQueries())
            if (!getQueries().contains(cq))
                addQuery(cq);
        for (ConjunctiveQuery cq : rightSubFormula.getQueries())
            if (!getQueries().contains(cq))
                addQuery(cq);
    }

    public BoundedBinaryTemporalFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery leftSubFormula,
                                        MetricTemporalConjunctiveQuery rightSubFormula, int lowerBound, int upperBound)
    {
        this(parentFormula.getTemporalKB(), parentFormula.isDistinct(), leftSubFormula, rightSubFormula, lowerBound,
                upperBound);
    }

    public MetricTemporalConjunctiveQuery getLeftSubFormula()
    {
        return _leftSubFormula;
    }

    public MetricTemporalConjunctiveQuery getRightSubFormula()
    {
        return _rightSubFormula;
    }

    public boolean isTemporal()
    {
        return true;
    }
}
