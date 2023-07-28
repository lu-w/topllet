package openllet.tcq.engine;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.Bool;
import openllet.query.sparqldl.engine.QueryBindingCandidateGenerator;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.engine.QueryResultBasedBindingCandidateGenerator;
import openllet.query.sparqldl.engine.cncq.AbstractCNCQQueryEngine;
import openllet.query.sparqldl.engine.cncq.CNCQQueryEngineSimple;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.results.MultiQueryResults;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.shared.tools.Log;
import openllet.tcq.model.automaton.DFA;
import openllet.tcq.model.automaton.Edge;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Manages the satisfiability knowledge for all queries and time points.
 * Due to its global scope, it can propagate satisfiability information around, e.g. connecting satisfiability of a
 * query with the unsatisfiability of its negation.
 * It can also propagate knowledge around based on similarities of ABoxes to certain time points.
 */
public class SatisfiabilityKnowledgeManager
{
    public static final Logger _logger = Log.getLogger(SatisfiabilityKnowledgeManager.class);

    public enum SubQuerySelectionStrategy
    {
        LIGHTWEIGHT, AMAP
    }

    protected static class SatisfiabilityStats
    {
        private final Map<Integer, Map<CNCQQuery, Double>> _ratioOfExcludedBindings = new HashMap<>();

        protected void informAboutBindingExclusion(int timePoint, CNCQQuery query, double ratioOfExcludedBindings)
        {
            if (!_ratioOfExcludedBindings.containsKey(timePoint))
                _ratioOfExcludedBindings.put(timePoint, new HashMap<>());
            Map<CNCQQuery, Double> map = _ratioOfExcludedBindings.get(timePoint);
            if (!map.containsKey(query))
                map.put(query, ratioOfExcludedBindings);
            else
                _logger.warning("Did not overwrite prior result with " + ratioOfExcludedBindings +
                        " when collecting statistics.");
        }

        @Override
        public String toString()
        {
            StringBuilder res = new StringBuilder("Already gathered satisfiability knowledge from CQ engine:\n");
            // avg. over CNCQ sat. for each time point
            List<Double> avgs = new ArrayList<>();
            for (Integer t : _ratioOfExcludedBindings.keySet())
            {
                Map<CNCQQuery, Double> map = _ratioOfExcludedBindings.get(t);
                double avgSat = map.values().stream().mapToDouble(val -> val).average().orElse(0.0);
                avgs.add(avgSat);
                res.append(String.format("t=" + t + ": %.2f\n", 100 * avgSat));
            }
            double avgSat = avgs.stream().mapToDouble(val -> val).average().orElse(0.0);
            res.append(String.format("total: %.2f", 100 * avgSat));
            return res.toString();
        }
    }

    private SubQuerySelectionStrategy _strategy = SubQuerySelectionStrategy.AMAP;
    private final TemporalConjunctiveQuery _tcq;
    private final List<SatisfiabilityKnowledge> _knowledges = new ArrayList<>();
    private final AbstractCNCQQueryEngine _cncqEngine = new CNCQQueryEngineSimple();
    private final QueryExec<ConjunctiveQuery> _cqEngine = new QueryEngine();
    private final Map<Integer, List<ConjunctiveQuery>> _cqCache = new HashMap<>();
    private QueryResult _globallyIncludeBindings;
    private final SatisfiabilityStats _stats = new SatisfiabilityStats();

    public SatisfiabilityKnowledgeManager(TemporalConjunctiveQuery query, DFA dfa)
    {
        _tcq = query;
        _globallyIncludeBindings = new QueryResultImpl(query).invert();
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
        {
            _globallyIncludeBindings = excludedBindings.invert();
            _globallyIncludeBindings.iterator();
        }
    }

