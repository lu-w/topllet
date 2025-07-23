package openllet.mtcq.engine.engine_rewriting;

import openllet.aterm.ATermAppl;
import openllet.core.utils.Pair;
import openllet.mtcq.engine.atemporal.BDQEngine;
import openllet.mtcq.engine.rewriting.CXNFTransformer;
import openllet.mtcq.engine.rewriting.CXNFVerifier;
import openllet.mtcq.model.query.*;
import openllet.mtcq.ui.StreamingUIHandler;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.engine.QueryCache;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.model.results.MultiQueryResults;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import openllet.shared.tools.Log;

import java.util.*;
import java.util.logging.Logger;

import static openllet.mtcq.engine.rewriting.MTCQSimplifier.*;

/**
 * Query engine for answering non-Boolean metric temporal conjunctive queries via rewriting into a normal form.
 * TODO:
 *   - add logging
 *   - comments & docstrings
 */
public class MTCQNormalFormEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    public static final Logger _logger = Log.getLogger(MTCQNormalFormEngine.class);

    private final BDQEngine _bdqEngine = new BDQEngine(); // For answering non-temporal queries
    private final QueryCache _queryCache = new QueryCache(); // Caches intermediate answers
    private final SortingStrategy _sorter = new SimpleSortingStrategy(); // To reorder conjunctions for efficiency
    private boolean _streaming = false; // Whether to fetch the input data from the 0MQ port
    private int _port = -1; // The port used for 0MQ (when in streaming mode)
    private StreamingUIHandler _ui = null; // If null: Do not print results in UI

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
    public MTCQNormalFormEngine(boolean streaming, StreamingUIHandler ui, int port)
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
        Collection<ATermAppl> vars = query.getResultVars();
        TemporalQueryResult temporalResultAt0 = new TemporalQueryResult(query);
        TemporalIterationState iteration = new TemporalIterationState(query, _streaming, _port, _timer);
        List<MTCQAnsweringToDo> todoList = new ArrayList<>();
        todoList.add(new MTCQAnsweringToDo(query, temporalResultAt0));

        // Main loop: Iteration over all timestamps in the database until last knowledge base.
        while (true)
        {
            System.out.println(iteration.getTimePoint());
            System.out.println(iteration.getMaxTime());
            System.out.println();

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
                    // TODO || conjunct instanceof OrFormula or && or.isOverDifferentResultVars()
                    if (!conjunct.isTemporal())
                    {
                        QueryResult atempResult = null;
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
                    else
                    {
                        MetricTemporalConjunctiveQuery tempPart;
                        QueryResult atempOrResult = null;
                        if (conjunct instanceof OrFormula or)
                        {
                            MetricTemporalConjunctiveQuery atempoOrPart;
                            if (!or.getLeftSubFormula().isTemporal())
                            {
                                atempoOrPart = or.getLeftSubFormula();
                                tempPart = or.getRightSubFormula();
                            }
                            else
                            {
                                tempPart = or.getLeftSubFormula();
                                atempoOrPart = or.getRightSubFormula();
                            }
                            QueryResult localCandidates = null;
                            if (candidates != null)
                                localCandidates = candidates.copy();
                            for (MetricTemporalConjunctiveQuery ucq : _sorter.sort(flattenAnd(atempoOrPart)))
                            {
                                QueryResult ucqResult = answerUCQWithNegations(ucq, iteration, localCandidates, vars);
                                if (atempOrResult == null)
                                    atempOrResult = ucqResult;
                                else
                                    atempOrResult.retainAll(ucqResult);
                                if (localCandidates == null)
                                    localCandidates = atempOrResult.copy();
                                else
                                    localCandidates.retainAll(ucqResult);
                            }
                        }
                        else  // must be of StrongNextFormula
                            tempPart = conjunct;
                        if (tempPart instanceof StrongNextFormula XtempPart)
                        {
                            // assembles candidates for inner part of next formula
                            QueryResult nextCandidates = null;
                            if (candidates != null)
                            {
                                nextCandidates = candidates.copy();
                                if (atempOrResult != null)
                                    // already found answer - no need to check anymore
                                    nextCandidates.removeAll(atempOrResult);
                            }
                            // check if we can merge with some existing todos
                            //  -> then we just use the existing temporal query result
                            TemporalQueryResult nextTemporalQueryResult = null;
                            for (MTCQAnsweringToDo existingToDo : nextTodoList)
                                if (XtempPart.getSubFormula().equals(existingToDo.query))
                                {
                                    if (existingToDo.candidates != null)
                                        existingToDo.candidates.addAll(nextCandidates);
                                    nextTemporalQueryResult = existingToDo.temporalQueryResult;
                                    break;
                                }
                            // TQR not found - assembles new temporal query result and creates new entry in todo list
                            if (nextTemporalQueryResult == null)
                            {
                                nextTemporalQueryResult = new TemporalQueryResult(XtempPart.getSubFormula());
                                nextTodoList.add(new MTCQAnsweringToDo(
                                        XtempPart.getSubFormula(), nextCandidates, nextTemporalQueryResult));
                            }
                            // adds assembled temporal query result and atemporal query result to current todo
                            todo.temporalQueryResult.addNewConjunct(atempOrResult, nextTemporalQueryResult);
                        }
                        else
                            throw new RuntimeException("Unexpected temporal operator: " + tempPart.getClass());
                    }
                }
            }
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
     * Non-temporal base case of the MTCQ rewriting engine. Answers UCQs with possible negated CQs in it.
     * Essentially wraps the {@code BDQEngine} and handles non-temporal non-CQ formulae such as last or end.
     * @param q An MTCQ that is a UCQ with only basic atoms (end, last, true, false), CQs, or negated CQs in it.
     * @param kb The non-temporal knowledge base to answer the query over.
     * @param candidates A set of candidates. If not empty, only these candidates will be considered as possible
     *                   answers.
     * @param variables A set of all variables, in case the overall MTCQ contains more variables than the given UCQ.
     *                  If this is the case, the function extends its generated answers to cover all variables.
     * @return The answers to the given query.
     */
    private QueryResult answerUCQWithNegations(MetricTemporalConjunctiveQuery q, TemporalIterationState kb,
                                               QueryResult candidates, Collection<ATermAppl> variables)
    {
        Pair<QueryResult, QueryResult> cache = _queryCache.fetch(q, candidates);
        QueryResult result = cache.first;
        candidates = cache.second;
        if (!candidates.isEmpty())
        {
            MetricTemporalConjunctiveQuery toPrint = null;
            QueryResult newResult;
            List<MetricTemporalConjunctiveQuery> cleanDisjuncts = new ArrayList<>();
            for (MetricTemporalConjunctiveQuery disjunct : flattenOr(q))
            {
                // Note: EndFormula always unsatisfiable before or at last KB -> skip
                if (disjunct instanceof LastFormula && kb.isLast())
                    return candidates.copy();
                else if (disjunct instanceof PropositionalTrueFormula || disjunct instanceof LogicalTrueFormula)
                    return candidates.copy();
                else if (disjunct instanceof ConjunctiveQueryFormula ||
                        (disjunct instanceof NotFormula not && not.getSubFormula() instanceof ConjunctiveQueryFormula))
                {
                    cleanDisjuncts.add(disjunct);
                    disjunct.setKB(kb.getKB());
                }
            }
            if (cleanDisjuncts.size() > 1)
            {
                OrFormula orFormula = makeOr(cleanDisjuncts);
                orFormula.setKB(kb.getKB());  // TODO fix correct setting of KB in makeOr()
                newResult = _bdqEngine.execABoxQuery(orFormula, null, candidates);
                toPrint = orFormula;
            }
            else if (cleanDisjuncts.size() == 1)
            {
                MetricTemporalConjunctiveQuery one = cleanDisjuncts.get(0);
                if (one instanceof LogicalFalseFormula || one instanceof PropositionalFalseFormula ||
                        one instanceof EmptyFormula)
                    newResult = new QueryResultImpl(one);
                else
                {
                    newResult = _bdqEngine.execABoxQuery(one, null, candidates);
                    toPrint = one;
                }
            }
            else
                // We have a formula of the form "last v end v last v false ..." and are not at last or end point.
                //   -> Nothing can entail this formula
                newResult = new QueryResultImpl(q);
            if (newResult instanceof MultiQueryResults m)
                newResult = m.toQueryResultImpl(q);
            result.addAll(newResult);
            // Expands to all variables
            if (!variables.equals(result.getResultVars()))
            {
                result.expandToAllVariables(variables);
                result.explicate();
            }
            _queryCache.add(q, candidates, result);  // TODO maybe add overwrite() functionality?
            if (_ui != null) _ui.informAboutResults(kb.getTimePoint(), kb.getKB(), q, result);
        }
        return result;
    }
}
