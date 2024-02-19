package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class EndFormula extends NullaryMTCQFormula
{
    public EndFormula(MTCQFormula parentFormula)
    {
        super(parentFormula);
    }

    public EndFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb, isDistinct);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "end";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public EndFormula copy()
    {
        return new EndFormula(getTemporalKB(), isDistinct());
    }
}
