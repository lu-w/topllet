package openllet.mtcq.engine.rewriting;

import openllet.mtcq.model.query.*;

public abstract class StandardTransformer implements MTCQVisitor
{
    protected MetricTemporalConjunctiveQuery _newFormula;

    public static MetricTemporalConjunctiveQuery transform(MetricTemporalConjunctiveQuery formula)
    {
        return new MTCQSimplifier().run(formula);
    }

    protected MetricTemporalConjunctiveQuery run(MetricTemporalConjunctiveQuery formula)
    {
        formula.accept(this);
        return this.getTransformedFormula();
    }

    protected MetricTemporalConjunctiveQuery getTransformedFormula()
    {
        return _newFormula;
    }

    @Override
    public void visit(XorFormula formula)
    {
        _newFormula = new XorFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()),
                run(formula.getRightSubFormula())
        );
    }

    @Override
    public void visit(WeakNextFormula formula)
    {
        _newFormula = new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula())
        );
    }

    @Override
    public void visit(UntilFormula formula)
    {
        _newFormula = new UntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()),
                run(formula.getRightSubFormula())
        );
    }

    @Override
    public void visit(StrongNextFormula formula)
    {
        _newFormula = new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula())
        );
    }

    @Override
    public void visit(ReleaseFormula formula)
    {
        _newFormula = new ReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()),
                run(formula.getRightSubFormula())
        );
    }

    @Override
    public void visit(OrFormula formula)
    {
        _newFormula = new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()),
                run(formula.getRightSubFormula())
        );
    }

    @Override
    public void visit(NotFormula formula)
    {
        _newFormula = new NotFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula())
        );
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
    public void visit(ImplFormula formula)
    {
        _newFormula = new ImplFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()),
                run(formula.getRightSubFormula())
        );
    }

    @Override
    public void visit(GloballyFormula formula)
    {
        _newFormula = new GloballyFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula())
        );
    }

    @Override
    public void visit(EventuallyFormula formula)
    {
        _newFormula = new EventuallyFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula())
        );
    }

    @Override
    public void visit(EquivFormula formula)
    {
        _newFormula = new EquivFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()),
                run(formula.getRightSubFormula())
        );
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
    public void visit(BoundedUntilFormula formula)
    {
        _newFormula = new BoundedUntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()),
                run(formula.getRightSubFormula()),
                formula.getLowerBound(), formula.getUpperBound()
        );
    }

    @Override
    public void visit(BoundedReleaseFormula formula)
    {
        _newFormula = new BoundedReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()),
                run(formula.getRightSubFormula()),
                formula.getLowerBound(), formula.getUpperBound()
        );
    }

    @Override
    public void visit(BoundedGloballyFormula formula)
    {
        _newFormula = new BoundedGloballyFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula()),
                formula.getLowerBound(), formula.getUpperBound()
        );
    }

    @Override
    public void visit(BoundedEventuallyFormula formula)
    {
        _newFormula = new BoundedEventuallyFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula()),
                formula.getLowerBound(), formula.getUpperBound()
        );
    }

    @Override
    public void visit(AndFormula formula)
    {
        _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()),
                run(formula.getRightSubFormula())
        );
    }
}
