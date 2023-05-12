package openllet.tcq.model.query;

import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.tcq.model.kb.TemporalKnowledgeBase;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface TemporalConjunctiveQuery
{
    boolean isDistinct();

    String getPropositionalAbstractionTCQ();

    String getNegatedPropositionalAbstractionTCQ();

    Set<Proposition> getPropositionsInAbstraction();

    Collection<ConjunctiveQuery> getConjunctiveQueries();

    Map<Proposition, ConjunctiveQuery> getPropositionalAbstraction();

    @Override
    String toString();

    TemporalKnowledgeBase getKB();

    void setKB(TemporalKnowledgeBase kb);
}
