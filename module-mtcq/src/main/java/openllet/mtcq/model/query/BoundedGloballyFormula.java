package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BoundedGloballyFormula extends BoundedUnaryTemporalFormula
{
    public BoundedGloballyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula, int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, subFormula, lowerBound, upperBound);
    }

    public BoundedGloballyFormula(MTCQFormula parentFormula, MTCQFormula subFormula, int lowerBound, int upperBound)
    {
        super(parentFormula, subFormula, lowerBound, upperBound);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "G_" + intervalToString() + "(" + getSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public BoundedGloballyFormula copy()
    {
        return new BoundedGloballyFormula(getTemporalKB(), isDistinct(), getSubFormula().copy(), getLowerBound(),
                getUpperBound());
    }
}
