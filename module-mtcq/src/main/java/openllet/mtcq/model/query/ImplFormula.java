package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class ImplFormula extends BinaryBooleanFormula
{
    public ImplFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula left, MTCQFormula right)
    {
        super(temporalKb, isDistinct, left, right);
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
}
