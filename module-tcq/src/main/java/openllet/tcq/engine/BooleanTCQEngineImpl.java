package openllet.tcq.engine;

import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.engine.cncq.CNCQQueryEngineSimple;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.shared.tools.Log;
import openllet.tcq.engine.automaton.MLTL2DFA;
import openllet.tcq.model.automaton.DFA;
import openllet.tcq.model.automaton.Edge;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.core.utils.Timer;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class BooleanTCQEngineImpl implements BooleanTCQEngine
{
    public static final Logger _logger = Log.getLogger(BooleanTCQEngineImpl.class);

    QueryExec<CNCQQuery> queryEngine = new CNCQQueryEngineSimple();

    @Override
    public boolean supports(TemporalConjunctiveQuery tcq) {
        for (ConjunctiveQuery cq : tcq.getConjunctiveQueries())
            if (cq.hasCycle())
                return false;
        return true;
    }

    @Override
    public boolean exec(TemporalConjunctiveQuery tcq)
    {
        assert(supports(tcq));

        _logger.info("Starting entailment check for TCQ " + tcq);

        String negTcqProp = tcq.getNegatedPropositionalAbstractionTCQ();
        _logger.info("Checking DFA satisfiability for negated and propositionally abstracted TCQ " + negTcqProp);

        try
        {
            DFA automaton = MLTL2DFA.convert(negTcqProp);
            Timer timer = new Timer();
            timer.start();
            boolean dfaSatisfiable = _checkDFASatisfiability(automaton, tcq);
            timer.stop();
            _logger.info("DFA check returned " + (dfaSatisfiable ? "satisfiable" : "unsatisfiable") +
                    ", therefore TCQ is " + (dfaSatisfiable ? "not entailed" : "entailed"));
            _logger.info("DFA satisfiability check took " + timer.getTotal() + " ms");
            return !dfaSatisfiable;
        }
        catch (IOException | InterruptedException e)
        {
            System.out.println("TCQ " + tcq + " can not be checked, error: " + e);
            return false;
        }
    }

    private boolean _checkDFASatisfiability(DFA dfa, TemporalConjunctiveQuery tcq)
    {
        Timer cncqTimer = new Timer();
        Integer initState = dfa.getInitialState();
        Set<Integer> states = new HashSet<>();
        if (initState != null)
            states.add(initState);
        _logger.info("Starting in states " + states);
        int numLetter = 0;
        if (states.size() > 0)
        {
            boolean trappedInAcceptingSink = false;
            while (tcq.getKB().hasNext() && !trappedInAcceptingSink)
            {
                KnowledgeBase letter =  tcq.getKB().next();
                _logger.info("Checking ABox #" + numLetter + " for states " + states);
                Set<Integer> newStates = new HashSet<>();
                for (int state : states)
                {
                    // TODO we can abort overall if there is some state in states that is a sink AND accepting
                    _logger.info("\tExamining state " + state);
                    List<Edge> edges = dfa.getEdges(state);
                    // check if we are in a sink (i.e. state - X -> state) then early abort
                    if (edges.size() == 1)
                    {
                        List<CNCQQuery> cncqs = edges.get(0).getCNCQs(tcq.getPropositionalAbstraction());
                        if (cncqs.size() == 1 && cncqs.get(0).isEmpty())
                        {
                            _logger.info("\t\tSink detected at state " + state);
                            int toState = edges.get(0).getToState();
                            if (dfa.isAccepting(toState))
                            {
                                // we are "trapped" in an accepting sink - early escape from looping over ABoxes
                                trappedInAcceptingSink = true;
                                _logger.info("Early abort of DFA iteration - trapped in accepting sink " + toState);
                                break;
                            }
                            else
                                // continue with next state - no need to add the sink to the newStates list, no counter
                                // example can ever be generated from it
                                continue;
                        }
                    }
                    // if we are not in a sink, we find the successor state
                    for (Edge edge : edges)
                    {
                        // TODO find good order in which new states are examined -
                        //  trivial ones first (ie. those that are sinks)
                        _logger.info("\t\tChecking edge " + edge + " to state " + edge.getToState());
                        if (!newStates.contains(edge.getToState()))
                        {
                            boolean edgeSat = false;
                            for (CNCQQuery cncq : edge.getCNCQs(tcq.getPropositionalAbstraction()))
                            {
                                _logger.info("\t\t\tChecking CNCQ " + cncq);
                                if (!cncq.isEmpty())
                                {
                                    cncq.setKB(letter);
                                    cncqTimer.start();
                                    boolean querySat = !queryEngine.exec(cncq).isEmpty();
                                    cncqTimer.stop();
                                    if (querySat)
                                    {
                                        newStates.add(edge.getToState());
                                        edgeSat = true;
                                        _logger.info("\t\t\tCNCQ is satisfied!");
                                        break;
                                    }
                                    else
                                        _logger.info("\t\t\tCNCQ unsatisfied!");
                                }
                                else
                                {
                                    newStates.add(edge.getToState());
                                    edgeSat = true;
                                    _logger.info("\t\t\tCNCQ empty, therefore trivially satisfied");
                                    break;
                                }
                            }
                            if (edgeSat)
                                _logger.info("\t\tEdge satisfied, added " + edge.getToState() + " to new states");
                        }
                        else
                            _logger.info("\t\t\tEdge can be ignored since successor state " + edge.getToState() +
                                    " is already in new states");
                    }
                }
                states = new HashSet<>(newStates);
                numLetter++;
            }
            // it suffices if one of the reached states is accepting
            boolean isAccepting = false;
            for (int state : states)
                isAccepting |= dfa.isAccepting(state);
            _logger.info("ABoxes completely iterated, end state are " + states + " which are " +
                    (isAccepting ? "accepting" : "not accepting"));
            _logger.info("Checked a total of " + cncqTimer.getCount() + " CNCQ queries, which took " +
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
