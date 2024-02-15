package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class EndFormula extends NullaryMTCQFormula
{
    public EndFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb, isDistinct);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "end";
    }

    protected void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }
}
