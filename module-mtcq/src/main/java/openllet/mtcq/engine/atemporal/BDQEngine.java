package openllet.mtcq.engine.atemporal;

import openllet.mtcq.model.query.ConjunctiveQueryFormula;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.mtcq.model.query.NotFormula;
import openllet.mtcq.model.query.OrFormula;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.model.results.QueryResult;

import static openllet.mtcq.engine.rewriting.MTCQSimplifier.flattenOr;

public class BDQEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    @Override
    public boolean supports(MetricTemporalConjunctiveQuery q)
    {
        if (q instanceof OrFormula qOr)
        {
            for (MetricTemporalConjunctiveQuery disjunct : flattenOr(qOr))
                if (!(disjunct instanceof NotFormula) && !(disjunct instanceof ConjunctiveQueryFormula))
                    return false;
            return true;
        }
        else
            return false;
    }

    @Override
    protected QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        return null;
    }
}
