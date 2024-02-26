package openllet.mtcq.engine.rewriting;

import openllet.mtcq.model.query.*;

public class MTCQSimplifier extends StandardTransformer
{
    @Override
    public void visit(AndFormula formula)
    {
        MetricTemporalConjunctiveQuery l = formula.getLeftSubFormula();
        MetricTemporalConjunctiveQuery r = formula.getRightSubFormula();
        // (A & true) -> A
        if (l instanceof LogicalTrueFormula || l instanceof PropositionalTrueFormula)
            _newFormula = run(r);
        // (true & A) -> A
        else if (r instanceof LogicalTrueFormula || r instanceof PropositionalTrueFormula)
            _newFormula = run(l);
        // (A & false) -> false
        if (l instanceof LogicalFalseFormula || l instanceof PropositionalFalseFormula)
            _newFormula = l.copy();
        // (false & A) -> false
        else if (r instanceof LogicalFalseFormula || r instanceof PropositionalFalseFormula)
            _newFormula = r.copy();
        // (A & !A) -> false
        else if (l instanceof NotFormula lNot && lNot.getSubFormula().equals(r))
            _newFormula = new LogicalFalseFormula(formula.getTemporalKB(), formula.isDistinct());
        // (!A & A) -> false
        else if (r instanceof NotFormula rNot && rNot.getSubFormula().equals(l))
            _newFormula = new LogicalFalseFormula(formula.getTemporalKB(), formula.isDistinct());
        // (A & A) -> A
        else if (r.equals(l))
            _newFormula = run(l);
        // (A & (A & B)) -> (A & B)
        else if ((r instanceof AndFormula rAnd && rAnd.getLeftSubFormula().equals(l)))
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(l),
                    run(rAnd.getRightSubFormula())
            );
        // (A & (B & A)) -> (A & B)
        else if ((r instanceof AndFormula rAnd && rAnd.getRightSubFormula().equals(l)))
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(l),
                    run(rAnd.getLeftSubFormula())
            );
        // ((A & B) & A) -> (A & B)
        else if ((l instanceof AndFormula lAnd && lAnd.getLeftSubFormula().equals(r)))
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(r),
                    run(lAnd.getRightSubFormula())
            );
        // ((B & A) & A) -> (A & B)
        else if ((l instanceof AndFormula lAnd && lAnd.getRightSubFormula().equals(r)))
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(r),
                    run(lAnd.getLeftSubFormula())
            );
        // (A & (A | B)) -> A
        else if ((r instanceof OrFormula rOr && rOr.getLeftSubFormula().equals(l)))
            _newFormula = run(l);
        // (A & (B | A)) -> A
        else if ((r instanceof OrFormula rOr && rOr.getRightSubFormula().equals(l)))
            _newFormula = run(l);
        // ((A | B) & A) -> A
        else if ((l instanceof OrFormula lOr && lOr.getLeftSubFormula().equals(r)))
            _newFormula = run(r);
        // ((B | A) & A) -> A
        else if ((l instanceof OrFormula lOr && lOr.getRightSubFormula().equals(r)))
            _newFormula = run(r);
        else if (r.equals(l))
            _newFormula = run(l);
        else
            super.visit(formula);
    }

    @Override
    public void visit(OrFormula formula)
    {
        MetricTemporalConjunctiveQuery l = formula.getLeftSubFormula();
        MetricTemporalConjunctiveQuery r = formula.getRightSubFormula();
        // (A | true) -> true
        if (l instanceof LogicalTrueFormula || l instanceof PropositionalTrueFormula)
            _newFormula = l.copy();
        // (true | A) -> true
        else if (r instanceof LogicalTrueFormula || r instanceof PropositionalTrueFormula)
            _newFormula = r.copy();
        // (A | false) -> A
        if (l instanceof LogicalFalseFormula || l instanceof PropositionalFalseFormula)
            _newFormula = run(r);
        // (false | A) -> A
        else if (r instanceof LogicalFalseFormula || r instanceof PropositionalFalseFormula)
            _newFormula = run(l);
        // (A | !A) -> true
        else if (l instanceof NotFormula lNot && lNot.getSubFormula().equals(r))
            _newFormula = new LogicalTrueFormula(formula.getTemporalKB(), formula.isDistinct());
        // (!A | A) -> true
        else if (r instanceof NotFormula rNot && rNot.getSubFormula().equals(l))
            _newFormula = new LogicalTrueFormula(formula.getTemporalKB(), formula.isDistinct());
        // (A | (A | B)) -> (A | B)
        else if ((r instanceof OrFormula rOr && rOr.getLeftSubFormula().equals(l)))
            _newFormula = new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(l),
                    run(rOr.getRightSubFormula())
            );
        // (A | (B | A)) -> (A | B)
        else if ((r instanceof OrFormula rOr && rOr.getRightSubFormula().equals(l)))
            _newFormula = new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(l),
                    run(rOr.getLeftSubFormula())
            );
        // ((A | B) | A) -> (A | B)
        else if ((l instanceof OrFormula lOr && lOr.getLeftSubFormula().equals(r)))
            _newFormula = new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(r),
                    run(lOr.getRightSubFormula())
            );
        // ((B | A) | A) -> (A | B)
        else if ((l instanceof OrFormula lOr && lOr.getRightSubFormula().equals(r)))
            _newFormula = new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(r),
                    run(lOr.getLeftSubFormula())
            );
        // (A | (A & B)) -> A
        else if ((r instanceof AndFormula rAnd && rAnd.getLeftSubFormula().equals(l)))
            _newFormula = run(l);
        // (A | (B & A)) -> A
        else if ((r instanceof AndFormula rAnd && rAnd.getRightSubFormula().equals(l)))
            _newFormula = run(l);
        // ((A & B) | A) -> A
        else if ((l instanceof AndFormula lAnd && lAnd.getLeftSubFormula().equals(r)))
            _newFormula = run(r);
        // ((B & A) | A) -> A
        else if ((l instanceof AndFormula lAnd && lAnd.getRightSubFormula().equals(r)))
            _newFormula = run(r);
        // (A | A) -> A
        else if (r.equals(l))
            _newFormula = run(l);
        else
            super.visit(formula);
    }

    @Override
    public void visit(NotFormula formula)
    {
        // !(!(A)) -> A
        if (formula.getSubFormula() instanceof NotFormula subNot)
            _newFormula = run(subNot.getSubFormula());
        else
            super.visit(formula);
    }
}
