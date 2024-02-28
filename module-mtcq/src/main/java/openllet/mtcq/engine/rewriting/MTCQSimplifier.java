package openllet.mtcq.engine.rewriting;

import openllet.mtcq.model.query.*;

import java.util.*;

public class MTCQSimplifier extends StandardTransformer
{
    @Override
    public void visit(AndFormula formula)
    {
        MetricTemporalConjunctiveQuery l = formula.getLeftSubFormula();
        MetricTemporalConjunctiveQuery r = formula.getRightSubFormula();
        boolean onlyDidRecursion = false;
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
        else
        {
            List<MetricTemporalConjunctiveQuery> ls = flattenOr(l);
            List<MetricTemporalConjunctiveQuery> rs = flattenOr(r);
            // ((A | B | C) & (A | B)) -> (A | B)
            if (ls.containsAll(rs))
                _newFormula = run(r);
            // ((A | B) & (A | B | C)) -> (A | B)
            else if (rs.containsAll(ls))
                _newFormula = run(l);
            else
            {
                // ((A & C) & (A & B)) -> (A & B & C)
                ls = flattenAnd(l);
                rs = flattenAnd(r);
                if (!Collections.disjoint(ls, rs))
                {
                    Set<MetricTemporalConjunctiveQuery> conjunts = new HashSet<>(ls);
                    conjunts.addAll(rs);
                    AndFormula simplifiedAnd = makeAnd(conjunts);
                    super.visit(simplifiedAnd);
                }
                // (X b | a) & (X c | a) & (X d | a) & (X e | x) -> X (b & c & d) | a) & X(e | x)

                // (X b | (a1 | a2)) & (X c | a1) ->

                else
                {
                    onlyDidRecursion = true;
                    super.visit(formula);
                }

                // TODO what if both are applicable?
                // TODO? ((A | B | D) & (A | B | C)) -> (A | B) | (C & D)
            }
        }
        if (!onlyDidRecursion)
            appliedTransformationRule("OR");
    }

    @Override
    public void visit(OrFormula formula)
    {
        MetricTemporalConjunctiveQuery l = formula.getLeftSubFormula();
        MetricTemporalConjunctiveQuery r = formula.getRightSubFormula();
        boolean onlyDidRecursion = false;
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
        // (A | A) -> A
        else if (r.equals(l))
            _newFormula = run(l);
        else
        {
            List<MetricTemporalConjunctiveQuery> ls = flattenAnd(l);
            List<MetricTemporalConjunctiveQuery> rs = flattenAnd(r);
            // ((A & B & C) | (A & B)) -> (A & B)
            if (ls.containsAll(rs))
                _newFormula = run(r);
            // ((A & B) | (A & B & C)) -> (A & B)
            else if (rs.containsAll(ls))
                _newFormula = run(l);
            else
            {
                // ((A | C) | (A | B)) -> (A | B | C)
                ls = flattenOr(l);
                rs = flattenOr(r);
                if (!Collections.disjoint(ls, rs))
                {
                    Set<MetricTemporalConjunctiveQuery> disjuncts = new HashSet<>(ls);
                    disjuncts.addAll(rs);
                    OrFormula simplifiedOr = makeOr(disjuncts);
                    super.visit(simplifiedOr);
                }
                else
                {
                    onlyDidRecursion = true;
                    super.visit(formula);
                }

                // TODO what if both are applicable?
                // TODO? ((A & B & D) | (A & B & C)) -> (A & B) & (C | D)
            }
        }
        if (!onlyDidRecursion)
            appliedTransformationRule("OR");
    }

    private AndFormula makeAnd(Set<MetricTemporalConjunctiveQuery> conjuncts)
    {
        Iterator<MetricTemporalConjunctiveQuery> it = conjuncts.iterator();
        MetricTemporalConjunctiveQuery first = it.next();
        it.remove();
        if (conjuncts.size() >= 2)
            return new AndFormula(first.getTemporalKB(), first.isDistinct(), first, makeAnd(conjuncts));
        else
            return new AndFormula(first.getTemporalKB(), first.isDistinct(), first, it.next());
    }

