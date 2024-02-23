package openllet.mtcq.model.query;

import openllet.core.KnowledgeBase;
import openllet.mtcq.model.kb.FileBasedTemporalKnowledgeBaseImpl;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.AbstractCompositeQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.shared.tools.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * TODO:
 *  - implement equals() both against String and MTCQFormula
 */
public abstract class MTCQFormula extends AbstractCompositeQuery<ConjunctiveQuery, MetricTemporalConjunctiveQuery>
        implements MetricTemporalConjunctiveQuery
{
    public static final Logger _logger = Log.getLogger(MTCQFormula.class);

    private TemporalKnowledgeBase _temporalKb;
    private PropositionFactory _propositions = null;
    private MetricTemporalConjunctiveQuery _parentFormula;

    public MTCQFormula(MetricTemporalConjunctiveQuery parentFormula)
    {
        super(parentFormula.getKB(), parentFormula.isDistinct());
        _temporalKb = parentFormula.getTemporalKB();
        _parentFormula = parentFormula;
    }

    public MTCQFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct)
    {
        super(temporalKb.get(0), isDistinct);
        _temporalKb = temporalKb;
    }

    @Override
    public void setParentFormula(MetricTemporalConjunctiveQuery parentFormula)
    {
        _parentFormula = parentFormula;
    }

    @Override
    public MetricTemporalConjunctiveQuery getParentFormula()
    {
        return _parentFormula;
    }

    @Override
    public Map<Proposition, ConjunctiveQuery> getPropositionalAbstraction()
    {
        if (_propositions == null)
            toPropositionalAbstractionString();
        return _propositions.getCreatedPropositions();
    }

    @Override
    public String toPropositionalAbstractionString()
    {
        if (_propositions == null)
            _propositions = new PropositionFactory();
        return toString(_propositions);
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
    public void setTemporalKB(TemporalKnowledgeBase tkb)
    {
        _temporalKb = tkb;
    }

    @Override
    public List<MetricTemporalConjunctiveQuery> split()
    {
        _logger.warning("Splitting not yet implemented on MTCQs");
        return null;
    }

    @Override
    public MetricTemporalConjunctiveQuery createQuery(KnowledgeBase kb, boolean isDistinct)
    {
         _logger.warning("Using createQuery() on an MTCQ - this method shall not be used");
         return new LogicalTrueFormula(new FileBasedTemporalKnowledgeBaseImpl(new ArrayList<>()), isDistinct);
    }

    @Override
    protected String getCompositeDelimiter()
    {
        return ",";
    }

    @Override
    public String toString()
    {
        return toString(null);
    }
}
