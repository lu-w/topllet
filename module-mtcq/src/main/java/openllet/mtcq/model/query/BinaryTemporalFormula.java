package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public abstract class BinaryTemporalFormula extends BinaryMTCQFormula
{
    public BinaryTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery left,
                                 MetricTemporalConjunctiveQuery right)
    {
        super(temporalKb, isDistinct, left, right);
    }

    public BinaryTemporalFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
    {
        super(parentFormula, left, right);
    }

    @Override
    public boolean isTemporal()
    {
        return true;
    }
}
