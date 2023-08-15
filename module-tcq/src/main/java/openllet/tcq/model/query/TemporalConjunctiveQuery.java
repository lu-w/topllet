package openllet.tcq.model.query;

import openllet.query.sparqldl.model.CompositeQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.tcq.model.kb.TemporalKnowledgeBase;

import java.util.Collection;
import java.util.Map;

public interface TemporalConjunctiveQuery extends CompositeQuery<ConjunctiveQuery, TemporalConjunctiveQuery>
{
    Collection<Proposition> getPropositionsInAbstraction();

    Map<Proposition, ConjunctiveQuery> getPropositionalAbstraction();

    Collection<ConjunctiveQuery> getConjunctiveQueries();

    void addConjunctiveQuery(Proposition proposition, ConjunctiveQuery query);

    void addConjunctiveQuery(Proposition proposition, ConjunctiveQuery query, String queryString);

    String toPropositionalAbstractionString();

    String toNegatedPropositionalAbstractionString();

    TemporalKnowledgeBase getTemporalKB();

    void setTemporalKB(TemporalKnowledgeBase temporalKb);
}
