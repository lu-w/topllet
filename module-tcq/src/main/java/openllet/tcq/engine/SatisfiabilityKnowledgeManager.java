package openllet.tcq.engine;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Bool;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.engine.cncq.AbstractCNCQQueryEngine;
import openllet.query.sparqldl.engine.cncq.CNCQQueryEngineSimple;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.results.MultiQueryResults;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.tcq.model.automaton.DFA;
import openllet.tcq.model.automaton.Edge;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Manages the satisfiability knowledge for all queries and time points.
 * Due to its global scope, it can propagate satisfiability information around, e.g. connecting satisfiability of a
 * query with the unsatisfiability of its negation.
 * It can also propagate knowledge around based on similarities of ABoxes to certain time points.
 */
public class SatisfiabilityKnowledgeManager
{
    public enum SubQuerySelectionStrategy
    {
        LIGHTWEIGHT, AMAP
    }

    private SubQuerySelectionStrategy _strategy = SubQuerySelectionStrategy.AMAP;
    private final TemporalConjunctiveQuery _tcq;
    private final List<SatisfiabilityKnowledge> _knowledges = new ArrayList<>();
    private final AbstractCNCQQueryEngine _cncqEngine = new CNCQQueryEngineSimple();
    private final QueryExec<ConjunctiveQuery> _cqEngine = new QueryEngine();
    private final Map<Integer, List<ConjunctiveQuery>> _cqCache = new HashMap<>();
    private QueryResult _globallyExcludeBindings;

    public SatisfiabilityKnowledgeManager(TemporalConjunctiveQuery query, DFA dfa)
    {
        _tcq = query;
        _globallyExcludeBindings = new QueryResultImpl(query);
        for (int state: dfa.getStates())
            for (Edge edge : dfa.getEdges(state))
                for (CNCQQuery cncq : edge.getCNCQs())
                {
                    SatisfiabilityKnowledge newKnowledge = new SatisfiabilityKnowledge(cncq, _tcq);
                    if (cncq.isEmpty())
                    {
                        QueryResult allBindings = new QueryResultImpl(cncq).invert();
                        newKnowledge.informAboutSatisfiability(allBindings, true,
                                IntStream.rangeClosed(0, query.getTemporalKB().size()).boxed().toList());
                        for (int i = 0; i < query.getTemporalKB().size(); i++)
                            newKnowledge.isComplete(i);
                    }
                    _knowledges.add(newKnowledge);
                }
    }

    public SatisfiabilityKnowledgeManager(TemporalConjunctiveQuery query, DFA dfa, SubQuerySelectionStrategy strategy)
    {
        this(query, dfa);
        _strategy = strategy;
    }

    public void setGloballyExcludedBindings(QueryResult excludedBindings)
    {
        if (excludedBindings != null)
            _globallyExcludeBindings = excludedBindings;
    }

    public void doNotGloballyExcludeBindings()
    {
        _globallyExcludeBindings = new QueryResultImpl(_tcq);
    }

    public SatisfiabilityKnowledge getKnowledgeOnQuery(CNCQQuery query)
    {
        for (SatisfiabilityKnowledge knowledge : _knowledges)
            if (knowledge.getQuery() == query)
                return knowledge;
        return null;
    }

    private Collection<ConjunctiveQuery> getCandidatesForCheckingUnderapproximatingSemantics(CNCQQuery query)
    {
        // TODO do not add duplicate queries here? can this even happen?
        List<ConjunctiveQuery> candidates = new ArrayList<>();
        if (!query.getPositiveQueries().isEmpty())
        {
            candidates.add(query.mergePositiveQueries());
            // TODO maybe add all positive subqueries of query as well
            if (_strategy.compareTo(SubQuerySelectionStrategy.AMAP) >= 0 && query.getPositiveQueries().size() > 1)
                for (ConjunctiveQuery pQuery : query.getPositiveQueries())
                    candidates.add(pQuery.copy());
        }
        if (!query.getNegativeQueries().isEmpty())
            for (ConjunctiveQuery nQuery : query.getNegativeQueries())
            {
                ConjunctiveQuery pQuery = nQuery.copy();
                pQuery.setNegation(false);
                candidates.add(pQuery);
            }
        for (ConjunctiveQuery candidate : candidates)
            candidate.setKB(query.getKB());
        return candidates;
    }

