package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class EventuallyFormula extends UnaryTemporalFormula
{
    public EventuallyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }

    public EventuallyFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery subFormula)
    {
        super(parentFormula, subFormula);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "F (" + getSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public EventuallyFormula copy()
    {
        return new EventuallyFormula(getTemporalKB(), isDistinct(), getSubFormula().copy());
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof BoundedEventuallyFormula oF)
            return getSubFormula().equals(oF.getSubFormula());
        else
            return false;
    }
}
