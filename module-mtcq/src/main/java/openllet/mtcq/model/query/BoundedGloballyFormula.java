package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BoundedGloballyFormula extends BoundedUnaryTemporalFormula
{
    public BoundedGloballyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula,
                                  int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, subFormula, lowerBound, upperBound);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "G_" + intervalToString() + " (" + getSubFormula().toString(propositions) + ")";
    }
}
