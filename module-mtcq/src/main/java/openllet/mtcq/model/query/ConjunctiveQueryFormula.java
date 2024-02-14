package openllet.mtcq.model.query;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

public class ConjunctiveQueryFormula extends MTCQFormula
{
    private final ConjunctiveQuery _cq;

    public ConjunctiveQueryFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct,
                                   ConjunctiveQuery conjunctiveQuery)
    {
        super(temporalKb, isDistinct);
        _cq = conjunctiveQuery;
    }

    @Override
    public boolean isTemporal()
    {
        return false;
    }

    public ConjunctiveQuery getConjunctiveQuery()
    {
        return _cq;
    }
}
