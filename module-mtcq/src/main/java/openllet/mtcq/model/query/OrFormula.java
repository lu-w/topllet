package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class OrFormula extends BinaryBooleanFormula
{
    public OrFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula left, MTCQFormula right)
    {
        super(temporalKb, isDistinct, left, right);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "(" + getLeftSubFormula().toString(propositions) + " | " + getRightSubFormula().toString(propositions) +
                ")";
    }

    protected void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }
}
