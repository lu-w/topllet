package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class LogicalTrueFormula extends NullaryMTCQFormula
{
    public LogicalTrueFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb, isDistinct);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "tt";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }
}
