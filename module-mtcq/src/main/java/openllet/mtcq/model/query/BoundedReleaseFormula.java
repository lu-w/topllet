package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BoundedReleaseFormula extends BoundedBinaryTemporalFormula
{
    public BoundedReleaseFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery leftSubFormula,
                                 MetricTemporalConjunctiveQuery rightSubFormula, int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, leftSubFormula, rightSubFormula, lowerBound, upperBound);
    }

    public BoundedReleaseFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery leftSubFormula, MetricTemporalConjunctiveQuery rightSubFormula,
                                 int lowerBound, int upperBound)
    {
        super(parentFormula, leftSubFormula, rightSubFormula, lowerBound, upperBound);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "(" + getLeftSubFormula().toString(propositions) + ") R_" + intervalToString() + " (" +
                getRightSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public BoundedReleaseFormula copy()
    {
        return new BoundedReleaseFormula(getTemporalKB(), isDistinct(), getLeftSubFormula().copy(),
                getRightSubFormula().copy(), getLowerBound(), getUpperBound());
    }
}
