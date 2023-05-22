package openllet.tcq.model.query;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.model.AbstractCompositeQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.shared.tools.Log;
import openllet.tcq.model.kb.TemporalKnowledgeBase;
import openllet.tcq.model.kb.TemporalKnowledgeBaseImpl;

import java.util.*;
import java.util.logging.Logger;

public class TemporalConjunctiveQueryImpl extends AbstractCompositeQuery<ConjunctiveQuery, TemporalConjunctiveQuery>
        implements TemporalConjunctiveQuery
{
    public static final Logger _logger = Log.getLogger(TemporalConjunctiveQueryImpl.class);

    private TemporalKnowledgeBase _temporalKb;
    private final Map<Proposition, ConjunctiveQuery> _propAbs = new TreeMap<>();
    private String _propAbsTcq;
    private final String _tcq;

    public TemporalConjunctiveQueryImpl(String tcq, TemporalKnowledgeBase temporalKb, boolean distinct)
    {
        super(temporalKb.first(), distinct);
        _temporalKb = temporalKb;
        _tcq = tcq;
        _propAbsTcq = tcq;
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
    public void addConjunctiveQuery(Proposition proposition, ConjunctiveQuery query)
    {
        addConjunctiveQuery(proposition, query, null);
    }

    @Override
    public void addConjunctiveQuery(Proposition proposition, ConjunctiveQuery query, String queryString)
    {
        super.addQuery(query);
        _propAbs.put(proposition, query);
        if (queryString != null)
        {
            // TODO replace only in cqString region, else this happens: "F(C(a)) & (C(a) ^ D(b))" -> F(a) & (a ^ D(b))
            _propAbsTcq = _propAbsTcq.replace(queryString, proposition.toString());
        }
    }

    @Override
    public Map<Proposition, ConjunctiveQuery> getPropositionalAbstraction()
    {
        return _propAbs;
    }

    @Override
    public TemporalKnowledgeBase getTemporalKB()
    {
        return _temporalKb;
    }

    @Override
    public void setTemporalKB(TemporalKnowledgeBase temporalKb)
    {
        _temporalKb = temporalKb;
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
        return "query(" + String.join(", ", resultVarStrings) + ") :- " + _tcq;
    }

    @Override
    public String toPropositionalAbstractionString()
    {
        return _propAbsTcq;
    }

    @Override
    public String toNegatedPropositionalAbstractionString()
    {
        return "!(" + toPropositionalAbstractionString() + ")";
    }

    @Override
    public List<TemporalConjunctiveQuery> split()
    {
        _logger.fine("Tried to split a temporal conjunctive query, but temporal conjunctive queries shall not be split.");
        return List.of(this);
    }

    @Override
    public TemporalConjunctiveQuery createQuery(KnowledgeBase kb, boolean isDistinct)
    {
        _logger.warning("Using createQuery(..) on a temporal conjunctive query - this method shall not be used");
        return new TemporalConjunctiveQueryImpl("", new TemporalKnowledgeBaseImpl(new ArrayList<>()), isDistinct);
    }

    @Override
    public TemporalConjunctiveQuery copy()
    {
        TemporalConjunctiveQuery copy = new TemporalConjunctiveQueryImpl(_tcq, _temporalKb, _distinct);
        for (ConjunctiveQuery q : getConjunctiveQueries())
            copy.addQuery(q.copy());
        copy.setDistVars(new EnumMap<>(getDistVarsWithVarType()));
        copy.setResultVars(new ArrayList<>(getResultVars()));
        return copy;
    }
}
