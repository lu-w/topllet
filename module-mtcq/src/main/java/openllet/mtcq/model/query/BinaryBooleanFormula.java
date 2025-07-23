package openllet.mtcq.model.query;

import openllet.core.utils.Pair;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;

public abstract class BinaryBooleanFormula extends BinaryMTCQFormula
{
    public BinaryBooleanFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct, MetricTemporalConjunctiveQuery left,
                                MetricTemporalConjunctiveQuery right)
    {
        super(temporalKb, isDistinct, left, right);
    }

    public BinaryBooleanFormula(MetricTemporalConjunctiveQuery parentFormula, MetricTemporalConjunctiveQuery left, MetricTemporalConjunctiveQuery right)
    {
        super(parentFormula, left, right);
    }

    /**
     * Splits this formula into a non-temporal and temporal part, if this is possbile. If it is not possible, throws
     * a {@code IllegalArgumentException}.
     * @return A pair consisting of the atemporal part as the first and temporal part as the second element.
     */
    public Pair<MetricTemporalConjunctiveQuery, MetricTemporalConjunctiveQuery> splitIntoTemporalAndNonTemporalPart()
    {
        MetricTemporalConjunctiveQuery atemporalPart;
        MetricTemporalConjunctiveQuery temporalPart;
        if (!getLeftSubFormula().isTemporal())
        {
            atemporalPart = getLeftSubFormula();
            temporalPart = getRightSubFormula();
        }
        else if (!getRightSubFormula().isTemporal())
        {
            temporalPart = getLeftSubFormula();
            atemporalPart = getRightSubFormula();
        }
        else
            throw new IllegalArgumentException("Cannot split " + this + " into non-temporal and temporal part because " +
                    "there is no such division.");
        return new Pair<>(atemporalPart, temporalPart);
    }

    @Override
    public boolean isTemporal()
    {
        return getLeftSubFormula().isTemporal() || getRightSubFormula().isTemporal();
    }
}
