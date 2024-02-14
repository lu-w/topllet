package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class GloballyFormula extends UnaryTemporalFormula
{
    public GloballyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }

    @Override
    protected String toString(PropositionFactory propositions)
    {
        return "G (" + getSubFormula().toString(propositions) + ")";
    }
}
