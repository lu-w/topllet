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
        _leftSubFormula = left;
        _rightSubFormula = right;
        for (ConjunctiveQuery cq : left.getQueries())
            addQuery(cq);
        for (ConjunctiveQuery cq : right.getQueries())
            addQuery(cq);
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
