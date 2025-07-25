package openllet.mtcq.engine.automaton;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.engine.AbstractBooleanQueryEngine;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.engine.bcq.BCQQueryEngineSimple;
import openllet.query.sparqldl.model.bcq.BCQQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.shared.tools.Log;
import openllet.mtcq.model.automaton.DFA;
import openllet.mtcq.model.automaton.Edge;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.core.utils.Timer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This query engine is deprecated - the non-Boolean MTCQ engine handles the Boolean case as well, as it is more
 * efficient.
 */
@Deprecated
public class BooleanMTCQEngine extends AbstractBooleanQueryEngine<MetricTemporalConjunctiveQuery>
        implements QueryExec<MetricTemporalConjunctiveQuery>
{
    public static final Logger _logger = Log.getLogger(BooleanMTCQEngine.class);

    private final QueryExec<BCQQuery> _bcqQueryEngine = new BCQQueryEngineSimple();

    @Override
    protected boolean execBooleanABoxQuery(MetricTemporalConjunctiveQuery q)
    {
        _logger.fine("Starting entailment check for MTCQ " + q);
        String negMtcqProp = q.toNegatedPropositionalAbstractionString();
        _logger.finer("Checking DFA satisfiability for negated and propositionally abstracted MTCQ " + negMtcqProp);
        DFA automaton = MLTL2DFA.convert(negMtcqProp);
        boolean dfaSatisfiable = _checkDFASatisfiability(automaton, q);
        _logger.finer("DFA check returned " + (dfaSatisfiable ? "satisfiable" : "unsatisfiable") +
                ", therefore MTCQ is " + (dfaSatisfiable ? "not entailed" : "entailed"));
        return !dfaSatisfiable;
    }

    private boolean _checkDFASatisfiability(DFA dfa, MetricTemporalConjunctiveQuery mtcq)
    {
        Timer bcqTimer = new Timer();
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
            Iterator<KnowledgeBase> kbIterator = mtcq.getTemporalKB().iterator();
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
                        List<BCQQuery> bcqs = edges.get(0).getBCQs();
                        int toState = edges.get(0).getToState();
                        if (bcqs.size() == 1 && bcqs.get(0).isEmpty())
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
                            // check if we need to a BCQ check at all for this edge
                            else
                            {
                                newStates.add(toState);
                                continue;
                            }
                        }
                    }
                    // if we are not in a sink and have some BCQ to check, we find the successor states
                    for (Edge edge : edges)
                    {
                        _logger.finer("\t\tChecking edge " + edge + " to state " + edge.getToState());
                        if (!newStates.contains(edge.getToState()))
                        {
                            boolean edgeSat = false;
                            for (BCQQuery bcq : edge.getBCQs())
                            {
                                _logger.finer("\t\t\tChecking BCQ " + bcq);
                                if (!bcq.isEmpty())
                                {
                                    bcq.setKB(letter);
                                    bcqTimer.start();
                                    boolean querySat = !_bcqQueryEngine.exec(bcq).isEmpty();
                                    bcqTimer.stop();
                                    if (querySat)
                                    {
                                        newStates.add(edge.getToState());
                                        edgeSat = true;
                                        _logger.finer("\t\t\tBCQ is satisfiable!");
                                        break;
                                    }
                                    else
                                        _logger.finer("\t\t\tBCQ unsatisfiable!");
                                }
                                else
                                {
                                    newStates.add(edge.getToState());
                                    edgeSat = true;
                                    _logger.finer("\t\t\tBCQ empty, therefore trivially satisfied");
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
            _logger.fine("Checked a total of " + bcqTimer.getCount() + " BCQ queries, which took " +
                    bcqTimer.getTotal() + " ms");
            return isAccepting;
        }
        else
        {
            // No initial state -> L(A) is empty
            return false;
        }
    }

    @Override
    public QueryResult exec(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        return exec(q);
    }
}
