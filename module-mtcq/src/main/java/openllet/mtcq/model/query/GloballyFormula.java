package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class GloballyFormula extends UnaryTemporalFormula
{
    public GloballyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }

    public GloballyFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery subFormula)
    {
        super(parentFormula, subFormula);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "G (" + getSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public GloballyFormula copy()
    {
        return new GloballyFormula(getTemporalKB(), isDistinct(), getSubFormula().copy());
    }
}
