package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public class BinaryMTCQFormula extends MTCQFormula
{
    private final MTCQFormula _leftSubFormula;
    private final MTCQFormula _rightSubFormula;

    public BinaryMTCQFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula left, MTCQFormula right)
    {
        super(temporalKb, isDistinct);
        _leftSubFormula = left;
        _rightSubFormula = right;
    }

    public MTCQFormula getLeftSubFormula()
    {
        return _leftSubFormula;
    }

    public MTCQFormula getRightSubFormula()
    {
        return _rightSubFormula;
    }
}
