package openllet.mtcq.model.query;

import openllet.query.sparqldl.model.CompositeQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;

import java.util.Collection;
import java.util.Map;

/**
 * Interface for accessing temporal conjunctive queries, which are represented as a composite query consisting of
 * conjunctive queries.
 * Note that this interface does not enable access to the parse tree of the temporal formula, as only the CQs, the
 * propositionally abstracted MTCQ (as a string), and the original formula (as a string) are accessible.
 */
public interface MetricTemporalConjunctiveQuery extends CompositeQuery<ConjunctiveQuery, MetricTemporalConjunctiveQuery>
{
    /**
     * @return A copy of a collection of the propositions used in the propositional abstraction.
     */
    Collection<Proposition> getPropositionsInAbstraction();

    /**
     * @return An unmodifiable view on the map representing the propositional abstraction used in this query.
     */
    Map<Proposition, ConjunctiveQuery> getPropositionalAbstraction();

    /**
     * @return A copy of a collection of the conjunctive queries used in this query.
     */
    Collection<ConjunctiveQuery> getConjunctiveQueries();

    /**
     * Can be used to build the propositional abstraction. Replaces every occurence of the query with the given
     * proposition and adds the query and proposition to the propositions and CQs stored in this query.
     * @param proposition The proposition for the CQ.
     * @param query The query to replace in the MTCQ.
     * @param queryString The string representation of the query that is given.
     */
    void addConjunctiveQuery(Proposition proposition, ConjunctiveQuery query, String queryString);

    /**
     * @return The string representation of the propositional abstraction of this MTCQ.
     */
    String toPropositionalAbstractionString();

    /**
     * @return The string representation of the negated propositional abstraction of this MTCQ.
     */
    String toNegatedPropositionalAbstractionString();

    /**
     * @return The temporal knowledge base this query operates on.
     */
    TemporalKnowledgeBase getTemporalKB();
}
