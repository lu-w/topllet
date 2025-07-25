package openllet.mtcq.engine.engine_rewriting;

import openllet.mtcq.model.query.ConjunctiveQueryFormula;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.mtcq.model.query.OrFormula;

import java.util.List;

import static openllet.mtcq.engine.rewriting.MTCQSimplifier.flattenOr;

abstract class SortingStrategy {
    /**
     * Reorders (sorts) the given conjunction s.t. it can be answered more efficiently (mainly, by guessing which
     * conjuncts have small answer sets, which are then put to the front). Additional considerations, e.g., whether
     * conjuncts can be answered at the current time point only (i.e., having no temporal operator), can be made.
     * Does not change the semantics of the conjunction and does not modify the original list.
     * @param conjuncts A conjunction of arbitrary MTCQs.
     * @return A possibly reordered, new list containing the same pointers to the MTCQs.
     */
    abstract List<MetricTemporalConjunctiveQuery> sort(List<MetricTemporalConjunctiveQuery> conjuncts);

    /**
     * Checks if the MTCQ contains some part that can be answered at the current time point by a CQ engine:
     * it must be non-temporal and a CQ or a disjunction that contains at least one CQ or a non-variable formula (such
     * as last, end).
     * Note: This is just a heuristic.
     * @param mtcq The MTCQ to check CQ answerability for.
     * @return True only if it is possible to answer the MTCQ by CQs, false otherwise.
     */
    boolean answerableByCQ(MetricTemporalConjunctiveQuery mtcq)
    {
        if (mtcq.isTemporal())
            return false;
        else if (mtcq instanceof ConjunctiveQueryFormula)
            return true;
        else if (mtcq instanceof OrFormula or)
            // Check if disjunction contains at least on CQ
            return flattenOr(or).stream()
                    .anyMatch(disjunct -> disjunct instanceof ConjunctiveQueryFormula);
        else
            // last, end, true, false, tt, ff
            return true;
    }
}
