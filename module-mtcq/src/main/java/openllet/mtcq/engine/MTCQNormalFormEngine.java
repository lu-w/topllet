package openllet.mtcq.engine;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.Pair;
import openllet.mtcq.engine.atemporal.BDQEngine;
import openllet.mtcq.engine.rewriting.CXNFTransformer;
import openllet.mtcq.engine.rewriting.CXNFVerifier;
import openllet.mtcq.model.kb.StreamingDataHandler;
import openllet.mtcq.model.query.*;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.engine.QueryCache;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.model.results.MultiQueryResults;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.gui2.*;
import openllet.query.sparqldl.model.results.ResultBinding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;
import static openllet.mtcq.engine.rewriting.MTCQSimplifier.*;

public class MTCQNormalFormEngine extends AbstractQueryEngine<MetricTemporalConjunctiveQuery>
{
    private final BDQEngine _bdqEngine = new BDQEngine();
    private final QueryCache _queryCache = new QueryCache();
    private boolean _streaming = false;
    private int _port = 0; // 0: don't send results via 0MQ; >0: send results
    private Screen _screen;
    private Window _window;
    private MultiWindowTextGUI _gui;
    private final Map<MetricTemporalConjunctiveQuery, QueryResult> _resultsToPrintInStreamingMode = new HashMap<>();
    private final boolean _displayGUI = false; // TODO make command line option for GUI

    public MTCQNormalFormEngine()
    {
        super();
    }

    public MTCQNormalFormEngine(boolean streaming)
    {
        super();
        _streaming = streaming;
    }

    public MTCQNormalFormEngine(boolean streaming, int port)
    {
        super();
        _streaming = streaming;
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
        if (_timer != null)
            _timer.stop();
        if (_streaming)
        {
            kb = query.getTemporalKB().get(0);
            streamer = new StreamingDataHandler(kb, _port, query.getTemporalKB().getTimer());
            maxTime = Integer.MAX_VALUE;
            if (_displayGUI)
                setupOutput();
        }
        else
        {
            kb = query.getTemporalKB().get(0);
            streamer = null;
            maxTime =  query.getTemporalKB().size();
        }
        if (_timer != null)
            _timer.start();
        for (int t = 0; t < maxTime; t++)
        {
            if (_timer != null)
                _timer.stop(); // Timer shall not consider loading of KBs
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
            if (_timer != null)
                _timer.start();
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
            if (_streaming)
            {
                if (_timer != null)
                    _timer.stop();
                if (_displayGUI)
                    printResults(t, kb);
                _resultsToPrintInStreamingMode.clear();
                if (isLast)
                {
                    maxTime = t;
                    break;
                }
                if (_timer != null)
                    _timer.start();
                streamer.sendAck();
            }
        }



        // Adds empty query result for all things still in to-do list (they exceeded the trace length).
        for (ToDo todo : todoList)
            todo.temporalQueryResult.addNewConjunct(new QueryResultImpl(todo.query));

        QueryResult res = temporalResultAt0.collapse();
        if (_streaming)
        {
            if (_displayGUI)
            {
                _resultsToPrintInStreamingMode.put(query, res);
                printResults(maxTime, kb);
                try
                {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
            streamer.sendResult(res);
        }
        return res;
    }

    // TODO move out of engine
    private void printResults(int t, KnowledgeBase kb) {
        Panel panel = new Panel();
        panel.setSize(_screen.getTerminalSize());
        panel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        _window.setComponent(panel);
        Label toplletLabel = new Label("Topllet Stream Reasoner");
        panel.addComponent(toplletLabel);

        String aboxText = printAboxInfo(kb) + (_timer != null ? "\nTimer: " + _timer.getTotal() + " ms" : "");
        Panel aboxP = new Panel();
        aboxP.setSize(_screen.getTerminalSize());
        aboxP.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        Label aboxLabel = new Label("");
        aboxLabel.setLabelWidth(150);
        aboxLabel.setText(aboxText);
        Label aboxHead = new Label("");
        aboxHead.setLabelWidth(150);
        aboxHead.setText("ABox Stats (t = " + t + "):");
        aboxP.addComponent(aboxHead);
        aboxP.addComponent(aboxLabel);
        panel.addComponent(aboxP.withBorder(Borders.doubleLine()));

        List<MetricTemporalConjunctiveQuery> queries = new ArrayList<>(_resultsToPrintInStreamingMode.keySet().stream().toList());
        queries.sort(Comparator.comparing(Object::toString));
        for (MetricTemporalConjunctiveQuery q : queries) {
            String text = printSimpleResult(_resultsToPrintInStreamingMode.get(q));
            Panel p = new Panel();
            p.setSize(_screen.getTerminalSize());
            p.setLayoutManager(new LinearLayout(Direction.VERTICAL));
            Label lr = new Label("");
            lr.setLabelWidth(150);
            lr.setText(text);
            Label le = new Label("");
            le.setLabelWidth(150);
            Label lq = new Label("");
            lq.setLabelWidth(150);
            lq.setText(q.toString());
            p.addComponent(lq);
            p.addComponent(le);
            p.addComponent(lr);
            panel.addComponent(p.withBorder(Borders.doubleLine()));
        }
        try {
            _screen.doResizeIfNecessary();
            _gui.getGUIThread().processEventsAndUpdate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String printAboxInfo(KnowledgeBase kb)
    {
        int relCount = 0;
        int clsCount = 0;
        int indCount = 0;
        int litCount = 0;
        for (ATermAppl i : kb.getIndividuals())
        {
            indCount++;
            clsCount += max(0, kb.getABox().getIndividual(i).getTypes().size() - 2); // -2 due to _TOP_ and FunValue(i)
            relCount += kb.getABox().getIndividual(i).getOutEdges().size();
        }
        for (ATermAppl n : kb.getABox().getNodeList())
            if (kb.getABox().getLiteral(n) != null)
                litCount++;
        return "Individuals: " + indCount + "\nLiterals: " + litCount + "\nTypes: " + clsCount + "\nRelations: " +
                relCount + "\n";
    }

    private String printSimpleResult(QueryResult resultBindings)
    {
        StringBuilder res = new StringBuilder("{");
        for (ResultBinding binding : resultBindings)
        {
            res.append("(");
            for (ATermAppl var : resultBindings.getQuery().getResultVars())
                res.append(binding.getValue(var)).append(", ");
            if (res.length() > 2)
                res.delete(res.length() - 2, res.length());
            res.append("), ");
        }
        if (res.length() > 2)
            res.delete(res.length() - 2, res.length());
        res.append("}");
        return res.toString();
    }

    private void setupOutput()
    {
        try
        {
            Terminal _terminal = new UnixTerminal(System.in, System.out, StandardCharsets.UTF_8,
                    UnixLikeTerminal.CtrlCBehaviour.CTRL_C_KILLS_APPLICATION);
            _screen = new TerminalScreen(_terminal);
            _screen.startScreen();
            _window = new BasicWindow();
            _gui = new MultiWindowTextGUI(_screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
            _gui.addWindow(_window);
            _screen.doResizeIfNecessary();
            _gui.getGUIThread().processEventsAndUpdate();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
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
            if (toPrint != null)
                _resultsToPrintInStreamingMode.put(toPrint, result);
        }
        return result;
    }
}
