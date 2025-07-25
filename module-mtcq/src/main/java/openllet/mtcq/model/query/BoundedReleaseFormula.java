package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BoundedReleaseFormula extends BoundedBinaryTemporalFormula
{
    public BoundedReleaseFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct,
                                 MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right,
                                 int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, left, right, lowerBound, upperBound);
    }

    public BoundedReleaseFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery left,
                                 MetricTemporalConjunctiveQuery right,
                                 int lowerBound, int upperBound)
    {
        super(parentFormula, left, right, lowerBound, upperBound);
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

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof BoundedReleaseFormula oRe)
        {
            MetricTemporalConjunctiveQuery tL = getLeftSubFormula();
            MetricTemporalConjunctiveQuery tR = getRightSubFormula();
            MetricTemporalConjunctiveQuery oL = oRe.getLeftSubFormula();
            MetricTemporalConjunctiveQuery oR = oRe.getRightSubFormula();
            return (tL.equals(oL) && tR.equals(oR)) && getLowerBound() == oRe.getLowerBound() &&
                    getUpperBound() == oRe.getUpperBound();
        }
        else
            return false;
    }
}
