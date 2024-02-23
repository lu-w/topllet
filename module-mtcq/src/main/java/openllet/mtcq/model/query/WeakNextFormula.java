package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class WeakNextFormula extends UnaryTemporalFormula
{
    public WeakNextFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }

    public WeakNextFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery subFormula)
    {
        super(parentFormula, subFormula);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "X (" + getSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public WeakNextFormula copy()
    {
        return new WeakNextFormula(getTemporalKB(), isDistinct(), getSubFormula().copy());
    }
}
