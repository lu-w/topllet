package openllet.query.sparqldl.engine;

import openllet.core.utils.Pair;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.MultiQueryResults;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;

import java.util.HashMap;
import java.util.Map;

public class QueryCache
{
    private Map<Query<?>, Pair<QueryResult, QueryResult>> _cachedResults = new HashMap<>();

    /**
     *
     * @param query
     * @param candidates can be null (indicating everything)
     * @param result
     */
    public void add(Query<?> query, QueryResult candidates, QueryResult result)
    {
        if (result instanceof MultiQueryResults mqr)
            result = mqr.toQueryResultImpl(query);
        if (_cachedResults.containsKey(query))
        {
            _cachedResults.get(query).first.addAll(candidates.copy());
            _cachedResults.get(query).second.addAll(result.copy());
        }
        else
            _cachedResults.put(query, new Pair<>(candidates.copy(), result.copy()));
    }

    /**
     *
     * @param query
     * @param candidates can be null (indicating everything)
     * @return the cached results (first entry), and the candidates for which no cached result could be obtained
     *         (second entry).
     */
    public Pair<QueryResult, QueryResult> fetch(Query<?> query, QueryResult candidates)
    {
        if (_cachedResults.containsKey(query))
        {
            QueryResult cachedCandidates = _cachedResults.get(query).first;
            QueryResult candidatesWithNoCache;
            if (cachedCandidates == null) // everything was cached
                candidatesWithNoCache = new QueryResultImpl(query);
            else
            {
                if (candidates != null)
                {
                    candidatesWithNoCache = candidates.copy();
                    candidatesWithNoCache.removeAll(cachedCandidates);
                }
                else // user wants to retrieve over all candidates, we have only cached over a subset of that
                    candidatesWithNoCache = cachedCandidates.invert();
            }
            return new Pair<>(_cachedResults.get(query).second.copy(), candidatesWithNoCache);
        }
        else if (candidates == null)
            return new Pair<>(new QueryResultImpl(query), new QueryResultImpl(query).invert());
        else
            return new Pair<>(new QueryResultImpl(query), candidates.copy());
    }

    /**
     * Invalidates the cache (empties it). To be called after every time step (since the cache does not store
     * information on the temporal validity of the cached entries!).
     */
    public void invalidate()
    {
        _cachedResults = new HashMap<>();
    }
}
