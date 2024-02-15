package openllet.mtcq.engine.rewriting;

import openllet.mtcq.model.query.*;

public class DNFTransformer implements MTCQVisitor
{
    private MTCQFormula _newFormula;

    public static MTCQFormula transform(MTCQFormula formula)
    {
        DNFTransformer transformer = new DNFTransformer();
        formula.accept(transformer);
        return transformer.getTransformedFormula();
    }

    public MTCQFormula getTransformedFormula()
    {
        return _newFormula;
    }

    @Override
    public void visit(NotFormula formula)
    {
        MTCQFormula sub = formula.getSubFormula();
        if (sub instanceof NotFormula)
        {
            _newFormula = sub;
        }
        else if (sub instanceof OrFormula subOr)
        {
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    transform(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subOr.getLeftSubFormula())),
                    transform(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subOr.getRightSubFormula()))
            );
        }
        else if (sub instanceof AndFormula subAnd)
        {
            _newFormula = new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                    transform(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getLeftSubFormula())),
                    transform(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getRightSubFormula())));
        }
        else if (sub instanceof StrongNextFormula subNext)
        {
            _newFormula = new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                    transform(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subNext.getSubFormula()))
            );
        }
        else if (sub instanceof WeakNextFormula subNext)
        {
            _newFormula = new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                    transform(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subNext.getSubFormula()))
            );
        }
        else if (sub instanceof UntilFormula subAnd)
        {
            _newFormula = new ReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                    transform(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getLeftSubFormula())),
                    transform(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getRightSubFormula()))
            );
        }
        else if (sub instanceof ReleaseFormula subAnd)
        {
            _newFormula = new UntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                    transform(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getLeftSubFormula())),
                    transform(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getRightSubFormula()))
            );
        }
        else
            _newFormula = new NotFormula(formula.getTemporalKB(), formula.isDistinct(), transform(sub));
    }

    @Override
    public void visit(LogicalTrueFormula formula)
    {
        _newFormula = new LogicalTrueFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(LogicalFalseFormula formula)
    {
        _newFormula = new LogicalFalseFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(PropositionalTrueFormula formula)
    {
        _newFormula = new PropositionalTrueFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(PropositionalFalseFormula formula)
    {
        _newFormula = new PropositionalFalseFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(LastFormula formula)
    {
        _newFormula = new LastFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(ImplFormula formula)
    {
        _newFormula = new ImplFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getLeftSubFormula()), transform(formula.getRightSubFormula()));
    }

    @Override
    public void visit(GloballyFormula formula)
    {
        _newFormula = new GloballyFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getSubFormula()));
    }

    @Override
    public void visit(EventuallyFormula formula)
    {
        _newFormula = new EventuallyFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getSubFormula()));
    }

    @Override
    public void visit(EquivFormula formula)
    {
        _newFormula = new EquivFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getLeftSubFormula()), transform(formula.getRightSubFormula()));
    }

    @Override
    public void visit(EndFormula formula)
    {
        _newFormula = new EndFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(EmptyFormula formula)
    {
        _newFormula = new EmptyFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(ConjunctiveQueryFormula formula)
    {
        _newFormula = new ConjunctiveQueryFormula(formula.getTemporalKB(), formula.isDistinct(),
                formula.getConjunctiveQuery().copy());
    }

    @Override
    public void visit(BoundedUntilFormula formula)
    {
        _newFormula = new BoundedUntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getLeftSubFormula()), transform(formula.getRightSubFormula()),
                formula.getLowerBound(), formula.getUpperBound());
    }

    @Override
    public void visit(BoundedReleaseFormula formula)
    {
        _newFormula = new BoundedReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getLeftSubFormula()), transform(formula.getRightSubFormula()),
                formula.getLowerBound(), formula.getUpperBound());
    }

    @Override
    public void visit(BoundedGloballyFormula formula)
    {
        _newFormula = new BoundedGloballyFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getSubFormula()), formula.getLowerBound(), formula.getUpperBound());
    }

    @Override
    public void visit(BoundedEventuallyFormula formula)
    {
        _newFormula = new BoundedEventuallyFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getSubFormula()), formula.getLowerBound(), formula.getUpperBound());
    }

    public void visit(AndFormula formula)
    {
        _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getLeftSubFormula()), transform(formula.getRightSubFormula()));
    }

    @Override
    public void visit(XorFormula formula)
    {
        _newFormula = new XorFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getLeftSubFormula()), transform(formula.getRightSubFormula()));
    }

    @Override
    public void visit(WeakNextFormula formula)
    {
        _newFormula = new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getSubFormula()));
    }

    @Override
    public void visit(UntilFormula formula)
    {
        _newFormula = new UntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getLeftSubFormula()), transform(formula.getRightSubFormula()));
    }

    @Override
    public void visit(StrongNextFormula formula)
    {
        _newFormula = new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getSubFormula()));
    }

    @Override
    public void visit(ReleaseFormula formula)
    {
        _newFormula = new ReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getLeftSubFormula()), transform(formula.getRightSubFormula()));
    }

    @Override
    public void visit(OrFormula formula)
    {
        _newFormula = new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                transform(formula.getLeftSubFormula()), transform(formula.getRightSubFormula()));
    }
}
