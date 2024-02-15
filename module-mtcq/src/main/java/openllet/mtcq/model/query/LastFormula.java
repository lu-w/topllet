package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class LastFormula extends NullaryMTCQFormula
{
    public LastFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb, isDistinct);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "last";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }
}
