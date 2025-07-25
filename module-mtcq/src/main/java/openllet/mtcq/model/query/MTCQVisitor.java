package openllet.mtcq.model.query;

public interface MTCQVisitor
{
    void visit(XorFormula formula);
    void visit(WeakNextFormula formula);
    void visit(UntilFormula formula);
    void visit(StrongNextFormula formula);
    void visit(ReleaseFormula formula);
    void visit(OrFormula formula);
    void visit(NotFormula formula);
    void visit(LogicalTrueFormula formula);
    void visit(LogicalFalseFormula formula);
    void visit(PropositionalTrueFormula formula);
    void visit(PropositionalFalseFormula formula);
    void visit(LastFormula formula);
    void visit(ImplFormula formula);
    void visit(GloballyFormula formula);
    void visit(EventuallyFormula formula);
    void visit(EquivFormula formula);
    void visit(EndFormula formula);
    void visit(EmptyFormula formula);
    void visit(ConjunctiveQueryFormula formula);
    void visit(BoundedUntilFormula formula);
    void visit(BoundedReleaseFormula formula);
    void visit(BoundedGloballyFormula formula);
    void visit(BoundedEventuallyFormula formula);
    void visit(AndFormula formula);
}
