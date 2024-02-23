package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public abstract class BinaryBooleanFormula extends BinaryMTCQFormula
{
    public BinaryBooleanFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery left,
                                MetricTemporalConjunctiveQuery right)
    {
        super(temporalKb, isDistinct, left, right);
    }

    public BinaryBooleanFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
    {
        super(parentFormula, left, right);
    }

    @Override
    public boolean isTemporal()
    {
        return getLeftSubFormula().isTemporal() || getRightSubFormula().isTemporal();
    }
}
