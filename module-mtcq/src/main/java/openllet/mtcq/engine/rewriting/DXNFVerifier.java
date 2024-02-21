package openllet.mtcq.engine.rewriting;

import openllet.mtcq.model.query.*;

public class DXNFVerifier implements MTCQVisitor
{
    private boolean _isValid = true;
    private OrFormula _reason = null;

    public boolean verify(MTCQFormula formula)
    {
        _isValid = true;
        formula.accept(this);
        return _isValid;
    }

    public MTCQFormula getReason()
    {
        return _reason;
    }

    @Override
    public void visit(XorFormula formula)
    {
        formula.getLeftSubFormula().accept(this);
        formula.getRightSubFormula().accept(this);
    }

    @Override
    public void visit(WeakNextFormula formula)
    {
        formula.getSubFormula().accept(this);
    }

    @Override
    public void visit(UntilFormula formula)
    {
        formula.getLeftSubFormula().accept(this);
        formula.getRightSubFormula().accept(this);
    }

    @Override
    public void visit(StrongNextFormula formula)
    {
        // Intentionally left empty
    }

    @Override
    public void visit(ReleaseFormula formula)
    {
        formula.getLeftSubFormula().accept(this);
        formula.getRightSubFormula().accept(this);
    }

    @Override
    public void visit(OrFormula formula)
    {
        if (formula.getLeftSubFormula().isTemporal() && !(formula.getLeftSubFormula() instanceof StrongNextFormula))
        {
            _isValid = false;
            _reason = formula;
        }
        else if (formula.getRightSubFormula().isTemporal() && !(formula.getRightSubFormula() instanceof StrongNextFormula))
        {
            _isValid = false;
            _reason = formula;
        }
    }

    @Override
    public void visit(NotFormula formula)
    {
        formula.getSubFormula().accept(this);
    }

    @Override
    public void visit(LogicalTrueFormula formula)
    {
        // Intentionally left empty
    }

    @Override
    public void visit(LogicalFalseFormula formula)
    {
        // Intentionally left empty
    }

    @Override
    public void visit(PropositionalTrueFormula formula)
    {
        // Intentionally left empty
    }

    @Override
    public void visit(PropositionalFalseFormula formula)
    {
        // Intentionally left empty
    }

    @Override
    public void visit(LastFormula formula)
    {
        // Intentionally left empty
    }

    @Override
    public void visit(ImplFormula formula)
    {
        formula.getLeftSubFormula().accept(this);
        formula.getRightSubFormula().accept(this);
    }

    @Override
    public void visit(GloballyFormula formula)
    {
        formula.getSubFormula().accept(this);
    }

    @Override
    public void visit(EventuallyFormula formula)
    {
        formula.getSubFormula().accept(this);
    }

    @Override
    public void visit(EquivFormula formula)
    {
        formula.getLeftSubFormula().accept(this);
        formula.getRightSubFormula().accept(this);
    }

    @Override
    public void visit(EndFormula formula)
    {
        // Intentionally left empty
    }

    @Override
    public void visit(EmptyFormula formula)
    {
        // Intentionally left empty
    }

    @Override
    public void visit(ConjunctiveQueryFormula formula)
    {
        // Intentionally left empty
    }

    @Override
    public void visit(BoundedUntilFormula formula)
    {
        formula.getLeftSubFormula().accept(this);
        formula.getRightSubFormula().accept(this);
    }

    @Override
    public void visit(BoundedReleaseFormula formula)
    {
        formula.getLeftSubFormula().accept(this);
        formula.getRightSubFormula().accept(this);
    }

    @Override
    public void visit(BoundedGloballyFormula formula)
    {
        formula.getSubFormula().accept(this);
    }

    @Override
    public void visit(BoundedEventuallyFormula formula)
    {
        formula.getSubFormula().accept(this);
    }

    @Override
    public void visit(AndFormula formula)
    {
        formula.getLeftSubFormula().accept(this);
        formula.getRightSubFormula().accept(this);
    }
}
