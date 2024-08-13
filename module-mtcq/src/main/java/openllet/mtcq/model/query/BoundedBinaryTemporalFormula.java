package openllet.mtcq.model.query;

import openllet.core.KnowledgeBase;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public abstract class BoundedBinaryTemporalFormula extends BoundedTemporalFormula
{
    private final MetricTemporalConjunctiveQuery _leftSubFormula;
    private final MetricTemporalConjunctiveQuery _rightSubFormula;

    public BoundedBinaryTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct,
                                        MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right,
                                        int lowerBound, int upperBound)
    {
        super(temporalKb, isDistinct, lowerBound, upperBound);
        assert(left != null && right != null);
        _leftSubFormula = left;
        _rightSubFormula = right;
        for (ConjunctiveQuery cq : left.getQueries())
            if (!getQueries().contains(cq))
                addQuery(cq);
        for (ConjunctiveQuery cq : right.getQueries())
            if (!getQueries().contains(cq))
                addQuery(cq);
    }

    public BoundedBinaryTemporalFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery leftSubFormula,
                                        MetricTemporalConjunctiveQuery rightSubFormula, int lowerBound, int upperBound)
    {
        this(parentFormula.getTemporalKB(), parentFormula.isDistinct(), leftSubFormula, rightSubFormula, lowerBound,
                upperBound);
    }

    @Override
    public void setTemporalKB(TemporalKnowledgeBase tkb)
    {
        super.setTemporalKB(tkb);
        _leftSubFormula.setTemporalKB(tkb);
        _rightSubFormula.setTemporalKB(tkb);
    }

    @Override
    public void setKB(KnowledgeBase kb)
    {
        super.setKB(kb);
        _leftSubFormula.setKB(kb);
        _rightSubFormula.setKB(kb);
    }

    public MetricTemporalConjunctiveQuery getLeftSubFormula()
    {
        return _leftSubFormula;
    }

    public MetricTemporalConjunctiveQuery getRightSubFormula()
    {
        return _rightSubFormula;
    }

    public boolean isTemporal()
    {
        return true;
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + _leftSubFormula.hashCode();
        result = PRIME * result + _rightSubFormula.hashCode();
        result = PRIME * result + getLowerBound();
        result = PRIME * result + getUpperBound();
        result = PRIME * result + _distVars.hashCode();
        result = PRIME * result + _resultVars.hashCode();
        result = PRIME * result + getUndistVars().hashCode();
        result = PRIME * result + _temporalKb.hashCode();
        return result;
    }
}
