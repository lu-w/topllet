package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public abstract class NullaryMTCQFormula extends MTCQFormula
{
    public NullaryMTCQFormula(MTCQFormula parentFormula)
    {
        super(parentFormula);
    }

    public NullaryMTCQFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb, isDistinct);
    }

    @Override
    public boolean isTemporal()
    {
        return false;
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + getClass().hashCode();
        result = PRIME * result + _distVars.hashCode();
        result = PRIME * result + _resultVars.hashCode();
        result = PRIME * result + getUndistVars().hashCode();
        result = PRIME * result + _temporalKb.hashCode();
        return result;
    }
}
