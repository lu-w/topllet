package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class GloballyFormula extends UnaryTemporalFormula
{
    public GloballyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }

    public GloballyFormula(MTCQFormula parentFormula, MTCQFormula subFormula)
    {
        super(parentFormula, subFormula);
    }

    @Override
    protected String toString(PropositionFactory propositions)
    {
        return "G(" + getSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public GloballyFormula copy()
    {
        return new GloballyFormula(getTemporalKB(), isDistinct(), getSubFormula().copy());
    }
}
