package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class LastFormula extends NullaryMTCQFormula
{
    public LastFormula(MTCQFormula parentFormula)
    {
        super(parentFormula);
    }

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

    @Override
    public LastFormula copy()
    {
        return new LastFormula(getTemporalKB(), isDistinct());
    }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof LastFormula;
    }
}
