package openllet.mtcq.engine;

import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.utils.Bool;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.engine.bcq.AbstractBCQQueryEngine;
import openllet.query.sparqldl.engine.bcq.BCQQueryEngineSimple;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.bcq.BCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.results.MultiQueryResults;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.query.sparqldl.model.results.ResultBinding;
import openllet.shared.tools.Log;
import openllet.mtcq.model.automaton.DFA;
import openllet.mtcq.model.automaton.Edge;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Manages the satisfiability knowledge for all BCQs and time points.
 * Due to its global scope, it can propagate satisfiability information around, e.g. connecting satisfiability of a
 * query with the unsatisfiability of its negation.
 * It can also propagate knowledge around based on similarities of ABoxes to certain time points.
 */
@Deprecated
public class SatisfiabilityKnowledgeManager
{
    public static final Logger _logger = Log.getLogger(SatisfiabilityKnowledgeManager.class);

    public enum SubQuerySelectionStrategy
    {
        LIGHTWEIGHT, AMAP
    }

    /**
     * This class is only used for statistics for benchmarking.
     */
    protected static class SatisfiabilityStats
    {
        private final Map<Integer, Map<BCQQuery, Double>> _percentagesOfIncludedBindings = new HashMap<>();

        protected void informAboutBindingInclusion(int timePoint, BCQQuery query, double ratioOfIncludedBindings)
        {
            if (!_percentagesOfIncludedBindings.containsKey(timePoint))
                _percentagesOfIncludedBindings.put(timePoint, new HashMap<>());
            Map<BCQQuery, Double> map = _percentagesOfIncludedBindings.get(timePoint);
            if (!map.containsKey(query))
                map.put(query, 100 * ratioOfIncludedBindings);
            else
                _logger.warning("Did not overwrite prior result with " + ratioOfIncludedBindings +
                        " when collecting statistics.");
        }

        @Override
        public String toString()
        {
            StringBuilder res = new StringBuilder("Already gathered satisfiability knowledge from CQ engine:\n");
            // avg. over BCQ sat. for each time point
            List<Double> avgs = new ArrayList<>();
            for (Integer t : _percentagesOfIncludedBindings.keySet())
            {
                Map<BCQQuery, Double> map = _percentagesOfIncludedBindings.get(t);
                double avgSat = map.values().stream().mapToDouble(val -> val).average().orElse(0.0);
                avgs.add(avgSat);
                res.append(String.format("t=" + t + ": %.6f\n", avgSat));
            }
            double avgSat = avgs.stream().mapToDouble(val -> val).average().orElse(0.0);
            res.append(String.format("total: %.6f", avgSat));
            return res.toString();
        }
    }

    private SubQuerySelectionStrategy _strategy = SubQuerySelectionStrategy.LIGHTWEIGHT;
    private final MetricTemporalConjunctiveQuery _mtcq;
    private final List<SatisfiabilityKnowledge> _knowledges = new ArrayList<>();
    private final AbstractBCQQueryEngine _bcqEngine = new BCQQueryEngineSimple();
    private final QueryExec<ConjunctiveQuery> _cqEngine = new QueryEngine();
    private final Map<Integer, List<ConjunctiveQuery>> _cqCache = new HashMap<>();
    private QueryResult _globallyIncludeBindings;
    private final SatisfiabilityStats _stats = new SatisfiabilityStats();

    /**
     * Constructs a new satisfiability knowledge manager for the given query and DFA.
     * @param query The query to manage satisfiability information for.
     * @param dfa The DFA corresponding to the query.
     */
    public SatisfiabilityKnowledgeManager(MetricTemporalConjunctiveQuery query, DFA dfa)
    {
        _mtcq = query;
        _globallyIncludeBindings = new QueryResultImpl(query).invert();
        for (int state: dfa.getStates())
            for (Edge edge : dfa.getEdges(state))
                for (BCQQuery bcq : edge.getBCQs())
                {
                    SatisfiabilityKnowledge newKnowledge = new SatisfiabilityKnowledge(bcq, _mtcq);
                    if (bcq.isEmpty())
                    {
                        QueryResult allBindings = new QueryResultImpl(bcq).invert();
                        newKnowledge.informAboutSatisfiability(allBindings, true,
                                IntStream.rangeClosed(0, query.getTemporalKB().size()).boxed().toList());
                        for (int i = 0; i < query.getTemporalKB().size(); i++)
                            newKnowledge.isComplete(i);
                    }
                    _knowledges.add(newKnowledge);
                }
    }

    public SatisfiabilityKnowledgeManager(MetricTemporalConjunctiveQuery query, DFA dfa, SubQuerySelectionStrategy strategy)
    {
        this(query, dfa);
        _strategy = strategy;
    }

    /**
     * Informs the manager to not generate satisfiability results for the given excluded bindings.
     * @param excludedBindings The bindings to exclude. Can be null.
     */
    public void setGloballyExcludedBindings(QueryResult excludedBindings)
    {
        if (excludedBindings != null)
        {
            _globallyIncludeBindings = excludedBindings.invert();
            _globallyIncludeBindings.iterator();
        }
    }

