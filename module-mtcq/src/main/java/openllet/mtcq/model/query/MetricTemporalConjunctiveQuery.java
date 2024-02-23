package openllet.mtcq.model.query;

import openllet.query.sparqldl.model.CompositeQuery;
import openllet.query.sparqldl.model.Query;
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
     * @return An unmodifiable view on the map representing the propositional abstraction used in this query.
     */
    Map<Proposition, ConjunctiveQuery> getPropositionalAbstraction();

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

    void setParentFormula(MetricTemporalConjunctiveQuery parentFormula);

    MetricTemporalConjunctiveQuery getParentFormula();

    /**
     * @return True iff. this formula is temporal, i.e., it does not contain a temporal operator also in its subformulae
     */
    boolean isTemporal();

    void accept(MTCQVisitor visitor);

    String toString(PropositionFactory propositions);
}
