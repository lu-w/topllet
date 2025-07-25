package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class PropositionalFalseFormula extends NullaryMTCQFormula
{
    public PropositionalFalseFormula(MTCQFormula parentFormula)
    {
        super(parentFormula);
    }

    public PropositionalFalseFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb, isDistinct);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "false";
    }

    @Override
    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public PropositionalFalseFormula copy()
    {
        return new PropositionalFalseFormula(getTemporalKB(), isDistinct());
    }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof PropositionalFalseFormula;
    }
}
