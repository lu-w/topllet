package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class NotFormula extends UnaryBooleanFormula
{
    public NotFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }

    public NotFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery subFormula)
    {
        super(parentFormula, subFormula);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "!(" + getSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public NotFormula copy()
    {
        return new NotFormula(getTemporalKB(), isDistinct(), getSubFormula().copy());
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof NotFormula oN)
            return getSubFormula().equals(oN.getSubFormula());
        else
            return false;
    }
}
