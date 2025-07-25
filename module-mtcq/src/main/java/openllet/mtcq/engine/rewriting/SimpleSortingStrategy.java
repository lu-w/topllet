package openllet.mtcq.engine.rewriting;

import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple sorting strategy: Puts non-temporal CQ-answerable conjuncts to the front, followed by non-temporal
 * non-CQ-answerable conjuncts, followed by temporal conjuncts.
 */
public class SimpleSortingStrategy extends SortingStrategy {
    @Override
    public List<MetricTemporalConjunctiveQuery> sort(List<MetricTemporalConjunctiveQuery> conjuncts) {
        /* Possible improvements:
         * Atemporal parts with disjunctions that are supersets of other disjunctions are put to the front.
         * CQ-answerable shall put pure CQs to the front with higher priority than UCQs containing a CQ.
         * Guessing of answer set sizes.
         */
        List<MetricTemporalConjunctiveQuery> sorted = new ArrayList<>();
        for (MetricTemporalConjunctiveQuery conjunct : conjuncts)
            if (conjunct.isTemporal())
                sorted.add(conjunct);
            else if (answerableByCQ(conjunct))
                sorted.add(0, conjunct);
            else if (!sorted.isEmpty())
                if (sorted.get(0).isTemporal())
                    sorted.add(0, conjunct);
                else
                    sorted.add(1, conjunct);
            else
                sorted.add(0, conjunct);
        return sorted;
    }
}
