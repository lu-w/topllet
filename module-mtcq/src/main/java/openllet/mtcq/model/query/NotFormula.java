package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class NotFormula extends UnaryBooleanFormula
{
    public NotFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
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
}
