package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class UntilFormula extends BinaryTemporalFormula
{
    public UntilFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
    {
        super(temporalKb, isDistinct, left, right);
    }

    public UntilFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
    {
        super(parentFormula, left, right);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "(" + getLeftSubFormula().toString(propositions) + ") U (" + getRightSubFormula().toString(propositions) +
                ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public UntilFormula copy()
    {
        return new UntilFormula(getTemporalKB(), isDistinct(), getLeftSubFormula().copy(), getRightSubFormula().copy());
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof UntilFormula oU)
            return getLeftSubFormula().equals(oU.getLeftSubFormula()) &&
                    getRightSubFormula().equals(oU.getRightSubFormula());
        else
            return false;
    }
}
