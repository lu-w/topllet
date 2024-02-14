package openllet.mtcq.engine;

import openllet.mtcq.engine.automaton.MLTL2DFA;
import openllet.mtcq.model.automaton.DFA;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.mtcq.parser.ParseException;
import openllet.query.sparqldl.engine.AbstractQueryEngine;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.QueryResultImpl;

public class MTCQEngineOptimized extends AbstractQueryEngine<MetricTemporalConjunctiveQuery> implements QueryExec<MetricTemporalConjunctiveQuery>
{
    @Override
    protected QueryResult execABoxQuery(MetricTemporalConjunctiveQuery q, QueryResult excludeBindings, QueryResult restrictToBindings)
    {
        System.out.println("========== CHECKING " + q + " ========================");
        // 1) construct FA from q
        System.out.println("Prop abs = " + q.toPropositionalAbstractionString());
        DFA automaton = MLTL2DFA.convert(q.toPropositionalAbstractionString(), q);
        FAStates states = new FAStates();
        Integer initState = automaton.getInitialState();
        if (initState != null && !q.getTemporalKB().isEmpty())
        {
            FAState initialState = new FAState(q, automaton, initState, 0);
            states.add(initialState);
        }
        // 2) for each edge in current state, compute entailed bindings by UBCQEngine, do so only over the entailed candidates of current state
        while(states.hasExecutableState())
        {
            FAState execState = states.get(0);
            System.out.println("Executing state " + execState);
            for (FAState newState : execState.execute())
            {
                states.mergeOrAdd(newState);
            }
            states.remove(execState);
            // consider for future optimization (not now):
            // - propagate bindings that are certainly trapped in an accepting state to all other exec. states
            // - do not inspect edges that lead into non-accepting sinks
            // - propagate sure (non-)acceptance of bindings to other states (this collides with before - chose what is more efficient?)
        }

        // 3) return exactly those bindings for which some final state was reached
        return fetchEntailedAnswers(q, states);
    }

    private QueryResult fetchEntailedAnswers(MetricTemporalConjunctiveQuery q, FAStates states)
    {
        QueryResult res = new QueryResultImpl(q);
        for (FAState state : states)
            if (state.isAccepting())
                res.addAll(state.getEntailedAnswers());
        return res;
    }
}
