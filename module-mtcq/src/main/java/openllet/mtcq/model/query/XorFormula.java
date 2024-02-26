package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class XorFormula extends BinaryBooleanFormula
{
    public XorFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
    {
        super(temporalKb, isDistinct, left, right);
    }

    public XorFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
    {
        super(parentFormula, left, right);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "(" + getLeftSubFormula().toString(propositions) + ") ^ (" + getRightSubFormula().toString(propositions) +
                ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public XorFormula copy()
    {
        return new XorFormula(getTemporalKB(), isDistinct(), getLeftSubFormula().copy(), getRightSubFormula().copy());
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof XorFormula oXor)
        {
            MetricTemporalConjunctiveQuery tL = getLeftSubFormula();
            MetricTemporalConjunctiveQuery tR = getRightSubFormula();
            MetricTemporalConjunctiveQuery oL = oXor.getLeftSubFormula();
            MetricTemporalConjunctiveQuery oR = oXor.getRightSubFormula();
            return (tL.equals(oL) && tR.equals(oR)) || (tL.equals(oR) && tR.equals(oL));
        }
        else
            return false;
    }
}
