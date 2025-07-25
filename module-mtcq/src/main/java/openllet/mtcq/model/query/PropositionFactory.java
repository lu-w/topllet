package openllet.mtcq.model.query;

import openllet.query.sparqldl.model.cq.ConjunctiveQuery;

import java.util.HashMap;
import java.util.Map;

/**
 * Global factory for creating propositions to re-use propositions for already encountered CQs.
 */
public class PropositionFactory
{
    private final Map<ConjunctiveQuery, Proposition> _alreadyCreated = new HashMap<>();
    int curPropLength = 1;
    int curChar = 0;
    final char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * Creas a proposition for the given query. In case there was already a proposition constructed for the same query,
     * it returns this proposition.
     * @param query The query to construct the proposition for.
     * @return A proposition for the query.
     */
    public Proposition create(ConjunctiveQuery query)
    {
        if (_alreadyCreated.containsKey(query))
            return _alreadyCreated.get(query);
        else
        {
            String propString = String.valueOf(chars[curChar]).repeat(Math.max(0, curPropLength));
            if (curChar < chars.length - 1)
                curChar++;
            else {
                curPropLength += 1;
                curChar = 0;
            }
            Proposition prop = new PropositionImpl(propString, _alreadyCreated.size());
            _alreadyCreated.put(query, prop);
            return prop;
        }
    }

    public Map<Proposition, ConjunctiveQuery> getCreatedPropositions()
    {
        Map<Proposition, ConjunctiveQuery> created = new HashMap<>();
        for (ConjunctiveQuery cq : _alreadyCreated.keySet())
            created.put(_alreadyCreated.get(cq), cq);
        return created;
    }
}
