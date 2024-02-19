package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public abstract class TemporalFormula extends MTCQFormula
{
    public TemporalFormula(MTCQFormula parentFormula)
    {
        super(parentFormula);
    }

    public TemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb, isDistinct);
    }
}
