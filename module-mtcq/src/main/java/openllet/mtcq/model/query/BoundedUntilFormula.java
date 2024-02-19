package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BoundedUntilFormula extends BoundedBinaryTemporalFormula
{

    public BoundedUntilFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula leftSubFormula,
                               MTCQFormula rightSubFormula, int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, leftSubFormula, rightSubFormula, lowerBound, upperBound);
    }

    public BoundedUntilFormula(MTCQFormula parentFormula, MTCQFormula leftSubFormula, MTCQFormula rightSubFormula,
                               int lowerBound, int upperBound)
    {
        super(parentFormula, leftSubFormula, rightSubFormula, lowerBound, upperBound);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "(" + getLeftSubFormula().toString(propositions) + ") U_" + intervalToString() + " (" +
                getRightSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public BoundedUntilFormula copy()
    {
        return new BoundedUntilFormula(getTemporalKB(), isDistinct(), getLeftSubFormula().copy(),
                getRightSubFormula().copy(), getLowerBound(), getUpperBound());
    }
}
