package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public abstract class BoundedBinaryTemporalFormula extends BoundedTemporalFormula
{
    private final MTCQFormula _leftSubFormula;
    private final MTCQFormula _rightSubFormula;

    public BoundedBinaryTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct,
                                        MTCQFormula leftSubFormula, MTCQFormula rightSubFormula, int lowerBound,
                                        int upperBound)
    {
        super(temporalKb, isDistinct, lowerBound, upperBound);
        _leftSubFormula = leftSubFormula;
        _rightSubFormula = rightSubFormula;
    }

    public MTCQFormula getLeftSubFormula()
    {
        return _leftSubFormula;
    }

    public MTCQFormula getRightSubFormula()
    {
        return _rightSubFormula;
    }

    public boolean isTemporal()
    {
        return true;
    }
}
