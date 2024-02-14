package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class EmptyFormula extends MTCQFormula
{
    public EmptyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb, isDistinct);
    }

    @Override
    public boolean isTemporal()
    {
        return false;
    }
}
