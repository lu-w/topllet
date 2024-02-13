package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class StrongNextFormula extends UnaryTemporalFormula
{
    public StrongNextFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }
}
