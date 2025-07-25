package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BoundedEventuallyFormula extends BoundedUnaryTemporalFormula
{
    public BoundedEventuallyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery subFormula,
                                    int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, subFormula, lowerBound, upperBound);
    }

    public BoundedEventuallyFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery subFormula, int lowerBound, int upperBound)
    {
        super(parentFormula, subFormula, lowerBound, upperBound);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "F_" + intervalToString() + "(" + getSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public BoundedEventuallyFormula copy()
    {
        return new BoundedEventuallyFormula(getTemporalKB(), isDistinct(), getSubFormula().copy(), getLowerBound(),
                getUpperBound());
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof BoundedEventuallyFormula oF)
            return getSubFormula().equals(oF.getSubFormula()) && getLowerBound() == oF.getLowerBound() &&
                    getUpperBound() == oF.getUpperBound();
        else
            return false;
    }
}
