package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class ReleaseFormula extends BinaryTemporalFormula
{
    public ReleaseFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
    {
        super(temporalKb, isDistinct, left, right);
    }

    public ReleaseFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
    {
        super(parentFormula, left, right);
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

    @Override
    public ReleaseFormula copy()
    {
        return new ReleaseFormula(getTemporalKB(), isDistinct(), getLeftSubFormula().copy(), getRightSubFormula().copy());
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof ReleaseFormula oRe)
            return getLeftSubFormula().equals(oRe.getLeftSubFormula()) &&
                    getRightSubFormula().equals(oRe.getRightSubFormula());
        else
            return false;
    }
}
