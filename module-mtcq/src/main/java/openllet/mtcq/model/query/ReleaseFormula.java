package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class ReleaseFormula extends BinaryTemporalFormula
{
    public ReleaseFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula leftSubFormula,
                        MTCQFormula rightSubFormula)
    {
        super(temporalKb, isDistinct, leftSubFormula, rightSubFormula);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "(" + getLeftSubFormula().toString(propositions) + " R " + getRightSubFormula().toString(propositions) +
                ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }
}
