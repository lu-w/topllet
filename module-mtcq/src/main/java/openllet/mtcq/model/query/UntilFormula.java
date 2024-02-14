package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class UntilFormula extends BinaryTemporalFormula
{
    public UntilFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula leftSubFormula,
                        MTCQFormula rightSubFormula)
    {
        super(temporalKb, isDistinct, leftSubFormula, rightSubFormula);
    }

    @Override
    public String toString()
    {
        return "(" + getLeftSubFormula() + " U " + getRightSubFormula() + ")";
    }
}
