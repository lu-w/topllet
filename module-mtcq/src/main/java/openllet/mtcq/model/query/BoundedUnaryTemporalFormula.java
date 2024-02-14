package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public abstract class BoundedUnaryTemporalFormula extends BoundedTemporalFormula
{
    private final MTCQFormula _subFormula;

    public BoundedUnaryTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula,
                                       int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, lowerBound, upperBound);
        _subFormula = subFormula;
    }

    public MTCQFormula getSubFormula()
    {
        return _subFormula;
    }

    public boolean isTemporal()
    {
        return true;
    }
}
