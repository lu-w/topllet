package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class EquivFormula extends BinaryBooleanFormula
{
    public EquivFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
    {
        super(temporalKb, isDistinct, left, right);
    }

    public EquivFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
    {
        super(parentFormula, left, right);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "(" + getLeftSubFormula().toString(propositions) + ") <-> (" + getRightSubFormula().toString(propositions)
                + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public EquivFormula copy()
    {
        return new EquivFormula(getTemporalKB(), isDistinct(), getLeftSubFormula().copy(), getRightSubFormula().copy());
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof EquivFormula oAnd)
        {
            MetricTemporalConjunctiveQuery tL = getLeftSubFormula();
            MetricTemporalConjunctiveQuery tR = getRightSubFormula();
            MetricTemporalConjunctiveQuery oL = oAnd.getLeftSubFormula();
            MetricTemporalConjunctiveQuery oR = oAnd.getRightSubFormula();
            return (tL.equals(oL) && tR.equals(oR)) || (tL.equals(oR) && tR.equals(oL));
        }
        else
            return false;
    }
}
