package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class EventuallyFormula extends UnaryTemporalFormula
{
    public EventuallyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }

    public EventuallyFormula(MTCQFormula parentFormula, MTCQFormula subFormula)
    {
        super(parentFormula, subFormula);
    }

    @Override
    protected String toString(PropositionFactory propositions)
    {
        return "F(" + getSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }
}
