package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class EmptyFormula extends MTCQFormula
{
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
    protected String toString(PropositionFactory propositions)
    {
        return "";
    }

    protected void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }
}