    public SatisfiabilityKnowledge transferAndGetKnowledgeOnQuery(CNCQQuery query, int timePoint)
    {
        SatisfiabilityKnowledge knowledge = getKnowledgeOnQuery(query);
        if (knowledge != null)
            knowledge.transferKnowledgeFromPreviousStepTo(timePoint);
        return knowledge;
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
        Set<ConjunctiveQuery> candidates = new HashSet<>();
        if (!query.getPositiveQueries().isEmpty())
        {
            candidates.add(query.mergePositiveQueries());
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
        // TODO PLANNED OPTIMIZATION: propagate knowledge to all time points in which it also holds
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
            // Removes all non-distinct bindings (e.g., x->a, y->a), if the CQ engine returned some
            if (_tcq.isDistinct())
            {
                List<ResultBinding> toRemove = new ArrayList<>();
                for (ResultBinding b : result)
                    if (!b.isDistinct())
                        toRemove.add(b);
                for (ResultBinding b : toRemove)
                    result.remove(b);
            }
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
        QueryResult restrictTo;
        QueryResult certainSatKnowledge = knowledgeOnQuery.getCertainSatisfiabilityKnowledge(timePoint).get(Bool.TRUE);
        QueryResult certainUnsatKnowledge = knowledgeOnQuery.getCertainSatisfiabilityKnowledge(timePoint).get(Bool.FALSE);
        int certainKnowledgeSize = certainSatKnowledge.size() + certainUnsatKnowledge.size();
        int restrictToSize = restrictSatToBindings != null ? restrictSatToBindings.size() : 0;
        double restrictToRatio = (double) restrictToSize / certainSatKnowledge.getMaxSize();
        double certainKnowledgeRatio = (double) certainKnowledgeSize / certainSatKnowledge.getMaxSize();
        // copy() is expensive - we need to avoid it if possible
        boolean restrictedSatBindingsInRestrictTo = false;
        double minRatio = 0.05;
        if (restrictToRatio > minRatio || certainKnowledgeRatio > minRatio)
        {
            restrictTo = _globallyIncludeBindings.copy();
            if (restrictToRatio > minRatio)
            {
                restrictedSatBindingsInRestrictTo = true;
                restrictTo.retainAll(restrictSatToBindings);
            }
            // removeAll is expensive - we only do so if we gain substantial knowledge (i.e., over 5% more).
            if (certainKnowledgeRatio > minRatio)
            {
                restrictTo.removeAll(certainSatKnowledge);
                restrictTo.removeAll(certainUnsatKnowledge);
            }
        }
        else
            restrictTo = _globallyIncludeBindings;
        // TODO fix stats for evaluation
        /*double maxSize = excludeForQuery.getMaxSize();
        double excludedBindingsSize = 1;
        if (maxSize > 0)
            excludedBindingsSize = (double) excludeForQuery.size() / excludeForQuery.getMaxSize();
        _stats.informAboutBindingExclusion(timePoint, query, excludedBindingsSize);*/
        if (!restrictTo.isEmpty())
        {
            QueryResult result = _cncqEngine.exec(query, null, restrictTo);
            if (!restrictedSatBindingsInRestrictTo)
                result.retainAll(restrictSatToBindings);
            knowledgeOnQuery.informAboutSatisfiability(result, true, timePoint);
        }
        // Sets knowledge complete s.t. satisfying bindings can be inverted to get unsatisfiable bindings.
        knowledgeOnQuery.setComplete(timePoint);
    }

    public Map<Bool, QueryResult> computeSatisfiableBindings(CNCQQuery query, int timePoint, KnowledgeBase kb,
                                                             boolean useUnderapproximatingSemantics,
                                                             QueryResult restrictSatToBindings)
            throws IOException, InterruptedException
    {
        query.setKB(kb);
        SatisfiabilityKnowledge knowledgeOnQuery = transferAndGetKnowledgeOnQuery(query, timePoint);
        if (knowledgeOnQuery != null)
        {
            if (useUnderapproximatingSemantics && knowledgeOnQuery.isEmpty(timePoint))
            {
                _logger.finer("Calling CQ engine for " + query);
                for (ConjunctiveQuery subQuery : getCandidatesForCheckingUnderapproximatingSemantics(query))
                    execConjunctiveQueryEngine(subQuery, timePoint);
            }
            else if (!useUnderapproximatingSemantics && !knowledgeOnQuery.isComplete(timePoint))
            {
                _logger.finer("Calling CNCQ engine for " + query);
                execCNCQQueryEngine(query, timePoint, knowledgeOnQuery, restrictSatToBindings);
            }
            else if (!useUnderapproximatingSemantics)
                _stats.informAboutBindingExclusion(timePoint, query, 1);
            _logger.finer("Retrieving satisfiability knowledge on " + query);
            return knowledgeOnQuery.getCertainSatisfiabilityKnowledge(timePoint, restrictSatToBindings);
        }
        else
            throw new RuntimeException("Knowledge on query " + query + " has not been initialized");
    }

    public String getStats()
    {
        return _stats.toString();
    }
}
