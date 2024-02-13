package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public abstract class BinaryTemporalFormula extends BinaryMTCQFormula
{
    public BinaryTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula left, MTCQFormula right)
    {
        super(temporalKb, isDistinct, left, right);
    }

    @Override
    public boolean isTemporal()
    {
        return true;
    }
}
