package openllet.mtcq.engine.engine_rewriting;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Timer;
import openllet.mtcq.model.kb.StreamingDataHandler;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.mtcq.ui.MTCQEngineUI;
import openllet.query.sparqldl.model.results.QueryResult;

import javax.annotation.Nullable;

/**
 * Stores the state information for a given iteration of the {@code MTCQNormalFormEngine}.
 * Specifically contains a {@code KnowledgeBase} that has been loaded as part of a {@code TemporalKnowledgeBase}.
 * Is also able to handle streaming and UI information.
 */
public class TemporalIterationState {
    private KnowledgeBase _kb = null;
    private int _t = 0;
    private int _maxTime = 0;
    private MTCQEngineUI _ui = null;
    private StreamingDataHandler _streamer = null;
    private Timer _timer;
    private MetricTemporalConjunctiveQuery _query;

    /**
     * Handles initialization of the *first* iteration, including loading of the first knowledge base of a temporal
     * knowledge base. Use {@link #loadNextIteration loadNextIteration} for preparing subsequent iterations.
     * @param query The query containing the temporal knowledge base to initialize the first data for.
     */
    public TemporalIterationState(MetricTemporalConjunctiveQuery query)
    {
        this(query, false, 0,null);
    }

    /**
     * Handles initialization of the *first* iteration, including loading of the first knowledge base of a temporal
     * knowledge base. Considers whether we are in streaming mode or not.
     * The maximum time point is {@code Integer.MAX_VALUE} if in streaming mode.
     * Use {@link #loadNextIteration loadNextIteration} for preparing subsequent iterations.
     * @param query The query containing the temporal knowledge base to initialize the first data for.
     * @param streaming Whether to load the knowledge base from a streaming 0MQ source.
     * @param port The port to use for 0MQ if in streaming mode.
     */
    public TemporalIterationState(MetricTemporalConjunctiveQuery query, boolean streaming, int port)
    {
        this(query, streaming, port, null);
    }

    /**
     * Handles initialization of the *first* iteration, including loading of the first knowledge base of a temporal
     * knowledge base. Considers whether we are in streaming mode or not. Stops and re-starts the timer during loading.
     * The maximum time point is {@code Integer.MAX_VALUE} if in streaming mode.
     * Use {@link #loadNextIteration loadNextIteration} for preparing subsequent iterations.
     * @param query The query containing the temporal knowledge base to initialize the first data for.
     * @param streaming Whether to load the knowledge base from a streaming 0MQ source.
     * @param port The port to use for 0MQ if in streaming mode.
     * @param timer The timer to disable during loading.
     */
    public TemporalIterationState(MetricTemporalConjunctiveQuery query, boolean streaming, int port, Timer timer)
    {
        _timer = timer;
        if (_timer != null) _timer.stop();
        _query = query;
        if (streaming)
        {
            _kb = query.getTemporalKB().get(0);
            _streamer = new StreamingDataHandler(_kb, port, query.getTemporalKB().getTimer());
            _maxTime = Integer.MAX_VALUE;
        }
        else
        {
            _kb = query.getTemporalKB().get(0);
            _maxTime =  query.getTemporalKB().size() - 1;
        }
        if (_timer != null) _timer.start();
    }

    /**
     * Handles initialization of a knowledge base of a temporal knowledge base if all its parameters are already known.
     * @param query The MTCQ that is overall to be checked.
     * @param kb The knowledge base of this iteration.
     * @param t The time point of this iteration.
     * @param maxTime The maximum number of time steps.
     * @param ui The UI to send data to, if required.
     * @param streamer The streaming service to send/receive data to/from, if required.
     * @param timer The timer to exclude loading times for, if required.
     */
    private TemporalIterationState(MetricTemporalConjunctiveQuery query, KnowledgeBase kb, int t, int maxTime,
                                   @Nullable MTCQEngineUI ui, @Nullable StreamingDataHandler streamer,
                                   @Nullable Timer timer)
    {
        _kb = kb;
        _t = t;
        _maxTime = maxTime;
        _ui = ui;
        _streamer = streamer;
        _timer = timer;
        _query = query;
    }

    public int getTimePoint() {
        return _t;
    }

    public int getMaxTime() {
        return _maxTime;
    }

    public KnowledgeBase getKB() {
        return _kb;
    }

    boolean isLast()
    {
        if (_streamer != null)
            return _streamer.isLast();
        else
            return _t == _maxTime;
    }

    boolean hasNext()
    {
        if (_streamer != null)
            return _streamer.isLast();
        else
            return _t < _maxTime;
    }

    /**
     * Loads the next knowledge base (viewed from this knowledge base) either from the streamer or from its temporal
     * knowledge base. If in streaming, blocks until data is streamed fully. Updates the UI accordingly.
     * @return Record of the loaded knowledge base, whether it is the last one, and the current time point (unmodified).
     */
    public TemporalIterationState loadNextIteration()
    {
        int t = _t + 1;
        if (t > _maxTime)
            throw new IndexOutOfBoundsException("Tried to load knowledge base for time " + t +
                    " but maximum time point is " + _maxTime);
        if (_timer != null) _timer.stop();
        boolean isLast;
        KnowledgeBase kb = _kb;
        if (_ui != null) _ui.informAboutStartOfIteration(t);
        if (_streamer != null)
        {
            _streamer.waitAndUpdateKB();
            if (_streamer.isLast() && _maxTime < Integer.MAX_VALUE)
                _maxTime = t;
        }
        else
            kb = _query.getTemporalKB().get(t);
        if (_timer != null) _timer.start();
        return new TemporalIterationState(_query, kb, t, _maxTime, _ui, _streamer, _timer);
    }

    /**
     * Notifies the UI and the streamer about a time point iteration having completed.
     */
    public void notifyUIAndStreamingAboutIterationEnd()
    {
        if (_timer != null) _timer.stop();
        if (_ui != null)
        {
            // We cannot (yet) pass a result to the UI due to lazy evaluation and hence it is null.
            _ui.informAboutResults(_t, _kb, _query, null);
            _ui.informAboutEndOfIteration(_t);
        }
        if (_streamer != null && !isLast())
            _streamer.sendAck();
        if (_timer != null) _timer.start();
    }

    /**
     * Notifies the UI and the streamer about a finished computation for the MTCQ.
     * @param result The result that was computed
     */
    public void notifyUIAndStreamingAboutResult(QueryResult result)
    {
        if (_ui != null) {
            _ui.informAboutResults(_maxTime - 1, _kb, _query, result);
            _ui.clear();
        }
        if (_streamer != null)
            _streamer.sendResult(result);
    }

    public void notifyUIAboutResult(QueryResult result) {
        if (_ui != null)
            _ui.informAboutResults(_t, _kb, _query, result);
    }
}
