package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class LogicalTrueFormula extends NullaryMTCQFormula
{
    public LogicalTrueFormula(MTCQFormula parentFormula)
    {
        super(parentFormula);
    }

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

    @Override
    public LogicalTrueFormula copy()
    {
        return new LogicalTrueFormula(getTemporalKB(), isDistinct());
    }
}
