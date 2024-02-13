package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public abstract class UnaryMTCQFormula extends MTCQFormula
{
    private final MTCQFormula _subFormula;

    public UnaryMTCQFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct);
        _subFormula = subFormula;
    }

    public MTCQFormula getSubFormula()
    {
        return _subFormula;
    }
}
