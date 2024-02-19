package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class NotFormula extends UnaryBooleanFormula
{
    public NotFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }

    public NotFormula(MTCQFormula parentFormula, MTCQFormula subFormula)
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
}
