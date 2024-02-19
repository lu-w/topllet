package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BoundedEventuallyFormula extends BoundedUnaryTemporalFormula
{
    public BoundedEventuallyFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula,
                                    int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, subFormula, lowerBound, upperBound);
    }

    public BoundedEventuallyFormula(MTCQFormula parentFormula, MTCQFormula subFormula, int lowerBound, int upperBound)
    {
        super(parentFormula, subFormula, lowerBound, upperBound);
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        return "F_" + intervalToString() + "(" + getSubFormula().toString(propositions) + ")";
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public BoundedEventuallyFormula copy()
    {
        return new BoundedEventuallyFormula(getTemporalKB(), isDistinct(), getSubFormula().copy(), getLowerBound(),
                getUpperBound());
    }
}