    private OrFormula makeOr(Set<MetricTemporalConjunctiveQuery> disjuncts)
    {
        Iterator<MetricTemporalConjunctiveQuery> it = disjuncts.iterator();
        MetricTemporalConjunctiveQuery first = it.next();
        it.remove();
        if (disjuncts.size() >= 2)
            return new OrFormula(first.getTemporalKB(), first.isDistinct(), first, makeOr(disjuncts));
        else
            return new OrFormula(first.getTemporalKB(), first.isDistinct(), first, it.next());
    }

    public static List<MetricTemporalConjunctiveQuery> flattenAnd(MetricTemporalConjunctiveQuery formula)
    {
        List<MetricTemporalConjunctiveQuery> flattened = new ArrayList<>();
        Stack<MetricTemporalConjunctiveQuery> toResolve = new Stack<>();
        if (formula instanceof AndFormula and)
        {
            toResolve.push(and.getLeftSubFormula());
            toResolve.push(and.getRightSubFormula());
        }
        else
            return List.of(formula);
        while(!toResolve.empty())
        {
            MetricTemporalConjunctiveQuery f = toResolve.pop();
            if (f instanceof AndFormula fAnd)
            {
                toResolve.push(fAnd.getLeftSubFormula());
                toResolve.push(fAnd.getRightSubFormula());
            }
            else
                flattened.add(f);
        }
        return flattened;
    }

    public static List<MetricTemporalConjunctiveQuery> flattenOr(MetricTemporalConjunctiveQuery formula)
    {
        List<MetricTemporalConjunctiveQuery> flattened = new ArrayList<>();
        Stack<MetricTemporalConjunctiveQuery> toResolve = new Stack<>();
        if (formula instanceof OrFormula or)
        {
            toResolve.push(or.getLeftSubFormula());
            toResolve.push(or.getRightSubFormula());
        }
        else
            return List.of(formula);
        while(!toResolve.empty())
        {
            MetricTemporalConjunctiveQuery f = toResolve.pop();
            if (f instanceof OrFormula fOr)
            {
                toResolve.push(fOr.getLeftSubFormula());
                toResolve.push(fOr.getRightSubFormula());
            }
            else
                flattened.add(f);
        }
        return flattened;
    }

    @Override
    public void visit(NotFormula formula)
    {
        // !(!(A)) -> A
        if (formula.getSubFormula() instanceof NotFormula subNot)
        {
            _newFormula = run(subNot.getSubFormula());
            appliedTransformationRule("NOT");
        }
        else
            super.visit(formula);
    }

    // TODO
    //  - F_[0,0] a -> a
    //  - check for below 2 cases: if they are in a series of conjunctions... -> need to flatten this conjunction as well
    //  - (X a | b) & (X c | b) -> X (a & c) | b  (no need for complicated cases with lots of nested | since DXNF pretty much ensures (X a | b) & (X c | d) & ... form - right??
    //  - (X a | b) & (X a | c) -> X a | (b & c)
    //  - X a & X b -> X (a & b)
    //  think whether other temporal simplification would also make sense. maybe only for X?
    //  - (X a) U (X b) -> X (a U b)
    //  - (X a) R (X b) -> X (a R b)
    //  - tt U Xa -> X (tt U a)
    //  - ff R Xa -> X (ff R a)
    //  - X tt -> tt
    //  - FX a -> XF a
    //  - a U ff -> ff
    //  - GF a v GF b -> GF (a v b) resp. last(a vb)
    //  - FG a v FG b -> FG (a v b) resp. last(a vb)
    //  - (a U b) &/v (c U b) -> (a &/v b) U c
    //  - (a U b) &/v (a U c) -> (a &/v b) U c
    //  - true U (a U b) -> true U b
    //  - false R (a U b) -> false R q
    //  - GGa -> Ga
    //  - F(a U b) -> F b
    //  - (G A) & (G B) -> G (A & B)
    // TODO confirm all of these for LTLf!
    //  and all these equivalently for F/G and bounded versions
}
