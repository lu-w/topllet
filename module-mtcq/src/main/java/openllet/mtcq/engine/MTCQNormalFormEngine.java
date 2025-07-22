package openllet.mtcq.engine;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.Pair;
import openllet.mtcq.engine.atemporal.BDQEngine;
import openllet.mtcq.engine.rewriting.CXNFTransformer;
import openllet.mtcq.engine.rewriting.CXNFVerifier;
import openllet.mtcq.model.kb.StreamingDataHandler;
import openllet.mtcq.model.query.*;
import openllet.mtcq.ui.StreamingUIHandler;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.engine.QueryCache;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.model.results.MultiQueryResults;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;

import java.util.*;

import static openllet.mtcq.engine.rewriting.MTCQSimplifier.*;

public class MTCQNormalFormEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    private final BDQEngine _bdqEngine = new BDQEngine();
    private final QueryCache _queryCache = new QueryCache();
    private boolean _streaming = false;
    private int _port = 0; // 0: don't send results via 0MQ; >0: send results
    private StreamingUIHandler _ui = null;

    public MTCQNormalFormEngine()
    {
        super();
    }

    public MTCQNormalFormEngine(boolean streaming)
    {
        super();
        _streaming = streaming;
    }

    public MTCQNormalFormEngine(boolean streaming, StreamingUIHandler ui)
    {
        super();
        _streaming = streaming;
        _ui = ui;
    }

    public MTCQNormalFormEngine(boolean streaming, int port)
    {
        super();
        _streaming = streaming;
        _port = port;
    }
    public MTCQNormalFormEngine(boolean streaming, StreamingUIHandler ui, int port)
    {
        super();
        _streaming = streaming;
        _ui = ui;
        _port = port;
    }

    @Override
    protected QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        CXNFTransformer.resetCache();
        return answerTime(q);
    }

    static class ToDo
    {
        MetricTemporalConjunctiveQuery query;
        QueryResult candidates;
        TemporalQueryResult temporalQueryResult;
        ToDo(MetricTemporalConjunctiveQuery query, QueryResult candidates, TemporalQueryResult temporalQueryResult)
        {
            this.query = query;
            this.candidates = candidates;
            this.temporalQueryResult = temporalQueryResult;
        }
        ToDo(MetricTemporalConjunctiveQuery query, TemporalQueryResult temporalQueryResult)
        {
            this.query = query;
            this.candidates = null;
            this.temporalQueryResult = temporalQueryResult;
        }
    }

    private QueryResult answerTime(MetricTemporalConjunctiveQuery query)
    {
        Collection<ATermAppl> vars = query.getResultVars();
        TemporalQueryResult temporalResultAt0 = new TemporalQueryResult(query);
        // Elements are of the form: query, candidates to check against, temporal result to write to.
        List<ToDo> todoList = new ArrayList<>();
        todoList.add(new ToDo(query, temporalResultAt0));
        int maxTime;
        boolean isLast;
        KnowledgeBase kb;
        StreamingDataHandler streamer;

        if (_timer != null) _timer.stop();
        if (_streaming)
        {
            kb = query.getTemporalKB().get(0);
            streamer = new StreamingDataHandler(kb, _port, query.getTemporalKB().getTimer());
            maxTime = Integer.MAX_VALUE;
        }
        else
        {
            kb = query.getTemporalKB().get(0);
            streamer = null;
            maxTime =  query.getTemporalKB().size();
        }
        if (_timer != null) _timer.start();

        for (int t = 0; t < maxTime; t++)
        {
            if (_timer != null) _timer.stop(); // Timer shall not consider loading of KBs
            if (_ui != null) _ui.informAboutStartOfIteration(t);
            if (_streaming)
            {
                streamer.waitAndUpdateKB();
                isLast = streamer.isLast();
            }
            else
            {
                kb = query.getTemporalKB().get(t);
                isLast = t == (maxTime - 1);
            }
            if (_timer != null) _timer.start();
            List<ToDo> nextTodoList = new ArrayList<>();
            for (ToDo todo : todoList)
            {
                QueryResult candidates = todo.candidates;
                MetricTemporalConjunctiveQuery transformed = CXNFTransformer.transform(todo.query);
                CXNFVerifier verifier = new CXNFVerifier();
                if (!verifier.verify(transformed))
                    throw new RuntimeException("Unexpected: After transformation, MTCQ is not in normal form. " +
                            "Reason is: " + verifier.getReason());

                List<MetricTemporalConjunctiveQuery> flattenedCNF = sort(flattenAnd(transformed));
                for (MetricTemporalConjunctiveQuery conjunct : flattenedCNF)
                {
                    if (!conjunct.isTemporal())  // TODO || conjunct instanceof OrFormula or && or.isOverDifferentResultVars()
                    {
                        QueryResult atempResult = null;
                        for (MetricTemporalConjunctiveQuery ucq : sort(flattenAnd(conjunct)))
                        {
                            QueryResult ucqResult = answerUCQWithNegations(ucq, kb, isLast, candidates, vars);
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
                            for (MetricTemporalConjunctiveQuery ucq : sort(flattenAnd(atempoOrPart)))
                            {
                                QueryResult ucqResult = answerUCQWithNegations(ucq, kb, isLast, localCandidates, vars);
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
                                    nextCandidates.removeAll(atempOrResult); // already found answer - no need to check anymore
                            }
                            // check if we can merge with some existing todos - then we just use the existing temporal query result
                            TemporalQueryResult nextTemporalQueryResult = null;
                            for (ToDo existingToDo : nextTodoList)
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
                                nextTodoList.add(new ToDo(XtempPart.getSubFormula(), nextCandidates, nextTemporalQueryResult));
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
            if (_timer != null) _timer.stop();
            if (_ui != null)
            {
                _ui.informAboutResults(t, kb, query, null); // we cannot pass a result due to lazy evaluation
                _ui.informAboutEndOfIteration(t);
            }
            if (_streaming)
            {
                if (isLast)
                {
                    maxTime = t;
                    break;
                }
                streamer.sendAck();
            }
            if (_timer != null) _timer.start();
        }

        // Adds empty query result for all things still in to-do list (they exceeded the trace length).
        for (ToDo todo : todoList)
            todo.temporalQueryResult.addNewConjunct(new QueryResultImpl(todo.query));

        QueryResult res = temporalResultAt0.collapse();
        if (_ui != null) {
            _ui.informAboutResults(query.getTemporalKB().size() - 1, query.getTemporalKB().getLastLoadedKB(),
                    query, res);
            _ui.clear();
        }
        if (_streaming) streamer.sendResult(res);
        return res;
    }

    private List<MetricTemporalConjunctiveQuery> sort(List<MetricTemporalConjunctiveQuery> cnf)
    {
        List<MetricTemporalConjunctiveQuery> sorted = new ArrayList<>();
        for (MetricTemporalConjunctiveQuery conjunct : cnf)
            if (conjunct.isTemporal())
                sorted.add(conjunct);
            else if (answerableByCQ(conjunct))
                sorted.add(0, conjunct);
            else if (!sorted.isEmpty())
                if (sorted.get(0).isTemporal())
                    sorted.add(0, conjunct);
                else
                    sorted.add(1, conjunct);
            else
                sorted.add(0, conjunct);
        return sorted;
        // TODO adapt sorted s.t. atmeporal parts with disjunctions that are supersets of other disjunctions are answered first?
    }

    private boolean answerableByCQ(MetricTemporalConjunctiveQuery mtcq)
    {
        if (mtcq instanceof ConjunctiveQueryFormula)
            return true;
        else if (mtcq instanceof OrFormula or)
        {
            int numberOfCQs = 0;
            for (MetricTemporalConjunctiveQuery disjunct : flattenOr(or))
                if (disjunct instanceof ConjunctiveQueryFormula)
                    numberOfCQs++;
            return numberOfCQs <= 1;
        }
        else
            return true;
    }

    private QueryResult answerUCQWithNegations(MetricTemporalConjunctiveQuery q, KnowledgeBase kb, boolean isLastKB,
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
                if (disjunct instanceof LastFormula && isLastKB)
                    return candidates.copy();
                else if (disjunct instanceof PropositionalTrueFormula || disjunct instanceof LogicalTrueFormula)
                    return candidates.copy();
                else if (disjunct instanceof ConjunctiveQueryFormula ||
                        (disjunct instanceof NotFormula not && not.getSubFormula() instanceof ConjunctiveQueryFormula))
                {
                    cleanDisjuncts.add(disjunct);
                    disjunct.setKB(kb);
                }
            }
            if (cleanDisjuncts.size() > 1)
            {
                OrFormula orFormula = makeOr(cleanDisjuncts);
                orFormula.setKB(kb);  // TODO fix correct setting of KB in makeOr()
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
                // we have a formula of the form "last v end v last v false v false ..." and are not at last or end point.
                //   -> nothing can entail this formula
                newResult = new QueryResultImpl(q);
            if (newResult instanceof MultiQueryResults m)
                newResult = m.toQueryResultImpl(q);
            result.addAll(newResult);
            // expands to all variables (can probably be done more efficiently) - suffices for now
            if (!variables.equals(result.getResultVars()))
            {
                result.expandToAllVariables(variables);
                result.explicate();
            }
            _queryCache.add(q, candidates, result);  // TODO maybe add overwrite() functionality?
            if (_ui != null) _ui.informAboutResults(-1, kb, q, result);
        }
        return result;
    }
}
