package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class EventuallyFormula extends UnaryTemporalFormula
{
    public EventuallyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }
}
