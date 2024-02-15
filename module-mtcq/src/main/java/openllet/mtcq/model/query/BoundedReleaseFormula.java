package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BoundedReleaseFormula extends BoundedBinaryTemporalFormula
{
    public BoundedReleaseFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula leftSubFormula,
                               MTCQFormula rightSubFormula, int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, leftSubFormula, rightSubFormula, lowerBound, upperBound);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "(" + getLeftSubFormula().toString(propositions) + " R_" + intervalToString() + " " +
                getRightSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }
}
