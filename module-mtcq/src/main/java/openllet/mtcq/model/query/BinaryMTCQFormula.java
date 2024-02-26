package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public abstract class BinaryMTCQFormula extends MTCQFormula
{
    private final MetricTemporalConjunctiveQuery _leftSubFormula;
    private final MetricTemporalConjunctiveQuery _rightSubFormula;

    public BinaryMTCQFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
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

    public BinaryMTCQFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
    {
        this(parentFormula.getTemporalKB(), parentFormula.isDistinct(), left, right);
    }

    public MetricTemporalConjunctiveQuery getLeftSubFormula()
    {
        return _leftSubFormula;
    }

    public MetricTemporalConjunctiveQuery getRightSubFormula()
    {
        return _rightSubFormula;
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + _leftSubFormula.hashCode();
        result = PRIME * result + _rightSubFormula.hashCode();
        result = PRIME * result + _distVars.hashCode();
        result = PRIME * result + _resultVars.hashCode();
        result = PRIME * result + getUndistVars().hashCode();
        return result;
    }
}
