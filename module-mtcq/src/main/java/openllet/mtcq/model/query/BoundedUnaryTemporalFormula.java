package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public abstract class BoundedUnaryTemporalFormula extends BoundedTemporalFormula
{
    private final MTCQFormula _subFormula;

    public BoundedUnaryTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula,
                                       int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, lowerBound, upperBound);
        _subFormula = subFormula;
        for (ConjunctiveQuery cq : subFormula.getQueries())
            if (!getQueries().contains(cq))
                addQuery(cq);
    }

    public BoundedUnaryTemporalFormula(MTCQFormula parentFormula, MTCQFormula subFormula, int lowerBound,
                                       int upperBound)
    {
        this(parentFormula.getTemporalKB(), parentFormula.isDistinct(), subFormula, lowerBound, upperBound);
    }

    public MTCQFormula getSubFormula()
    {
        return _subFormula;
    }

    public boolean isTemporal()
    {
        return true;
    }
}
