package openllet.mtcq.engine.rewriting;

import openllet.mtcq.model.query.*;

public class DNFTransformer implements MTCQVisitor
{
    private MTCQFormula _newFormula;

    public MTCQFormula transform(MTCQFormula formula)
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
        MTCQFormula transformed;
        MTCQFormula sub = formula.getSubFormula();
        if (sub instanceof NotFormula)
        {
            transformed = sub;
        }
        else if (sub instanceof OrFormula subOr)
        {
            transformed = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), transform(subOr.getLeftSubFormula())),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), transform(subOr.getRightSubFormula()))
            );
        }
        else if (sub instanceof AndFormula subAnd)
        {
            transformed = new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getLeftSubFormula()),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getRightSubFormula())
            );
        }
        else if (sub instanceof StrongNextFormula subNext)
        {
            transformed = new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subNext.getSubFormula())
            );
        }
        else if (sub instanceof WeakNextFormula subNext)
        {
            transformed = new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subNext.getSubFormula())
            );
        }
        else if (sub instanceof UntilFormula subAnd)
        {
            transformed = new ReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getLeftSubFormula()),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getRightSubFormula())
            );
        }
        else if (sub instanceof ReleaseFormula subAnd)
        {
            transformed = new UntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getLeftSubFormula()),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getRightSubFormula())
            );
        }
        else if (sub instanceof UnaryMTCQFormula subUnary)
        {
            transformed = transform(subUnary.getSubFormula());
        }
        else if (sub instanceof BinaryMTCQFormula subBinary)
        {
            transformed = transform(subBinary.getLeftSubFormula());
        }

    }

    @Override
    public void visit(LogicalTrueFormula formula)
    {

    }

    @Override
    public void visit(LogicalFalseFormula formula)
    {

    }

    @Override
    public void visit(LastFormula formula)
    {

    }

    @Override
    public void visit(ImplFormula formula)
    {

    }

    @Override
    public void visit(GloballyFormula formula)
    {

    }

    @Override
    public void visit(EventuallyFormula formula)
    {

    }

    @Override
    public void visit(EquivFormula formula)
    {

    }

    @Override
    public void visit(EndFormula formula)
    {

    }

    @Override
    public void visit(EmptyFormula formula)
    {

    }

    @Override
    public void visit(ConjunctiveQueryFormula formula)
    {

    }

    @Override
    public void visit(BoundedUntilFormula formula)
    {

    }

    @Override
    public void visit(BoundedReleaseFormula formula)
    {

    }

    @Override
    public void visit(BoundedGloballyFormula formula)
    {

    }

    @Override
    public void visit(BoundedEventuallyFormula formula)
    {

    }

    public void visit(AndFormula formula)
    {

    }

    @Override
    public void visit(XorFormula formula)
    {

    }

    @Override
    public void visit(WeakNextFormula formula)
    {

    }

    @Override
    public void visit(UntilFormula formula)
    {

    }

    @Override
    public void visit(StrongNextFormula formula)
    {

    }

    @Override
    public void visit(ReleaseFormula formula)
    {

    }

    @Override
    public void visit(OrFormula formula)
    {

    }
}
