package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BoundedEventuallyFormula extends BoundedUnaryTemporalFormula
{
    public BoundedEventuallyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula,
                                    int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, subFormula, lowerBound, upperBound);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "F_" + intervalToString() + " (" + getSubFormula().toString(propositions) + ")";
    }
}
