package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class EmptyFormula extends MTCQFormula
{
    public EmptyFormula(MetricTemporalConjunctiveQuery parentFormula)
    {
        super(parentFormula);
    }

    public EmptyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb, isDistinct);
    }

    @Override
    public boolean isTemporal()
    {
        return false;
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public EmptyFormula copy()
    {
        return new EmptyFormula(getTemporalKB(), isDistinct());
    }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof EmptyFormula;
    }
}
