package openllet.tcq.engine;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.engine.AbstractBooleanQueryEngine;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.engine.cncq.CNCQQueryEngineSimple;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.shared.tools.Log;
import openllet.tcq.engine.automaton.MLTL2DFA;
import openllet.tcq.model.automaton.DFA;
import openllet.tcq.model.automaton.Edge;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.core.utils.Timer;
import openllet.tcq.parser.ParseException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This query engine is deprecated - the non-Boolean TCQ engine handles the Boolean case as well, as it is more
 * efficient.
 */
@Deprecated
public class BooleanTCQEngine extends AbstractBooleanQueryEngine<TemporalConjunctiveQuery>
        implements QueryExec<TemporalConjunctiveQuery>
{
    public static final Logger _logger = Log.getLogger(BooleanTCQEngine.class);

    private final QueryExec<CNCQQuery> _cncqQueryEngine = new CNCQQueryEngineSimple();

    @Override
    protected boolean execBooleanABoxQuery(TemporalConjunctiveQuery q) throws IOException, InterruptedException
    {
        _logger.fine("Starting entailment check for TCQ " + q);
        String negTcqProp = q.toNegatedPropositionalAbstractionString();
        _logger.finer("Checking DFA satisfiability for negated and propositionally abstracted TCQ " + negTcqProp);
        try
        {
            DFA automaton = MLTL2DFA.convert(negTcqProp);
            boolean dfaSatisfiable = _checkDFASatisfiability(automaton, q);
            _logger.finer("DFA check returned " + (dfaSatisfiable ? "satisfiable" : "unsatisfiable") +
                    ", therefore TCQ is " + (dfaSatisfiable ? "not entailed" : "entailed"));
            return !dfaSatisfiable;
        }
        catch (ParseException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    private boolean _checkDFASatisfiability(DFA dfa, TemporalConjunctiveQuery tcq) throws IOException,
            InterruptedException
    {
        Timer cncqTimer = new Timer();
        Integer initState = dfa.getInitialState();
        Set<Integer> states = new HashSet<>();
        if (initState != null)
            states.add(initState);
        _logger.finer("Starting in states " + states);
        int numLetter = 0;
        if (!states.isEmpty())
        {
            boolean trappedInAcceptingSink = false;
            Set<Integer> eventuallyReachableStates = new HashSet<>();
            Iterator<KnowledgeBase> kbIterator = tcq.getTemporalKB().iterator();
            while (kbIterator.hasNext() && !trappedInAcceptingSink && !states.isEmpty())
            {
                if (_timer != null)
                    _timer.stop();
                KnowledgeBase letter =  kbIterator.next();
                if (_timer != null)
                    _timer.start();
                _logger.finer("Checking ABox #" + numLetter + " for states " + states);
                Set<Integer> newStates = new HashSet<>();
                for (int state : states)
                {
                    _logger.finer("\tExamining state " + state);
                    List<Edge> edges = dfa.getEdges(state);
                    if (edges.size() == 1)
                    {
                        List<CNCQQuery> cncqs = edges.get(0).getCNCQs();
                        int toState = edges.get(0).getToState();
                        if (cncqs.size() == 1 && cncqs.get(0).isEmpty())
                        {
                            // check if we are in a sink (i.e. state - X -> state)
                            if (toState == state)
                            {
                                _logger.finer("\t\tSink detected at state " + state);
                                if (dfa.isAccepting(toState))
                                {
                                    // we are "trapped" in an accepting sink - early escape from looping over ABoxes
                                    trappedInAcceptingSink = true;
                                    _logger.finer("Early abort of DFA iteration - trapped in accepting sink " + toState);
                                    break;
                                }
                                else
                                {
                                    // continue with next state. no need to add it to the newStates list as it will not
                                    // be useful for reachability anymore (it's a sink)
                                    eventuallyReachableStates.add(toState);
                                    continue;
                                }
                            }
                            // check if we need to a CNCQ check at all for this edge
                            else
                            {
                                newStates.add(toState);
                                continue;
                            }
                        }
                    }
                    // if we are not in a sink and have some CNCQ to check, we find the successor states
                    for (Edge edge : edges)
                    {
                        _logger.finer("\t\tChecking edge " + edge + " to state " + edge.getToState());
                        if (!newStates.contains(edge.getToState()))
                        {
                            boolean edgeSat = false;
                            for (CNCQQuery cncq : edge.getCNCQs())
                            {
                                _logger.finer("\t\t\tChecking CNCQ " + cncq);
                                if (!cncq.isEmpty())
                                {
                                    cncq.setKB(letter);
                                    cncqTimer.start();
                                    boolean querySat = !_cncqQueryEngine.exec(cncq).isEmpty();
                                    cncqTimer.stop();
                                    if (querySat)
                                    {
                                        newStates.add(edge.getToState());
                                        edgeSat = true;
                                        _logger.finer("\t\t\tCNCQ is satisfiable!");
                                        break;
                                    }
                                    else
                                        _logger.finer("\t\t\tCNCQ unsatisfiable!");
                                }
                                else
                                {
                                    newStates.add(edge.getToState());
                                    edgeSat = true;
                                    _logger.finer("\t\t\tCNCQ empty, therefore trivially satisfied");
                                    break;
                                }
                            }
                            if (edgeSat)
                                _logger.finer("\t\tEdge satisfied, added " + edge.getToState() + " to new states");
                        }
                        else
                            _logger.finer("\t\t\tEdge can be ignored since successor state " + edge.getToState() +
                                    " is already in new states");
                    }
                }
                states = new HashSet<>(newStates);
                numLetter++;
            }
            states.addAll(eventuallyReachableStates);
            // it suffices if one of the reached states is accepting (or we were trapped in an accepting sink)
            boolean isAccepting = trappedInAcceptingSink;
            for (int state : states)
                isAccepting |= dfa.isAccepting(state);
            if (!trappedInAcceptingSink)
                _logger.fine("Finished iteration on ABoxes, end states are " + states + " which are " +
                        (isAccepting ? "accepting" : "not accepting"));
            else
                _logger.fine("DFA accepts the ABoxes due to being trapped in an accepting sink");
            _logger.fine("Checked a total of " + cncqTimer.getCount() + " CNCQ queries, which took " +
                    cncqTimer.getTotal() + " ms");
            return isAccepting;
        }
        else
        {
            // No initial state -> L(A) is empty
            return false;
        }
    }
}
