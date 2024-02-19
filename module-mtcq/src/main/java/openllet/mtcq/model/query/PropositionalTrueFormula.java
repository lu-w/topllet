package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class PropositionalTrueFormula extends NullaryMTCQFormula
{
    public PropositionalTrueFormula(MTCQFormula parentFormula)
    {
        super(parentFormula);
    }

    public PropositionalTrueFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb, isDistinct);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "true";
    }

    @Override
    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }
}
