package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public abstract class BinaryMTCQFormula extends MTCQFormula
{
    private final MTCQFormula _leftSubFormula;
    private final MTCQFormula _rightSubFormula;

    public BinaryMTCQFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula left, MTCQFormula right)
    {
        super(temporalKb, isDistinct);
        left.setParentFormula(this);
        right.setParentFormula(this);
        _leftSubFormula = left;
        _rightSubFormula = right;
        for (ConjunctiveQuery cq : left.getQueries())
            if (!getQueries().contains(cq))
                addQuery(cq);
        for (ConjunctiveQuery cq : right.getQueries())
            if (!getQueries().contains(cq))
                addQuery(cq);
    }

    public BinaryMTCQFormula(MTCQFormula parentFormula, MTCQFormula left, MTCQFormula right)
    {
        this(parentFormula.getTemporalKB(), parentFormula.isDistinct(), left, right);
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
