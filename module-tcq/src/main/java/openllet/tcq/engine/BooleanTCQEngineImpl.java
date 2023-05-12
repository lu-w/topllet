package openllet.tcq.engine;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.dot.DOTParsers;
import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.engine.cncq.CNCQQueryEngineSimple;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.tcq.engine.automaton.MLTL2DFA;
import openllet.tcq.model.kb.TemporalKnowledgeBase;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.tcq.parser.ConjunctiveQueryParser;

import java.io.File;
import java.io.IOException;

public class BooleanTCQEngineImpl implements BooleanTCQEngine
{
    QueryExec<CNCQQuery> queryEngine = new CNCQQueryEngineSimple();

    @Override
    public boolean supports(TemporalConjunctiveQuery tcq) {
        for (ConjunctiveQuery cq : tcq.getConjunctiveQueries())
            if (cq.hasCycle())
                return false;
        return true;
    }

    @Override
    public boolean exec(TemporalConjunctiveQuery tcq) {
        assert(supports(tcq));

        String negTcqProp = tcq.getNegatedPropositionalAbstractionTCQ();

        try {
            CompactDFA<String> automaton = MLTL2DFA.convert(negTcqProp);
            return _checkDFASatisfiability(automaton, tcq);
        }
        catch (IOException | InterruptedException | RuntimeException e)
        {
            System.out.println("TCQ " + tcq + " can not be converted to an automaton, error: " + e);
            return false;
        }
    }

    private boolean _checkDFASatisfiability(CompactDFA<String> dfa, TemporalConjunctiveQuery tcq)
    {
        /*
        int state = automaton.getInitialState();

        for (TemporalKnowledgeBase it = tcq.getKB(); it.hasNext(); ) {
            KnowledgeBase letter = it.next();
            // TODO fetch edges correctly
            for (int edge : automaton.getTransitions(state, ""))
            {
                // TODO we need to translate from "x 1\n1 x\n0,0" to a valid edge
                ConjunctiveQuery cncq = tcq.getPropositionalAbstraction().get(edge);
                cncq.setKB(letter)
                if (!queryEngine.exec(cncq).isEmpty())
                {
                    // TODO set successor state correctly
                    state = edge;
                    break;
                }
            }
        }
        return !automaton.isAccepting(state);
        */
        return true;
    }
}
