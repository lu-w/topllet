package openllet.mtcq.engine.rewriting;

import openllet.mtcq.model.query.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Note: assumes query to only consist of AndFormula, OrFormula, and NotFormula, while being in Negation Normal Form.
 * This is checked on traversal and a RuntimeException is thrown if this is not the case.
 */
public class CNFTransformer implements MTCQVisitor
{
    private MTCQFormula _newFormula;

    public static MetricTemporalConjunctiveQuery transform(MetricTemporalConjunctiveQuery formula)
    {
        return new CNFTransformer().run(formula);
    }

    public static List<MetricTemporalConjunctiveQuery> transformToListOfConjuncts(MetricTemporalConjunctiveQuery formula)
    {
        MetricTemporalConjunctiveQuery cur = transform(formula);
        List<MetricTemporalConjunctiveQuery> conjuncts = new ArrayList<>();
        if (cur instanceof AndFormula curAnd)
        {
            while (curAnd != null)
            {
                if (curAnd.getLeftSubFormula() instanceof OrFormula or &&
                        curAnd.getRightSubFormula() instanceof AndFormula and)
                {
                    conjuncts.add(or);
                    curAnd = and;
                }
                if (curAnd.getRightSubFormula() instanceof OrFormula or &&
                        curAnd.getLeftSubFormula() instanceof AndFormula and)
                {
                    conjuncts.add(or);
                    curAnd = and;
                }
                else if (curAnd.getLeftSubFormula() instanceof OrFormula or1 &&
                        curAnd.getRightSubFormula() instanceof OrFormula or2)
                {
                    conjuncts.add(or1);
                    conjuncts.add(or2);
                    curAnd = null;
                }
                else
                    throw new RuntimeException("Transformer encountered an unexpected formula in CNF: " + curAnd);
            }
        }
        else if (cur instanceof OrFormula || cur instanceof NullaryMTCQFormula || cur instanceof NotFormula ||
                cur instanceof ConjunctiveQueryFormula)
            conjuncts.add(cur);
        else
            throw new RuntimeException("Transformer encountered an unexpected formula in CNF: " + cur);
        return conjuncts;
    }

    protected MetricTemporalConjunctiveQuery run(MetricTemporalConjunctiveQuery formula)
    {
        formula.accept(this);
        return this.getTransformedFormula();
    }

    protected MTCQFormula getTransformedFormula()
    {
        return _newFormula;
    }

    @Override
    public void visit(OrFormula formula)
    {
        if (formula.getLeftSubFormula() instanceof AndFormula and)
        {
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            and.getLeftSubFormula(),
                            formula.getRightSubFormula()
                    )),
                    run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            and.getRightSubFormula(),
                            formula.getRightSubFormula()
                    ))
            );
        }
        else if (formula.getRightSubFormula() instanceof AndFormula and)
        {
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            and.getLeftSubFormula(),
                            formula.getLeftSubFormula()
                    )),
                    run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            and.getRightSubFormula(),
                            formula.getLeftSubFormula()
                    ))
            );
        }
        else
        {
            _newFormula = formula.copy();
        }
    }

    @Override
    public void visit(AndFormula formula)
    {
        _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()),
                run(formula.getRightSubFormula())
        );
    }

    @Override
    public void visit(NotFormula formula)
    {
        if (!(formula.getSubFormula() instanceof NullaryMTCQFormula ||
                formula.getSubFormula() instanceof ConjunctiveQueryFormula))
            throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
        else
            _newFormula = formula.copy();
    }

    @Override
    public void visit(LogicalTrueFormula formula)
    {
        _newFormula = formula.copy();
    }

    @Override
    public void visit(LogicalFalseFormula formula)
    {
        _newFormula = formula.copy();
    }

    @Override
    public void visit(PropositionalTrueFormula formula)
    {
        _newFormula = formula.copy();
    }

    @Override
    public void visit(PropositionalFalseFormula formula)
    {
        _newFormula = formula.copy();
    }

    @Override
    public void visit(LastFormula formula)
    {
        _newFormula = formula.copy();
    }

    @Override
    public void visit(EndFormula formula)
    {
        _newFormula = formula.copy();
    }

    @Override
    public void visit(EmptyFormula formula)
    {
        _newFormula = formula.copy();
    }

    @Override
    public void visit(ConjunctiveQueryFormula formula)
    {
        _newFormula = formula.copy();
    }

    @Override
    public void visit(XorFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }

    @Override
    public void visit(WeakNextFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }

    @Override
    public void visit(UntilFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }

    @Override
    public void visit(StrongNextFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }

    @Override
    public void visit(ReleaseFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }

    @Override
    public void visit(ImplFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }

    @Override
    public void visit(GloballyFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }

    @Override
    public void visit(EventuallyFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }

    @Override
    public void visit(EquivFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }

    @Override
    public void visit(BoundedUntilFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }

    @Override
    public void visit(BoundedReleaseFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }

    @Override
    public void visit(BoundedGloballyFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }

    @Override
    public void visit(BoundedEventuallyFormula formula)
    {
        throw new RuntimeException("Can not convert to CNF due to violation of normal form assumption.");
    }
}
