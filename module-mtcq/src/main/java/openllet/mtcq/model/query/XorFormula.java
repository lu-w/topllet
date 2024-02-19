package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class XorFormula extends BinaryBooleanFormula
{
    public XorFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula left, MTCQFormula right)
    {
        super(temporalKb, isDistinct, left, right);
    }

    public XorFormula(MTCQFormula parentFormula, MTCQFormula left, MTCQFormula right)
    {
        super(parentFormula, left, right);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "(" + getLeftSubFormula().toString(propositions) + " ^ " + getRightSubFormula().toString(propositions) +
                ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public XorFormula copy()
    {
        return new XorFormula(getTemporalKB(), isDistinct(), getLeftSubFormula().copy(), getRightSubFormula().copy());
    }
}
