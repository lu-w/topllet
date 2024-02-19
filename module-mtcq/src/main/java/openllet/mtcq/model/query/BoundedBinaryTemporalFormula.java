package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

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
        for (ConjunctiveQuery cq : leftSubFormula.getQueries())
            if (!getQueries().contains(cq))
                addQuery(cq);
        for (ConjunctiveQuery cq : rightSubFormula.getQueries())
            if (!getQueries().contains(cq))
                addQuery(cq);
    }

    public BoundedBinaryTemporalFormula(MTCQFormula parentFormula, MTCQFormula leftSubFormula,
                                        MTCQFormula rightSubFormula, int lowerBound, int upperBound)
    {
        this(parentFormula.getTemporalKB(), parentFormula.isDistinct(), leftSubFormula, rightSubFormula, lowerBound,
                upperBound);
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
