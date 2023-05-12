package openllet.tcq.engine;

import openllet.tcq.model.query.TemporalConjunctiveQuery;

public interface BooleanTCQEngine
{
    boolean supports(TemporalConjunctiveQuery tcq);

    boolean exec(TemporalConjunctiveQuery tcq);
}