    /**
     * Fetches the knowledge the manager currently has for the given BCQ at the time point. For this, it possibly
     * transfers knowledge from the previous time point to the given time point, if
     * `OpenlletOptions.MTCQ_ENGINE_TEMPORAL_BCQ_TRANSFER` is true.
     * @param query The BCQ to get the knowledge for.
     * @param timePoint Time point to get the knowledge for.
     * @return A pointer to the satisfiability knowledge for the given BCQ and time point, null if there is no
     *  knowledge.
     */
    public @Nullable SatisfiabilityKnowledge transferAndGetKnowledgeOnQuery(BCQQuery query, int timePoint)
    {
        SatisfiabilityKnowledge knowledge = getKnowledgeOnQuery(query);
        if (OpenlletOptions.MTCQ_ENGINE_TEMPORAL_BCQ_TRANSFER && knowledge != null)
            knowledge.transferKnowledgeFromPreviousStepTo(timePoint);
        return knowledge;
    }

    /**
     * @param query The BCQ to get the knowledge for.
     * @return A pointer to the satisfiability knowledge the manager currently has on the given BCQ.
     *  Can be null if there is none.
     */
    public @Nullable SatisfiabilityKnowledge getKnowledgeOnQuery(BCQQuery query)
    {
        for (SatisfiabilityKnowledge knowledge : _knowledges)
            if (knowledge.getQuery() == query)
                return knowledge;
        return null;
    }

    /**
     * Given a BCQ, this function extracts the CQs that are checked by the manager if in underapproximating mode, based
     * on the SubQuerySelectionStrategy. If Lightweight, it extracts all negative sub-CQs and the CQ representing the
     * conjunction of all positive sub-CQs. If AMAP, all positive sub-CQs are included as well.
     * @param query The BCQ to extract CQs from.
     * @return A new collection of CQs extracted from the BCQ.
     */
    private Collection<ConjunctiveQuery> getCandidatesForCheckingUnderapproximatingSemantics(BCQQuery query)
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

    /**
     * Propagates the entailed result for a CQ to the BCQs that are managed by this manager. Thus, it modifies the
     * entries of the _knowledges list to contain new information on (un)satisfiability.
     * @param query The CQ for which the entailed result was gained.
     * @param entailedResult The certain answers to the given query.
     * @param timePoint The time point for which the result was computed.
     */
    private void propagateKnowledge(ConjunctiveQuery query, QueryResult entailedResult, int timePoint)
    {
        // Implements "transferability" from CQ entailment to BCQ satisfiability.
        for (SatisfiabilityKnowledge knowledge : _knowledges)
        {
            if (knowledge.getQuery().getNegativeQueries().isEmpty())
            {
                if ((knowledge.getQuery().getPositiveQueries().size() == 1 &&
                        knowledge.getQuery().getPositiveQueries().contains(query)) ||
                        knowledge.getQuery().mergePositiveQueries().equals(query))
                    knowledge.informAboutSatisfiability(entailedResult.copy(), true, timePoint);
            }
            else
            {
                query.setNegation(true); // Temporarily sets negation to true to allow to find it in negative queries.
                if (knowledge.getQuery().getNegativeQueries().contains(query))
                    knowledge.informAboutSatisfiability(entailedResult.copy(), false, timePoint);
                if ((knowledge.getQuery().getNegativeQueries().size() == 1 &&
                        knowledge.getQuery().getPositiveQueries().isEmpty() &&
                        knowledge.getQuery().getNegativeQueries().contains(query)))
                    knowledge.informAboutSatisfiability(entailedResult.invert(), true, timePoint);
                query.setNegation(false);
            }
        }
    }

    /**
     * A wrapper around Openllet's conjunctive query engine. Executes the query for the knowledge base at the given time
     * point and propagates the gained knowledge within this knowledge manager.
     * @param query The CQ to answer.
     * @param timePoint The time point for which the query is executed.
     */
    private void execConjunctiveQueryEngine(ConjunctiveQuery query, int timePoint)
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
            if (_mtcq.isDistinct())
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

    /**
     * Helper function to transform a MultiQueryResults (possibly returned by the CQ engine) to a standard
     * QueryResultImpl, as we can only perform relevant computations on QueryResultImpl. In this case, it creates a
     * completely new query result.
     * Note that this explicates the cross-product. Outputs the input result if it is already a QueryResultImpl.
     * @param result The query result to convert.
     * @param origQuery The query of the query result to convert.
     * @return A QueryResult (that is guaranteed to be a QueryResultImpl).
     */
    private QueryResult toQueryResult(QueryResult result, Query<?> origQuery)
    {
        QueryResult usableResult;
        if (result instanceof MultiQueryResults mqr)
            usableResult = mqr.toQueryResultImpl(origQuery);
        else
            usableResult = result;
        return usableResult;
    }