    private void propagateKnowledge(ConjunctiveQuery query, QueryResult entailedResult, int timePoint)
    {
        // implements "transferability" from CQ entailment to CNCQ satisfiability
        for (SatisfiabilityKnowledge knowledge : _knowledges)
        {
            if (knowledge.getQuery().getNegativeQueries().size() == 0)
            {
                if ((knowledge.getQuery().getPositiveQueries().size() == 1 &&
                        knowledge.getQuery().getPositiveQueries().contains(query)) ||
                        knowledge.getQuery().mergePositiveQueries().equals(query))
                    knowledge.informAboutSatisfiability(entailedResult.copy(), true, timePoint);
            }
            else
            {
                query.setNegation(true); // temporarily setting negation to true to allow to find it in negative queries
                if (knowledge.getQuery().getNegativeQueries().contains(query))
                    knowledge.informAboutSatisfiability(entailedResult.copy(), false, timePoint);
                if ((knowledge.getQuery().getNegativeQueries().size() == 1 &&
                        knowledge.getQuery().getPositiveQueries().size() == 0 &&
                        knowledge.getQuery().getNegativeQueries().contains(query)))
                    knowledge.informAboutSatisfiability(entailedResult.invert(), true, timePoint);
                query.setNegation(false);
            }
        }
        // TODO propagate knowledge to all time points in which it also holds
    }

    private void execConjunctiveQueryEngine(ConjunctiveQuery query, int timePoint)
            throws IOException, InterruptedException
    {
        if (!_cqCache.containsKey(timePoint) || !_cqCache.get(timePoint).contains(query))
        {
            // query.copy() is required because for some queries and KBs, Openllet's CQ engine alters the query.
            //  Example: "r(x, y)" for undist. variables x and y results in "r(x, y) ^ SubPropertyOf(r,r)" after exec().
            QueryResult result = toQueryResult(_cqEngine.exec(query.copy()), query);
            if (!_cqCache.containsKey(timePoint))
                _cqCache.put(timePoint, new ArrayList<>());
            _cqCache.get(timePoint).add(query);
            propagateKnowledge(query, result, timePoint);
        }
    }

    private QueryResult toQueryResult(QueryResult result, Query<?> origQuery)
    {
        QueryResult usableResult;
        if (result instanceof MultiQueryResults mqr)
            usableResult = mqr.toQueryResultImpl(origQuery);
        else
            usableResult = result;
        return usableResult;
    }

    private void execCNCQQueryEngine(CNCQQuery query, int timePoint, SatisfiabilityKnowledge knowledgeOnQuery,
                                     QueryResult restrictSatToBindings) throws IOException, InterruptedException
    {
        for (QueryResult results : knowledgeOnQuery.getCertainSatisfiabilityKnowledge(timePoint).values())
            _globallyExcludeBindings.addAll(results);
        QueryResult result = _cncqEngine.exec(query, _globallyExcludeBindings, restrictSatToBindings);
        knowledgeOnQuery.informAboutSatisfiability(result, true, timePoint);
        knowledgeOnQuery.setComplete(timePoint);  // knowledge complete -> satisfying bindings can be inverted to get unsat bindings
        for (QueryResult results : knowledgeOnQuery.getCertainSatisfiabilityKnowledge(timePoint).values())
            _globallyExcludeBindings.removeAll(results);
    }

    public Map<Bool, QueryResult> computeSatisfiableBindings(CNCQQuery query, int timePoint, KnowledgeBase kb,
                                                             boolean useUnderapproximatingSemantics,
                                                             QueryResult restrictSatToBindings)
            throws IOException, InterruptedException
    {
        query.setKB(kb);
        SatisfiabilityKnowledge knowledgeOnQuery = getKnowledgeOnQuery(query);
        if (knowledgeOnQuery != null)
        {
            if (!knowledgeOnQuery.isComplete(timePoint))
                if (useUnderapproximatingSemantics)
                {
                    for (ConjunctiveQuery subQuery : getCandidatesForCheckingUnderapproximatingSemantics(query))
                        execConjunctiveQueryEngine(subQuery, timePoint);
                }
                else
                {
                    execCNCQQueryEngine(query, timePoint, knowledgeOnQuery, restrictSatToBindings);
                }
            return knowledgeOnQuery.getCertainSatisfiabilityKnowledge(timePoint, restrictSatToBindings);
        }
        else
            throw new RuntimeException("Knowledge on query " + query + " has not been initialized");
    }
}
