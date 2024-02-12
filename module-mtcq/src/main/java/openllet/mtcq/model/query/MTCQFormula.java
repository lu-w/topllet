package openllet.mtcq.model.query;

import openllet.core.KnowledgeBase;
import openllet.mtcq.model.kb.FileBasedTemporalKnowledgeBaseImpl;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.AbstractCompositeQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.shared.tools.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MTCQFormula extends AbstractCompositeQuery<ConjunctiveQuery, MetricTemporalConjunctiveQuery>
        implements MetricTemporalConjunctiveQuery
{
    public static final Logger _logger = Log.getLogger(MTCQFormula.class);

    private final TemporalKnowledgeBase _temporalKb;

    public MTCQFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb.get(0), isDistinct);
        _temporalKb = temporalKb;
    }

    @Override
    public Map<Proposition, ConjunctiveQuery> getPropositionalAbstraction()
    {
        // TODO
        return null;
    }

    @Override
    public String toPropositionalAbstractionString()
    {
        return toString(getPropositionalAbstraction());
    }

    @Override
    public String toNegatedPropositionalAbstractionString()
    {
        return "!(" + toPropositionalAbstractionString() + ")";
    }

    @Override
    public TemporalKnowledgeBase getTemporalKB()
    {
        return _temporalKb;
    }

    @Override
    public List<MetricTemporalConjunctiveQuery> split()
    {
        _logger.warning("Splitting not yet implemented on MTCQs");
        return null;
    }

    @Override
    public MTCQFormula createQuery(KnowledgeBase kb, boolean isDistinct)
    {
         _logger.warning("Using createQuery() on an MTCQ - this method shall not be used");
         return new MTCQFormula(new FileBasedTemporalKnowledgeBaseImpl(new ArrayList<>()), isDistinct);
    }

    @Override
    protected String getCompositeDelimiter()
    {
        return ",";
    }

    @Override
    public String toString()
    {
        return toString(new HashMap<>());
    }

    private String toString(Map<Proposition, ConjunctiveQuery> replaceCQsWithPropositions)
    {
        // TODO
        return "";
    }
}
