package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class StrongNextFormula extends UnaryTemporalFormula
{
    public StrongNextFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }

    public StrongNextFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery subFormula)
    {
        super(parentFormula, subFormula);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "X[!] (" + getSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public StrongNextFormula copy()
    {
        return new StrongNextFormula(getTemporalKB(), isDistinct(), getSubFormula().copy());
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof StrongNextFormula oX)
            return getSubFormula().equals(oX.getSubFormula());
        else
            return false;
    }
}
