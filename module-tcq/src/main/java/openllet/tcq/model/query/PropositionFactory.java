package openllet.tcq.model.query;

import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.tcq.parser.ConjunctiveQueryParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PropositionFactory
{
    private Map<ConjunctiveQuery, Proposition> _alreadyCreated = new HashMap<>();
    int curPropLength = 1;
    int curChar = 0;
    final char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();

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
}
