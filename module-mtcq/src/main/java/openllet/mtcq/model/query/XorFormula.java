package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class XorFormula extends BinaryBooleanFormula
{
    public XorFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula left, MTCQFormula right)
    {
        super(temporalKb, isDistinct, left, right);
    }

    @Override
    public String toString()
    {
        return "(" + getLeftSubFormula() + " ^ " + getRightSubFormula() + ")";
    }
}
