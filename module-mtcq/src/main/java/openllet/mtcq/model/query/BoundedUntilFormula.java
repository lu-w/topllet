package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BoundedUntilFormula extends BoundedBinaryTemporalFormula
{
    public BoundedUntilFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula leftSubFormula,
                               MTCQFormula rightSubFormula, int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, leftSubFormula, rightSubFormula, lowerBound, upperBound);
    }
}