    /**
     * A wrapper around the BCQ engine. Executes the query for the knowledge base at the given time point and
     * propagates the gained knowledge within this knowledge manager.
     * @param query The BCQ to answer.
     * @param timePoint The time point for which the query is executed.
     * @param knowledgeOnQuery Already gathered knowledge on the BCQ from a possible previous run with the CQ engine.
     * @param restrictSatToBindings If not null, satisfiability results are restricted to this set of bindings
     */
    private void execBCQQueryEngine(BCQQuery query, int timePoint, SatisfiabilityKnowledge knowledgeOnQuery,
                                     QueryResult restrictSatToBindings)
    {
        QueryResult restrictTo;
        QueryResult certainSatKnowledge = knowledgeOnQuery.getCertainSatisfiabilityKnowledge(timePoint).get(Bool.TRUE);
        QueryResult certainUnsatKnowledge = knowledgeOnQuery.getCertainSatisfiabilityKnowledge(timePoint).get(Bool.FALSE);
        int certainKnowledgeSize = certainSatKnowledge.size() + certainUnsatKnowledge.size();
        int restrictToSize = restrictSatToBindings != null ? restrictSatToBindings.size() : 0;
        double restrictToRatio = (double) restrictToSize / certainSatKnowledge.getMaxSize();
        double certainKnowledgeRatio = (double) certainKnowledgeSize / certainSatKnowledge.getMaxSize();
        // copy() is expensive - we need to avoid it if possible. Also, retain/removeAll is expensive - we only do so
        //  if we gain substantial exclusion possibility from it (threshold: 5%).
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
        double maxSize = restrictTo.getMaxSize();
        double restrictToStatsSize = 0;
        if (maxSize > 0)
            restrictToStatsSize = (double) restrictTo.size() / restrictTo.getMaxSize();
        _stats.informAboutBindingInclusion(timePoint, query, restrictToStatsSize);
        if (!restrictTo.isEmpty())
        {
            QueryResult result = _bcqEngine.exec(query, null, restrictTo);
            if (!restrictedSatBindingsInRestrictTo)
                result.retainAll(restrictSatToBindings);
            knowledgeOnQuery.informAboutSatisfiability(result, true, timePoint);
        }
        // Sets knowledge complete s.t. satisfying bindings can be inverted to get unsatisfiable bindings.
        knowledgeOnQuery.setComplete(timePoint);
    }

    /**
     * This is the main entry point to the satisfiability knowledge manager.
     * Computes and returns the satisfiable answers of the given query w.r.t. the givne knowledge base.
     * @param query The BCQ to compute satisfiability for.
     * @param timePoint The time point from which the given knowledge base is.
     * @param kb The knowledge base to compute satisfiable answers for.
     * @param useUnderapproximatingSemantics If true, uses only the CQ engine to compute answers.
     * @param restrictSatToBindings Restricts satisfiable answers to the given set of bindings, if not null.
     * @return A pointer to the current query result for satisfiable and unsatisfiable knowledge, as a mapping from
     * true (satisfiable) and false (unsatisfiable) to a query result.
     */
    public Map<Bool, QueryResult> computeSatisfiableBindings(BCQQuery query, int timePoint, KnowledgeBase kb,
                                                             boolean useUnderapproximatingSemantics,
                                                             QueryResult restrictSatToBindings)
    {
        query.setKB(kb);
        SatisfiabilityKnowledge knowledgeOnQuery = transferAndGetKnowledgeOnQuery(query, timePoint);
        if (knowledgeOnQuery != null)
        {
            // Case 1: We use the CQ engine only and have not checked the BCQ previously.
            if (useUnderapproximatingSemantics && knowledgeOnQuery.isEmpty(timePoint))
            {
                _logger.finer("Calling CQ engine for " + query);
                for (ConjunctiveQuery subQuery : getCandidatesForCheckingUnderapproximatingSemantics(query))
                    execConjunctiveQueryEngine(subQuery, timePoint);
            }
            // Case 2: We use full semantics and have not yet complete knowledge on the BCQ.
            else if (!useUnderapproximatingSemantics && !knowledgeOnQuery.isComplete(timePoint))
            {
                _logger.finer("Calling BCQ engine for " + query);
                execBCQQueryEngine(query, timePoint, knowledgeOnQuery, restrictSatToBindings);
            }
            // Case 3: We use full semantics but have complete knowledge (-> only updates statistics).
            else if (!useUnderapproximatingSemantics)
                _stats.informAboutBindingInclusion(timePoint, query, 0);
            _logger.finer("Retrieving satisfiability knowledge on " + query);
            return knowledgeOnQuery.getCertainSatisfiabilityKnowledge(timePoint, restrictSatToBindings);
        }
        else
            throw new RuntimeException("Knowledge on query " + query + " has not been initialized");
    }

    /**
     * @return Statistics on the manager's performance, for evaluation.
     */
    public String getStats()
    {
        return _stats.toString();
    }
}
