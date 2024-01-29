package openllet.mtcq.engine;

import openllet.core.KnowledgeBase;
import openllet.mtcq.engine.ubcq.UBCQQueryEngineByMTCQ;
import openllet.mtcq.model.automaton.DFA;
import openllet.mtcq.model.automaton.Edge;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.ubcq.UBCQQuery;

import java.io.IOException;
import java.util.List;

public class FAState
{
    private final MetricTemporalConjunctiveQuery _mtcq;
    private final DFA _dfa;
    private final int _faInternalState;
    private final int _timePoint;
    private final KnowledgeBase _kb;
    private QueryResult _entailedAnswers;

    public FAState(MetricTemporalConjunctiveQuery mtcq, DFA dfa, int faInternalState, int timePoint)
    {
        _mtcq = mtcq;
        _dfa = dfa;
        _faInternalState = faInternalState;
        _timePoint = timePoint;
        if (timePoint < mtcq.getTemporalKB().size())
            _kb = mtcq.getTemporalKB().get(timePoint);
        else
            _kb = null;
        _entailedAnswers = null; // null = no knowledge, everything could be entailed (has not yet been checked)
    }

    public int getInternalFAState()
    {
        return _faInternalState;
    }

    public int getTimePoint()
    {
        return _timePoint;
    }

    public boolean isAccepting()
    {
        return _dfa.isAccepting(_faInternalState);
    }

    protected void setEntailedAnswers(QueryResult entailedAnswers)
    {
        _entailedAnswers = entailedAnswers;
    }

    public QueryResult getEntailedAnswers()
    {
        if (_entailedAnswers != null)
            return _entailedAnswers;
        else
            throw new UnsupportedOperationException("Trying to get entailed answers before actually computing those " +
                    "for state " + this);
    }

    public void merge(FAState other)
    {
        if (_timePoint != other.getTimePoint() || _faInternalState != other.getInternalFAState())
            throw new RuntimeException("Trying to merge invalid states");
        if (_entailedAnswers != null)
            _entailedAnswers.addAll(other.getEntailedAnswers());
        else
            _entailedAnswers = other.getEntailedAnswers().copy(); // TODO copy() required?
    }

    public boolean canExecute()
    {
        return _timePoint < _mtcq.getTemporalKB().size() && _kb != null;
    }

    public FAStates execute()
    {
        FAStates newStates = new FAStates();
        if (isInSink())
        {
            FAState newState = new FAState(_mtcq, _dfa, _faInternalState, _timePoint + 1);
            newState.setEntailedAnswers(_entailedAnswers);
            newStates.add(newState);
        }
        // Check if we can execute this state, then check all edges and create appropriate successor states.
        else if (canExecute())
        {
            List<Edge> edges = _dfa.getEdges(_faInternalState);
            // TODO why are excludeBindings, includeBindings not part of QueryExec<>?
            UBCQQueryEngineByMTCQ _ubcqEngine = new UBCQQueryEngineByMTCQ(); // TODO replace once we have a more efficient one...
            for (Edge edge : edges)
            {
                System.out.println("Checking edge " + edge);
                UBCQQuery ubcq = edge.getUBCQ(_kb, _mtcq.isDistinct());
                try
                {
                    // TODO: excludeBindings are those for which we already certainly know that they are entailed (due to accepting sinks)
                    // incorporates prior knowledge on entailed answers (only those need to be checked)
                    QueryResult edgeResult = _ubcqEngine.exec(ubcq, null, _entailedAnswers);
                    edgeResult.expandToAllVariables(_mtcq.getResultVars());
                    System.out.println("Expanded (to " + _mtcq.getResultVars() + ") edge result = " + edgeResult);
                    // TODO this should obviously be avoided - only create those answers in restrictToBindings...
                    if (_entailedAnswers != null)
                        edgeResult.retainAll(_entailedAnswers);
                    System.out.println("Retained (to " + _entailedAnswers + ") edge result = " + edgeResult);
                    if (!edgeResult.isEmpty())
                    {
                        FAState newState = new FAState(_mtcq, _dfa, edge.getToState(), _timePoint + 1);
                        newState.setEntailedAnswers(edgeResult);
                        newStates.add(newState);
                        System.out.println("New state generated: " + newState);
                    }
                }
                catch (IOException | InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        return newStates;
    }

    /**
     * @return True iff. the current DFA state is a sink
     */
    private boolean isInSink()
    {
        List<Edge> edges = _dfa.getEdges(_faInternalState);
        return edges.size() == 1 && edges.get(0).getFromState() == edges.get(0).getToState();
    }

    @Override
    public String toString()
    {
        String res = "State " + _faInternalState + " @ t=" + _timePoint;
        if (_entailedAnswers != null)
            res += " - Entailed: " + _entailedAnswers;
        if (isAccepting())
            res += " (accepting)";
        return res;
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (_entailedAnswers == null ? 0 : _entailedAnswers.hashCode());
        result = PRIME * result + _faInternalState;
        result = PRIME * result + _timePoint;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final FAState other = (FAState) obj;
        if (_entailedAnswers == null && other._entailedAnswers != null)
            return false;
        else
            return other._faInternalState == _faInternalState && other._timePoint == _timePoint &&
                    _entailedAnswers.equals(other._entailedAnswers);
    }
}
