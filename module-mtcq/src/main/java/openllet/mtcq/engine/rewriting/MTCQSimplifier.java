package openllet.mtcq.engine.rewriting;

import openllet.core.utils.Pair;
import openllet.mtcq.model.query.*;

import java.util.*;
import java.util.stream.Stream;

public class MTCQSimplifier extends StandardTransformer
{
    private boolean _isInCXNFNormalForm = false;

    @Override
    protected MetricTemporalConjunctiveQuery run(MetricTemporalConjunctiveQuery formula)
    {
        _isInCXNFNormalForm = new DXNFVerifier().verify(formula);
        return super.run(formula);
    }

    @Override
    public void visit(AndFormula formula)
    {
        MetricTemporalConjunctiveQuery l = formula.getLeftSubFormula();
        MetricTemporalConjunctiveQuery r = formula.getRightSubFormula();
        MetricTemporalConjunctiveQuery newFormula;
        // (A & true) -> A
        if (l instanceof LogicalTrueFormula || l instanceof PropositionalTrueFormula)
            newFormula = run(r);
        // (true & A) -> A
        else if (r instanceof LogicalTrueFormula || r instanceof PropositionalTrueFormula)
            newFormula = run(l);
        // (A & false) -> false
        else if (l instanceof LogicalFalseFormula || l instanceof PropositionalFalseFormula)
            newFormula = l.copy();
        // (false & A) -> false
        else if (r instanceof LogicalFalseFormula || r instanceof PropositionalFalseFormula)
            newFormula = r.copy();
        else
        {
            List<MetricTemporalConjunctiveQuery> lhs = flattenAnd(l);
            List<MetricTemporalConjunctiveQuery> rhs = flattenAnd(r);
            List<MetricTemporalConjunctiveQuery> both = new ArrayList<>(Stream.concat(lhs.stream(), rhs.stream()).toList());
            List<MetricTemporalConjunctiveQuery> toRemove = new ArrayList<>();
            List<MetricTemporalConjunctiveQuery> toAdd = new ArrayList<>();
            for (MetricTemporalConjunctiveQuery A : both)
            {
                List<MetricTemporalConjunctiveQuery> fNoA = new ArrayList<>(both);
                fNoA.remove(A);
                // (A & ... & !A) -> false
                if (fNoA.contains(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), A)))
                {
                    System.out.println("applied rule (A & ... & !A) -> false");
                    toAdd.add(new LogicalFalseFormula(formula.getTemporalKB(), formula.isDistinct()));
                }
                // X a & ... & X b -> X (a & b) ...
                else if (A instanceof StrongNextFormula AX)
                {
                    StrongNextFormula otherX = findFirstStrongNext(fNoA);
                    if (otherX != null)
                    {
                        toRemove.add(A);
                        toRemove.add(otherX);
                        toAdd.add(new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        AX.getSubFormula(),
                                        otherX.getSubFormula()))
                        );
                        System.out.println("applied rule X a & ... & X b -> X (a & b) ...");
                    }
                }
                else
                {
                    // ((A | ... | B) & ... & (A | ... | B | ...)) -> (A | ... | B) & ...
                    //   special case included: A & ... & A -> A & ...
                    List<MetricTemporalConjunctiveQuery> aOrs = flattenOr(A);
                    for (MetricTemporalConjunctiveQuery B : fNoA)
                    {
                        List<MetricTemporalConjunctiveQuery> bOrs = flattenOr(B);
                        if (new HashSet<>(bOrs).containsAll(aOrs))
                        {
                            toRemove.add(B);
                            System.out.println("applied rule ((A | ... | B) & ... & (A | ... | B | ...)) -> (A | ... | B) & ...");
                            break;
                        }
                    }
                    // A == X b | a resp. a | X b - rules only applicable in normal form
                    if (_isInCXNFNormalForm &&
                            (A instanceof OrFormula AOr && ((AOr.getLeftSubFormula() instanceof StrongNextFormula) ||
                            (AOr.getRightSubFormula() instanceof StrongNextFormula))))
                    {
                        StrongNextFormula AX;
                        MetricTemporalConjunctiveQuery AO;
                        if (AOr.getLeftSubFormula() instanceof StrongNextFormula AOrX)
                        {
                            AX = AOrX;
                            AO = AOr.getRightSubFormula();
                        }
                        else
                        {
                            AX = (StrongNextFormula) AOr.getRightSubFormula();
                            AO = AOr.getLeftSubFormula();
                        }
                        // (X b | a) & ... & (X c | a) -> (X (b & c) | a) & ...
                        Pair<MetricTemporalConjunctiveQuery, StrongNextFormula> a_or_Xb_same_a =
                                getFirstSameAInAOrXbSequence(fNoA, AO);
                        if (a_or_Xb_same_a != null)
                        {
                            toRemove.add(A);
                            toRemove.add(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    a_or_Xb_same_a.first, a_or_Xb_same_a.second));
                            toAdd.add(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                AX.getSubFormula(),
                                                a_or_Xb_same_a.second.getSubFormula())
                                    ),
                                    AO
                            ));
                            System.out.println("applied rule (X b | a) & ... & (X c | a) -> (X (b & c) | a) & ...");
                        }
                        // (X b | a) & ... & (X b | c) -> (X b | (a & c)) & ...
                        else
                        {
                            Pair<MetricTemporalConjunctiveQuery, StrongNextFormula> a_or_Xb_same_Xb =
                                    getFirstSameXbInAOrXbSequence(fNoA, AX);
                            if (a_or_Xb_same_Xb != null)
                            {
                                toRemove.add(A);
                                toRemove.add(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        a_or_Xb_same_Xb.first, a_or_Xb_same_Xb.second));
                                toAdd.add(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                AX,
                                                new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                        AO,
                                                        a_or_Xb_same_Xb.first
                                                )
                                ));
                                System.out.println("applied rule (X b | a) & ... & (X b | c) -> (X b | (a & c)) & ...");
                            }
                        }
                    }
                }
                // only do one rule at a time
                if (!toAdd.isEmpty() || !toRemove.isEmpty())
                    break;
            }
            newFormula = assembleAndFormula(both, toRemove, toAdd);
        }
        if (newFormula != null)
        {
            System.out.println("orig = " + formula.toPropositionalAbstractionString());
            System.out.println("new = " + newFormula.toPropositionalAbstractionString());
            appliedTransformationRule("AND");
            _newFormula = run(newFormula);
        }
        else
            super.visit(formula);
    }

    private Pair<MetricTemporalConjunctiveQuery, StrongNextFormula> getFirstSameXbInAOrXbSequence(
            List<MetricTemporalConjunctiveQuery> queries, StrongNextFormula Xb)
    {
        for (MetricTemporalConjunctiveQuery q : queries)
            if (q instanceof OrFormula qOr && qOr.getLeftSubFormula() instanceof StrongNextFormula qOrX &&
                    qOrX.equals(Xb))
                return new Pair<>(qOr.getRightSubFormula(), qOrX);
            else if (q instanceof OrFormula qOr && qOr.getRightSubFormula() instanceof StrongNextFormula qOrX
                    && qOrX.equals(Xb))
                return new Pair<>(qOr.getLeftSubFormula(), qOrX);
        return null;
    }

    private Pair<MetricTemporalConjunctiveQuery, StrongNextFormula> getFirstSameAInAOrXbSequence(
            List<MetricTemporalConjunctiveQuery> queries, MetricTemporalConjunctiveQuery A)
    {
        for (MetricTemporalConjunctiveQuery q : queries)
            if (q instanceof OrFormula qOr && qOr.getLeftSubFormula() instanceof StrongNextFormula qOrX &&
                    qOr.getRightSubFormula().equals(A))
                return new Pair<>(qOr.getRightSubFormula(), qOrX);
            else if (q instanceof OrFormula qOr && qOr.getRightSubFormula() instanceof StrongNextFormula qOrX &&
                    qOr.getLeftSubFormula().equals(A))
                return new Pair<>(qOr.getLeftSubFormula(), qOrX);
        return null;
    }

    private MetricTemporalConjunctiveQuery assembleAndFormula(List<MetricTemporalConjunctiveQuery> andFormula,
                                                              List<MetricTemporalConjunctiveQuery> toRemove,
                                                              List<MetricTemporalConjunctiveQuery> toAdd)
    {
        MetricTemporalConjunctiveQuery query = null;
        if (!(toRemove.isEmpty() && toAdd.isEmpty()) && !andFormula.isEmpty())
        {
            MetricTemporalConjunctiveQuery falseFormula = new LogicalFalseFormula(andFormula.get(0).getTemporalKB(),
                    andFormula.get(0).isDistinct());
            if (toAdd.contains(falseFormula))
                query = falseFormula;
            else
            {
                andFormula.removeAll(toRemove);
                andFormula.addAll(toAdd);
                if (andFormula.size() > 1)
                    query = makeAnd(andFormula);
                else if (andFormula.size() == 1)
                    query = andFormula.get(0);
                else
                    query = falseFormula;
            }
        }
        return query;
    }

    private StrongNextFormula findFirstStrongNext(List<MetricTemporalConjunctiveQuery> queries)
    {
        for (MetricTemporalConjunctiveQuery q : queries)
            if (q instanceof StrongNextFormula x)
                return x;
        return null;
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

    private AndFormula makeAnd(Collection<MetricTemporalConjunctiveQuery> conjuncts)
    {
        Iterator<MetricTemporalConjunctiveQuery> it = conjuncts.iterator();
        MetricTemporalConjunctiveQuery first = it.next();
        it.remove();
        if (conjuncts.size() >= 2)
            return new AndFormula(first.getTemporalKB(), first.isDistinct(), first, makeAnd(conjuncts));
        else
            return new AndFormula(first.getTemporalKB(), first.isDistinct(), first, it.next());
    }

    public static OrFormula makeOr(Collection<MetricTemporalConjunctiveQuery> disjuncts)
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
