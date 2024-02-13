package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public abstract class NullaryMTCQFormula extends MTCQFormula
{
    public NullaryMTCQFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb, isDistinct);
    }

    @Override
    public boolean isTemporal()
    {
        return false;
    }
}
