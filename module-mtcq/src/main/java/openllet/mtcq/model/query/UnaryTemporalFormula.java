package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public abstract class UnaryTemporalFormula extends UnaryMTCQFormula
{

    public UnaryTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
    }

    public UnaryTemporalFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery subFormula)
    {
        super(parentFormula, subFormula);
    }

    @Override
    public boolean isTemporal()
    {
        return true;
    }
}
