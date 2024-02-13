package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BoundedUnaryTemporalFormula extends BoundedTemporalFormula
{
    private final MTCQFormula _subFormula;

    public BoundedUnaryTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, int lowerBound,
                                       int upperBound, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct, lowerBound, upperBound);
        _subFormula = subFormula;
    }

    public MTCQFormula getSubFormula()
    {
        return _subFormula;
    }
}
