package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public abstract class UnaryTemporalFormula extends UnaryMTCQFormula
{
    public UnaryTemporalFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MTCQFormula subFormula)
    {
        super(temporalKb, isDistinct, subFormula);
        for (ConjunctiveQuery cq : subFormula.getQueries())
            addQuery(cq);
    }

    @Override
    public boolean isTemporal()
    {
        return true;
    }
}
