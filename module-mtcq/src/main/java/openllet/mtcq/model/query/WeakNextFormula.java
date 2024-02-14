package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class WeakNextFormula extends UnaryTemporalFormula
{
    public WeakNextFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }

    @Override
    public String toString()
    {
        return "X (" + getSubFormula() + ")";
    }
}
