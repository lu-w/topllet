package openllet.mtcq.engine.engine_rewriting;

import openllet.aterm.ATermAppl;
import openllet.core.utils.Pair;
import openllet.mtcq.engine.atemporal.BDQEngine;
import openllet.mtcq.engine.rewriting.CXNFTransformer;
import openllet.mtcq.engine.rewriting.CXNFVerifier;
import openllet.mtcq.model.query.*;
import openllet.mtcq.ui.MTCQEngineUI;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.engine.QueryCache;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.model.results.MultiQueryResults;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.shared.tools.Log;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Logger;

import static openllet.mtcq.engine.rewriting.MTCQSimplifier.*;

/**
 * Query engine for answering non-Boolean metric temporal conjunctive queries via rewriting into a normal form.
 * Has a UI and streaming functionality. Streaming can be used via 0MQ and a configurable port. Any UI implementing the
 * {@code MTCQEngineUI} can be used to print results from this engine (also during computation) to the user.
 */
public class MTCQNormalFormEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    public static final Logger _logger = Log.getLogger(MTCQNormalFormEngine.class);

    private final BDQEngine _bdqEngine = new BDQEngine(); // For answering non-temporal queries
    private final QueryCache _queryCache = new QueryCache(); // Caches intermediate answers
    private final SortingStrategy _sorter = new SimpleSortingStrategy(); // To reorder conjunctions for efficiency
    private boolean _streaming = false; // Whether to fetch the input data from the 0MQ port
    private int _port = -1; // The port used for 0MQ (when in streaming mode)
    private MTCQEngineUI _ui = null; // If null: Do not print results in UI

    /**
     * Creates a new MTCQ engine for answering MTCQs via rewriting into normal form.
     */
    public MTCQNormalFormEngine()
    {
        super();
    }

    /**
     * Creates a new MTCQ engine for answering MTCQs via rewriting into normal form.
     * @param streaming If true, listens to the specified port for 0MQ messages containing the data.
     * @param port The 0MQ port when in streaming mode.
     */
    public MTCQNormalFormEngine(boolean streaming, int port)
    {
        super();
        _streaming = streaming;
        _port = port;
    }

    /**
     * Creates a new MTCQ engine for answering MTCQs via rewriting into normal form.
     * @param streaming If true, listens to the specified port for 0MQ messages containing the data.
     * @param ui The UI to pass (intermediate) results to, which are then printed to the user.
     * @param port The 0MQ port when in streaming mode.
     */
    public MTCQNormalFormEngine(boolean streaming, MTCQEngineUI ui, int port)
    {
        super();
        _streaming = streaming;
        _ui = ui;
        _port = port;
    }

    @Override
    protected QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings,
                                        QueryResult restrictToBindings)
    {
        CXNFTransformer.resetCache();
        QueryResult result = answer(q);
        // If required, consider result restriction after computing bindings. This is no primary use case of the MTCQ
        // engine and thus does not matter from a performance perspective.
        if (restrictToBindings != null && !restrictToBindings.isEmpty())
            result.retainAll(restrictToBindings);
        return result;
    }

    /**
     * Core functionality of the query engine. Answers the MTCQ by implementing the rewriting-based algorithm presented
     * in 'Temporal Conjunctive Query Answering via Rewriting'.
     * Allows for both a streaming and offline setting, which is configured in the constructor of this class.
     * The global timer, if given, is stopped on loading of knowledge bases.
     * @param query The MTCQ to answer, containing a pointer to the temporal knowledge base to reason over.
     * @return The certain answers of the query w.r.t. the temporal knowledge base.
     */
    private QueryResult answer(MetricTemporalConjunctiveQuery query)
    {
        TemporalQueryResult temporalResultAt0 = new TemporalQueryResult(query);
        TemporalIterationState iteration = new TemporalIterationState(query, _streaming, _port, _timer);
        List<MTCQAnsweringToDo> todoList = new ArrayList<>();
        todoList.add(new MTCQAnsweringToDo(query, temporalResultAt0));

        // Main loop: Iteration over all timestamps in the database until last knowledge base.
        while (true)
        {
            List<MTCQAnsweringToDo> nextTodoList = new ArrayList<>();
            for (MTCQAnsweringToDo todo : todoList)
            {
                QueryResult candidates = todo.candidates;
                MetricTemporalConjunctiveQuery transformed = CXNFTransformer.transform(todo.query);
                CXNFVerifier verifier = new CXNFVerifier();
                if (!verifier.verify(transformed))
                    throw new RuntimeException("Unexpected: After transformation, MTCQ is not in normal form. " +
                            "Reason is: " + verifier.getReason());

                List<MetricTemporalConjunctiveQuery> flattenedCNF = _sorter.sort(flattenAnd(transformed));
                for (MetricTemporalConjunctiveQuery conjunct : flattenedCNF)
                {
                    // Two cases:
                    // 1. a conjunct is non-temporal (can be answered directly)
                    // 2. it is temporal (required recursive answering)
                    if (!conjunct.isTemporal())
                        answerNonTemporalConjunct(conjunct, candidates, query.getResultVars(), todo, iteration);
                    else
                    {
                        // According to the normal form, the formula can either be a StrongNextFormula or an OrFormula
                        // containing a StrongNextFormula and a non-temporal formula (which we answer directly).
                        StrongNextFormula nextFormula;
                        QueryResult atemporalOrResult = null;
                        if (conjunct instanceof OrFormula or)
                        {
                            Pair<MetricTemporalConjunctiveQuery, MetricTemporalConjunctiveQuery> parts =
                                    or.splitIntoTemporalAndNonTemporalPart();
                            atemporalOrResult = answerNonTemporalPartOfNormalFormOr(parts.first, candidates, iteration,
                                    query.getResultVars());
                            nextFormula = (StrongNextFormula) parts.second;
                        }
                        else
                            nextFormula = (StrongNextFormula) conjunct;
                        // Assembles candidates for inner part of next formula
                        TemporalQueryResult nextTemporalQueryResult = answerTemporalPartOfNormalFormOr(nextFormula,
                                atemporalOrResult, candidates, nextTodoList);
                        // Adds assembled temporal query result and atemporal query result to current To-Do
                        todo.temporalQueryResult.addNewConjunct(atemporalOrResult, nextTemporalQueryResult);
                    }
                }
            }
            // Prepare for next iteration (and check if it is required).
            todoList = nextTodoList;
            _queryCache.invalidate();
            QueryEngine.getCache().invalidate();
            iteration.notifyUIAndStreamingAboutIterationEnd();
            if (iteration.hasNext())
                iteration = iteration.loadNextIteration();
            else
                break;
        }

        // Assembles final results and notifies all listeners.
        // For this, adds empty query result for all things still in to-do list (they exceeded the trace length).
        for (MTCQAnsweringToDo todo : todoList)
            todo.temporalQueryResult.addNewConjunct(new QueryResultImpl(todo.query));
        QueryResult res = temporalResultAt0.collapse();
        iteration.notifyUIAndStreamingAboutResult(res);
        return res;
    }

    /**
     * TODO
     * Can, for efficiency reasons, modify existing temporal query results in the To-Do list.
     * @param nextFormula StrongNextFormula contained in the OrFormula according to the normal form.
     * @param atempOrResult Result of the nontemporal part of the OrFormula.
     * @param candidates Already gathered candidates that have to be examined. If null, no candidates exist.
     * @param nextTodoList To-Do list for the next iteration. Required for performance reasons.
     *                     Warning: Can be modified!
     * @return The temporal query result representing the result for the temporal OrFormula.
     */
    private TemporalQueryResult answerTemporalPartOfNormalFormOr(StrongNextFormula nextFormula,
                                                                 QueryResult atempOrResult,
                                                                 @Nullable QueryResult candidates,
                                                                 List<MTCQAnsweringToDo> nextTodoList)
    {
        QueryResult nextCandidates = null;
        if (candidates != null)
        {
            nextCandidates = candidates.copy();
            if (atempOrResult != null)
                // Already found answer - no need to check anymore
                nextCandidates.removeAll(atempOrResult);
        }
        // Check if we can merge with some existing todos
        //  -> then we just use the existing temporal query result
        TemporalQueryResult nextTemporalQueryResult = null;
        for (MTCQAnsweringToDo existingToDo : nextTodoList)
            if (nextFormula.getSubFormula().equals(existingToDo.query))
            {
                if (existingToDo.candidates != null)
                    existingToDo.candidates.addAll(nextCandidates);
                nextTemporalQueryResult = existingToDo.temporalQueryResult;
                break;
            }
        // TQR not found - assembles new temporal query result and creates new entry in todo list
        if (nextTemporalQueryResult == null)
        {
            nextTemporalQueryResult = new TemporalQueryResult(nextFormula.getSubFormula());
            nextTodoList.add(new MTCQAnsweringToDo(
                    nextFormula.getSubFormula(), nextCandidates, nextTemporalQueryResult));
        }
        return nextTemporalQueryResult;
    }

    /**
     * TODO
     * @param nonTemporalFormula
     * @param candidates
     * @param iteration
     * @param vars
     * @return
     */
    private QueryResult answerNonTemporalPartOfNormalFormOr(MetricTemporalConjunctiveQuery nonTemporalFormula,
                                                            QueryResult candidates, TemporalIterationState iteration,
                                                            Collection<ATermAppl> vars)
    {
        QueryResult result = null;
        QueryResult localCandidates = null;
        if (candidates != null)
            localCandidates = candidates.copy();
        for (MetricTemporalConjunctiveQuery ucq : _sorter.sort(flattenAnd(nonTemporalFormula)))
        {
            QueryResult ucqResult = answerUCQWithNegations(ucq, iteration, localCandidates, vars);
            if (result == null)
                result = ucqResult;
            else
                result.retainAll(ucqResult);
            if (localCandidates == null)
                localCandidates = result.copy();
            else
                localCandidates.retainAll(ucqResult);
        }
        return result;
    }

    /**
     * Answers a non-temporal conjunct in some conjunction, by respecting previously generated candidates and updating
     * them accordingly after result computation. Updates the {@code todo} such that the results are stored there.
     * @param conjunct The conjunct to check.
     * @param candidates The candidates generated for previous conjuncts.
     * @param vars Variables of the overall MTCQ (results get possibly expanded if the conjunct refers to only a subset
     *             of them).
     * @param todo The data storage to put the results into.
     * @param iteration The current iteration state (for time and knowledge base information).
     */
    private void answerNonTemporalConjunct(MetricTemporalConjunctiveQuery conjunct, QueryResult candidates,
                                           Collection<ATermAppl> vars,  MTCQAnsweringToDo todo,
                                           TemporalIterationState iteration) {
        QueryResult atempResult = null;
        // In case we have a conjunct which is again a conjunction of UCQs, e.g., of the form (a | b | c) & (d & e).
        for (MetricTemporalConjunctiveQuery ucq : _sorter.sort(flattenAnd(conjunct)))
        {
            QueryResult ucqResult = answerUCQWithNegations(ucq, iteration, candidates, vars);
            if (atempResult == null)
                atempResult = ucqResult;
            else
                atempResult.retainAll(ucqResult);
            if (candidates == null)
                candidates = atempResult.copy();
            else
                candidates.retainAll(ucqResult);
        }
        todo.temporalQueryResult.addNewConjunct(atempResult);
    }

    /**
     * Non-temporal base case of the MTCQ rewriting engine. Answers UCQs with possible negated CQs in it.
     * Essentially wraps the {@code BDQEngine} and handles non-temporal non-CQ formulae such as last or end.
     * @param q An MTCQ that is a UCQ with only basic atoms (end, last, true, false), CQs, or negated CQs in it.
     * @param iteration The non-temporal knowledge base to answer the query over.
     * @param candidates A set of candidates. If not empty, only these candidates will be considered as possible
     *                   answers.
     * @param variables A set of all variables, in case the overall MTCQ contains more variables than the given UCQ.
     *                  If this is the case, the function extends its generated answers to cover all variables.
     * @return The answers to the given query.
     */
    private QueryResult answerUCQWithNegations(MetricTemporalConjunctiveQuery q, TemporalIterationState iteration,
                                               QueryResult candidates, Collection<ATermAppl> variables)
    {
        Pair<QueryResult, QueryResult> cache = _queryCache.fetch(q, candidates);
        QueryResult result = cache.first;
        candidates = cache.second;
        if (candidates.isEmpty())
            return result;

        // Query answering
        QueryResult newResult;
        List<MetricTemporalConjunctiveQuery> cleanDisjuncts = new ArrayList<>();
        // First, check if query contains Last or True and we do not need to examine anything.
        // Otherwise, assembles a cleaned-up disjunct (without above formulae).
        for (MetricTemporalConjunctiveQuery disjunct : flattenOr(q))
        {
            // Note: EndFormula always unsatisfiable before or at last KB -> skip
            if (disjunct instanceof LastFormula && iteration.isLast())
                return candidates.copy();
            else if (disjunct instanceof PropositionalTrueFormula || disjunct instanceof LogicalTrueFormula)
                return candidates.copy();
            else if (disjunct instanceof ConjunctiveQueryFormula ||
                    (disjunct instanceof NotFormula not && not.getSubFormula() instanceof ConjunctiveQueryFormula))
            {
                cleanDisjuncts.add(disjunct);
                disjunct.setKB(iteration.getKB());
            }
        }
        // Non-temporal query answering using BDQ engine (general case).
        if (cleanDisjuncts.size() > 1)
        {
            OrFormula orFormula = makeOr(cleanDisjuncts);
            orFormula.setKB(iteration.getKB());  // TODO fix correct setting of KB in makeOr()
            newResult = _bdqEngine.execABoxQuery(orFormula, null, candidates);
        }
        // Non-temporal query answering using either BDQ engine or handling of False.
        else if (cleanDisjuncts.size() == 1)
        {
            MetricTemporalConjunctiveQuery one = cleanDisjuncts.get(0);
            if (one instanceof LogicalFalseFormula || one instanceof PropositionalFalseFormula ||
                    one instanceof EmptyFormula)
                newResult = new QueryResultImpl(one);
            else
                newResult = _bdqEngine.execABoxQuery(one, null, candidates);
        }
        // We have a formula of the form "last v end v last v false ..." and are not at last or end point.
        //   -> Nothing can entail this formula
        else
            newResult = new QueryResultImpl(q);

        // Result assembly
        if (newResult instanceof MultiQueryResults m)
            newResult = m.toQueryResultImpl(q);
        result.addAll(newResult);
        // Expands to all variables
        if (!variables.equals(result.getResultVars()))
        {
            result.expandToAllVariables(variables);
            result.explicate();
        }
        _queryCache.add(q, candidates, result);
        iteration.notifyUIAboutResult(result);
        return result;
    }
}
