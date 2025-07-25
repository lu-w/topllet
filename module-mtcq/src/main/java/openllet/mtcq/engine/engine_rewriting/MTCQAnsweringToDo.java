package openllet.mtcq.engine.engine_rewriting;

import openllet.mtcq.engine.engine_rewriting.TemporalQueryResult;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.query.sparqldl.model.results.QueryResult;

/**
 * Data class for a query answering activity to be performed by the MTCQ engine. Contains the candidates which are
 * to be checked (null if anything shall be considered) and a pointer to the query result in which the results
 * shall be stored.
 */
class MTCQAnsweringToDo
{
    MetricTemporalConjunctiveQuery query;
    QueryResult candidates = null;
    TemporalQueryResult temporalQueryResult;

    MTCQAnsweringToDo(MetricTemporalConjunctiveQuery query, QueryResult candidates,
                      TemporalQueryResult temporalQueryResult)
    {
        this.query = query;
        this.candidates = candidates;
        this.temporalQueryResult = temporalQueryResult;
    }

    MTCQAnsweringToDo(MetricTemporalConjunctiveQuery query, TemporalQueryResult temporalQueryResult)
    {
        this.query = query;
        this.temporalQueryResult = temporalQueryResult;
    }
}