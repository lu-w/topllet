package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class LogicalFalseFormula extends NullaryMTCQFormula
{
    public LogicalFalseFormula(MTCQFormula parentFormula)
    {
        super(parentFormula);
    }

    public LogicalFalseFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb, isDistinct);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "ff";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public LogicalFalseFormula copy()
    {
        return new LogicalFalseFormula(getTemporalKB(), isDistinct());
    }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof LogicalFalseFormula;
    }
}
