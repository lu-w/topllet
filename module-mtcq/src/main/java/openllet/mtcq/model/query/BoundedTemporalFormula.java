package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public abstract class BoundedTemporalFormula extends TemporalFormula
{
    private final int _lowerBound;
    private final int _upperBound;

    public BoundedTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct);
        if (lowerBound > upperBound || lowerBound < 0)
            throw new IllegalArgumentException("Invalid bound configuration [" + lowerBound + "," + upperBound +
                    " in formula " + this);
        _lowerBound = lowerBound;
        _upperBound = upperBound;
    }

    public BoundedTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, int upperBound)
    {
        this(temporalKb, isDistinct, 0, upperBound);
    }

    public int getLowerBound()
    {
        return _lowerBound;
    }

    public int getUpperBound()
    {
        return _upperBound;
    }
}
