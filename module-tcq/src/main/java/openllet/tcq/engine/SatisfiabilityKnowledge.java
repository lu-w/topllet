package openllet.tcq.engine;

import openllet.core.utils.Bool;
import openllet.query.sparqldl.engine.QueryCandidateGeneratorNaive;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import java.util.*;

/**
 * Stores satisfiability information of an arbitrary BCQ wrt. different time points.
 */
public class SatisfiabilityKnowledge
{
    private final CNCQQuery _cncq;
    private final TemporalConjunctiveQuery _tcq;
    private final Map<Integer, QueryResult> _satisfiableBindings = new HashMap<>();
    private final Map<Integer, QueryResult> _unsatisfiableBindings = new HashMap<>();
    private final List<Integer> _isComplete = new ArrayList<>();

    public SatisfiabilityKnowledge(CNCQQuery query, TemporalConjunctiveQuery tcq)
    {
        _cncq = query;
        _tcq = tcq;
    }

    public CNCQQuery getQuery()
    {
        return _cncq;
    }

    public void informAboutSatisfiability(QueryResult bindings, boolean satisfiability, int applicableTimePoint)
    {
        informAboutSatisfiability(bindings, satisfiability, Set.of(applicableTimePoint));
    }

    public void informAboutSatisfiability(QueryResult bindings, boolean satisfiability,
                                          Collection<Integer> applicableTimePoints)
    {
        // The information may have been gained from an impartial query check (e.g. A(?x) when the overall TCQ
        // works over ?x and ?y. Therefore, we expand the query result to all variables of the TCQ.
        if (!bindings.getResultVars().equals(_tcq.getResultVars()))
            bindings.expandToAllVariables(_tcq.getResultVars());
        bindings.explicate();

        Map<Integer, QueryResult> applicableBindings;

        if (satisfiability)
            applicableBindings = _satisfiableBindings;
        else
            applicableBindings = _unsatisfiableBindings;

        for (int applicableTimePoint : applicableTimePoints)
            if (applicableBindings.containsKey(applicableTimePoint))
                applicableBindings.get(applicableTimePoint).addAll(bindings);
            else
            {
                applicableBindings.put(applicableTimePoint, bindings);
            }
    }

    public Map<Bool, QueryResult> getCertainSatisfiabilityKnowledge(int timePoint)
    {
        return getCertainSatisfiabilityKnowledge(timePoint, null);
    }

    public Map<Bool, QueryResult> getCertainSatisfiabilityKnowledge(int timePoint, QueryResult restrictSatToBindings)
    {
        Map<Bool, QueryResult> knowledge = new HashMap<>();
        if (_satisfiableBindings.containsKey(timePoint))
            knowledge.put(Bool.TRUE, filterBindings(_satisfiableBindings.get(timePoint), restrictSatToBindings));
        else
            knowledge.put(Bool.TRUE, new QueryResultImpl(_cncq));
        if (_unsatisfiableBindings.containsKey(timePoint))
            knowledge.put(Bool.FALSE, _unsatisfiableBindings.get(timePoint));
        else
            knowledge.put(Bool.FALSE, new QueryResultImpl(_cncq));
        return knowledge;
    }

    /**
     * @param bindings The bindings to filter
     * @param restrictToBindings The bindings to restrict bindings to
     * @return A new copy of bindings in which only bindings are present that are also present in restrictToBindings
     */
    private QueryResult filterBindings(QueryResult bindings, QueryResult restrictToBindings)
    {
        QueryResult filtered;
        if (restrictToBindings != null)
        {
            filtered = bindings.copy();
            filtered.retainAll(restrictToBindings);
        }
        else
            filtered = bindings;
        return filtered;
    }

    @Deprecated
    public Map<Bool, QueryResult> getSatisfiabilityKnowledge(int timePoint)
    {
        Map<Bool, QueryResult> knowledge = getCertainSatisfiabilityKnowledge(timePoint);
        knowledge.put(Bool.UNKNOWN, new QueryResultImpl(_cncq));
        for (ResultBinding binding : new QueryCandidateGeneratorNaive(_cncq))
        {
            if ((!_satisfiableBindings.containsKey(timePoint) ||
                    !_satisfiableBindings.get(timePoint).contains(binding)) &&
                (!_unsatisfiableBindings.containsKey(timePoint) ||
                    !_unsatisfiableBindings.get(timePoint).contains(binding)))
                knowledge.get(Bool.UNKNOWN).add(binding);
        }
        return knowledge;
    }

    public boolean isComplete(int timePoint)
    {
        return _isComplete.contains(timePoint) ||
                (getCertainSatisfiabilityKnowledge(timePoint).get(Bool.FALSE).size() +
                getCertainSatisfiabilityKnowledge(timePoint).get(Bool.TRUE).size() ==
                new QueryResultImpl(_cncq).getMaxSize());
    }

    public void setComplete(int timePoint)
    {
        _isComplete.add(timePoint);
    }
}
