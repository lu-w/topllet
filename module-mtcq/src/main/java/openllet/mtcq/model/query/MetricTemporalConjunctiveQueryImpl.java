package openllet.mtcq.model.query;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.AbstractCompositeQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.shared.tools.Log;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.mtcq.model.kb.FileBasedTemporalKnowledgeBaseImpl;

import java.util.*;
import java.util.logging.Logger;

/**
 * Standard implementation of a temporal conjunctive query.
 * Note that some functionality of the Query interface is not accessible due to operating partially on unparsed strings.
 */
public class MetricTemporalConjunctiveQueryImpl extends AbstractCompositeQuery<ConjunctiveQuery, MetricTemporalConjunctiveQuery>
        implements MetricTemporalConjunctiveQuery
{
    public static final Logger _logger = Log.getLogger(MetricTemporalConjunctiveQueryImpl.class);

    private final TemporalKnowledgeBase _temporalKb;
    private final Map<Proposition, ConjunctiveQuery> _propAbs = new TreeMap<>();
    private String _propAbsMtcq;
    private final String _mtcq;

    public MetricTemporalConjunctiveQueryImpl(String mtcq, TemporalKnowledgeBase temporalKb, boolean distinct)
    {
        super(temporalKb.get(0), distinct);
        _temporalKb = temporalKb;
        _mtcq = mtcq;
        _propAbsMtcq = mtcq;
    }

    @Override
    public Collection<Proposition> getPropositionsInAbstraction()
    {
        return _propAbs.keySet();
    }

    @Override
    public Collection<ConjunctiveQuery> getConjunctiveQueries()
    {
        return _propAbs.values();
    }

    @Override
    public void addConjunctiveQuery(Proposition proposition, ConjunctiveQuery query, String queryString)
    {
        super.addQuery(query);
        _propAbs.put(proposition, query);
        if (queryString != null)
            _propAbsMtcq = _propAbsMtcq.replace("(" + queryString + ")", "(" + proposition.toString() + ")");
    }

    @Override
    public Map<Proposition, ConjunctiveQuery> getPropositionalAbstraction()
    {
        return Collections.unmodifiableMap(_propAbs);
    }

    @Override
    public TemporalKnowledgeBase getTemporalKB()
    {
        return _temporalKb;
    }

    @Override
    protected String getCompositeDelimiter()
    {
        return " [...] ";
    }

    @Override
    public String toString()
    {
        List<String> resultVarStrings = new ArrayList<>();
        for (ATermAppl var : _resultVars)
            if (var.getArguments().getLength() > 0)
                resultVarStrings.add(var.getArgument(0).toString());
        return "query(" + String.join(", ", resultVarStrings) + ") :- " + _mtcq;
    }

    @Override
    public String toPropositionalAbstractionString()
    {
        return _propAbsMtcq;
    }

    @Override
    public String toNegatedPropositionalAbstractionString()
    {
        return "!(" + toPropositionalAbstractionString() + ")";
    }

    @Override
    public List<MetricTemporalConjunctiveQuery> split()
    {
        _logger.fine("Tried to split a temporal conjunctive query, but temporal conjunctive queries shall not be split.");
        return List.of(this);
    }

    @Override
    public MetricTemporalConjunctiveQuery createQuery(KnowledgeBase kb, boolean isDistinct)
    {
        _logger.warning("Using createQuery() on a temporal conjunctive query - this method shall not be used");
        return new MetricTemporalConjunctiveQueryImpl("", new FileBasedTemporalKnowledgeBaseImpl(new ArrayList<>()), isDistinct);
    }

    @Override
    public MetricTemporalConjunctiveQuery copy()
    {
        MetricTemporalConjunctiveQuery copy = new MetricTemporalConjunctiveQueryImpl(_mtcq, _temporalKb, _distinct);
        for (ConjunctiveQuery q : getConjunctiveQueries())
            copy.addQuery(q.copy());
        copy.setDistVars(new EnumMap<>(getDistVarsWithVarType()));
        copy.setResultVars(new ArrayList<>(getResultVars()));
        return copy;
    }
}
