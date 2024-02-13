package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public abstract class UnaryBooleanFormula extends UnaryMTCQFormula
{
    public UnaryBooleanFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }

    @Override
    public boolean isTemporal()
    {
        return getSubFormula().isTemporal();
    }
}
