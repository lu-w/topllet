package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class ImplFormula extends BinaryBooleanFormula
{
    public ImplFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula left, MTCQFormula right)
    {
        super(temporalKb, isDistinct, left, right);
    }

    public ImplFormula(MTCQFormula parentFormula, MTCQFormula left, MTCQFormula right)
    {
        super(parentFormula, left, right);
    }

    @Override
    protected String toString(PropositionFactory propositions)
    {
        return "(" + getLeftSubFormula().toString(propositions) + " -> " + getRightSubFormula().toString(propositions) +
                ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public ImplFormula copy()
    {
        return new ImplFormula(getTemporalKB(), isDistinct(), getLeftSubFormula().copy(), getRightSubFormula().copy());
    }
}
