package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BoundedEventuallyFormula extends BoundedUnaryTemporalFormula
{
    public BoundedEventuallyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, int lowerBound,
                                    int upperBound, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct, lowerBound, upperBound, subFormula);
    }
}
