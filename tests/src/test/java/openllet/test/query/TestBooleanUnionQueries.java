package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.KnowledgeBaseImpl;
import openllet.mtcq.engine.MTCQNormalFormEngine;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.query.sparqldl.engine.ucq.BooleanUnionQueryEngineSimple;
import openllet.query.sparqldl.engine.ucq.UnionQueryEngineSimple;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.ucq.UnionQuery;
import org.junit.Test;

import static openllet.core.utils.TermFactory.*;
import static openllet.core.utils.TermFactory.TOP;
import static openllet.query.sparqldl.model.cq.QueryAtomFactory.PropertyValueAtom;
import static openllet.query.sparqldl.model.cq.QueryAtomFactory.TypeAtom;
import static org.junit.Assert.*;

public class TestBooleanUnionQueries extends AbstractQueryTest
{
    @Test
    public void testBooleanQueries1()
    {
        classes(_A, _B, _C, _D, _E);
        individuals(_a, _b, _c);
        objectProperties(_r, _p, _q);

        _kb.addSubClass(_A, or(_B, _C));
        _kb.addSubClass(_A, not(_D));
        _kb.addSubClass(_B, some(_p, TOP));
        _kb.addSubClass(_C, not(some(_r, TOP)));
        _kb.addType(_a, _A);
        _kb.addType(_b, _D);
        _kb.addPropertyValue(_r, _a, _b);

        // UCQ 1: B(x) & r(x,b) v C(x) & r(x,b) -> is entailed but second disjunct can never be true
        UnionQuery ucq1 = unionQuery(query(TypeAtom(x, _B), PropertyValueAtom(x, _r, _b)),
                                     query(TypeAtom(y, _C), PropertyValueAtom(y, _r, _b)));

        // UCQ 2: B(x) & p(x,b) v C(x) & r(x,b) -> not entailed
        UnionQuery ucq2 = unionQuery(query(TypeAtom(x, _B), PropertyValueAtom(x, _p, _b)),
                                     query(TypeAtom(y, _C), PropertyValueAtom(y, _r, _b)));

        // UCQ 3: B(a) & r(a,x) v D(z) & r(y,z) -> entailed (both disjuncts)
        UnionQuery ucq3 = unionQuery(query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x)),
                                     query(TypeAtom(z, _D), PropertyValueAtom(y, _r, z)));

        // UCQ 4: B(a) & r(a,x) v E(z) & r(y,z) -> entailed (only first disjunct)
        UnionQuery ucq4 = unionQuery(query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x)),
                                     query(TypeAtom(z, _E), PropertyValueAtom(y, _r, z)));

        // UCQ 5: B(a) & r(a,x) & p(y,z) & D(y) v C(a) v B(a) & p(a,x) & r(a,y) & D(b) -> entailed
        UnionQuery ucq5 = unionQuery(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, x), PropertyValueAtom(y, _p, z), TypeAtom(y, _D)),
                query(TypeAtom(_a, _C)),
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x1), PropertyValueAtom(_a, _r, y1), TypeAtom(_b,
                        _D))
        );

        // UCQ 6: B(a) & r(a,x) & p(y,z) & D(y) v B(a) & p(a,x) & r(a,y) & D(b) -> entailed
        UnionQuery ucq6 = unionQuery(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, x), PropertyValueAtom(y, _p, z), TypeAtom(y, _D)),
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x1), PropertyValueAtom(_a, _r, y1), TypeAtom(_b,
                        _D))
        );

        // UCQ 7: B(a) ^ r(a,x) ^ p(y,z) ^ D(y) v C(a) -> not entailed (_a can never be in _C)
        UnionQuery ucq7 = unionQuery(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, x), PropertyValueAtom(y, _p, z), TypeAtom(y, _D)),
                query(TypeAtom(_a, _C))
        );

        // UCQ 8: C(a) -> not entailed
        UnionQuery ucq8 = unionQuery(query(TypeAtom(_a, _C)));

        // UCQ 9: B(a) v C(a) -> entailed
        UnionQuery ucq9 = unionQuery(query(TypeAtom(_a, _B)), query(TypeAtom(_a, _C)));

        // UCQ 10: p(x,y) -> entailed
        UnionQuery ucq10 = unionQuery(query(PropertyValueAtom(x, _p, y)));

        // UCQ 11: p(x,y) -> not entailed
        UnionQuery ucq11 = unionQuery(query(PropertyValueAtom(x, _q, y)));

        testQuery(ucq1, true);
        testQuery(ucq2, false);
        testQuery(ucq3, true);
        testQuery(ucq4, true);
        testQuery(ucq5, true);
        testQuery(ucq6, true);
        testQuery(ucq7, false);
        testQuery(ucq8, false);
        testQuery(ucq9, true);
        testQuery(ucq10, true);
        testQuery(ucq11, false);
    }

    @Test
    public void testBooleanQueries2()
    {
        classes(_A, _B, _C);
        individuals(_a, _b);
        objectProperties(_r, _p);

        _kb.addSubClass(_A, not(and(_B, _C)));
        _kb.addType(_a, _A);

        UnionQuery ucq1 = unionQuery(query(TypeAtom(_a, not(_B))));
        UnionQuery ucq2 = unionQuery(query(TypeAtom(_a, not(_B))),
                                     query(TypeAtom(_a, not(_C))));
        UnionQuery ucq3 = unionQuery(query(TypeAtom(_a, not(_B))),
                                     query(TypeAtom(_a, not(_C))),
                                     query(TypeAtom(_b, _A)));

        testQuery(ucq1, false);
        testQuery(ucq2, true);
        testQuery(ucq3, true);
    }

    @Test
    public void testOedipus()
    {
        final ATermAppl Patricide = term("Patricide");
        final ATermAppl oedipus = term("oedipus");
        final ATermAppl iokaste = term("iokaste");
        final ATermAppl polyneikes = term("polyneikes");
        final ATermAppl thersandros = term("thersandros");
        final ATermAppl hasChild = term("hasChild");

        classes(Patricide);
        individuals(oedipus, iokaste, polyneikes, thersandros);
        objectProperties(hasChild);

        _kb.addPropertyValue(hasChild, iokaste, oedipus);
        _kb.addPropertyValue(hasChild, oedipus, polyneikes);
        _kb.addPropertyValue(hasChild, iokaste, polyneikes);
        _kb.addPropertyValue(hasChild, polyneikes, thersandros);
        _kb.addType(oedipus, Patricide);
        _kb.addType(thersandros, not(Patricide));

        UnionQuery ucq1 = unionQuery(query(TypeAtom(x, Patricide), PropertyValueAtom(iokaste, hasChild, x),
                TypeAtom(y, not(Patricide)), PropertyValueAtom(x, hasChild, y)));

        UnionQuery ucq2 = unionQuery(query(TypeAtom(oedipus, Patricide), PropertyValueAtom(iokaste, hasChild, oedipus),
                TypeAtom(polyneikes, not(Patricide)), PropertyValueAtom(oedipus, hasChild, polyneikes)));
        UnionQuery ucq3 = unionQuery(query(TypeAtom(polyneikes, Patricide), PropertyValueAtom(iokaste, hasChild,
                polyneikes), TypeAtom(thersandros, not(Patricide)), PropertyValueAtom(polyneikes, hasChild,
                thersandros)));
        UnionQuery ucq4 = unionQuery(
                query(TypeAtom(oedipus, Patricide), PropertyValueAtom(iokaste, hasChild, oedipus),
                        TypeAtom(polyneikes, not(Patricide)), PropertyValueAtom(oedipus, hasChild, polyneikes)),
                query(TypeAtom(polyneikes, Patricide), PropertyValueAtom(iokaste, hasChild, polyneikes),
                        TypeAtom(thersandros, not(Patricide)), PropertyValueAtom(polyneikes, hasChild,
                        thersandros))
        );
        UnionQuery ucq5 = unionQuery(query(TypeAtom(x, Patricide), PropertyValueAtom(z, hasChild, x),
                TypeAtom(y, not(Patricide)), PropertyValueAtom(x, hasChild, y)));

        testQuery(ucq1, true);
        testQuery(ucq2, false);
        testQuery(ucq3, false);
        testQuery(ucq4, true);
        testQuery(ucq5, true);
    }

    @Test
    public void sharedUndistinguishedVarTest()
    {
        _kb.addClass(_A);
        _kb.addClass(_B);
        _kb.addIndividual(_a);
        _kb.addIndividual(_b);
        _kb.addType(_a, _A);
        _kb.addType(_b, _B);

        // _kb.addSubClass(_A, or(_B, _C)); // TODO: this leads to SEVERE warning: Invalid _clash dependency

        UnionQuery ucq1 = unionQuery(query(TypeAtom(x, _A)),
                                     query(TypeAtom(x, _B)));
        UnionQuery ucq2 = unionQuery(query(TypeAtom(x, _A)),
                                     query(TypeAtom(y, _B)));
        UnionQuery ucq3 = unionQuery(query(TypeAtom(_a, _A)),
                                     query(TypeAtom(_b, _B)));
        UnionQuery ucq4 = unionQuery(query(TypeAtom(x, _A)),
                                     query(TypeAtom(_b, _B)));
        UnionQuery ucq5 = unionQuery(query(TypeAtom(_a, _B)));
        UnionQuery ucq6 = unionQuery(query(TypeAtom(_b, _A)));
        UnionQuery ucq7 = unionQuery(query(TypeAtom(_a, _B)),
                                     query(TypeAtom(_b, _A)));
        UnionQuery ucq8 = unionQuery(query(TypeAtom(_b, _A)),
                                     query(TypeAtom(_a, _B)));

        testQuery(ucq1, true);
        testQuery(ucq2, true);
        testQuery(ucq3, true);
        testQuery(ucq4, true);
        testQuery(ucq5, false);
        testQuery(ucq6, false);
        testQuery(ucq7, false);
        testQuery(ucq8, false);
    }

    @Test
    public void disjointDisjunctTest()
    {
        classes(_A, _B, _C, _D);
        individuals(_a, _b);
        objectProperties(_r);

        _kb.addSubClass(_A, or(_B, _C));
        _kb.addSubClass(_B, all(_r, _D));

        _kb.addType(_a, _A);
        _kb.addPropertyValue(_r, _a, _b);

        UnionQuery ucq1 = unionQuery(query(TypeAtom(x, _C)),
                                     query(TypeAtom(y, _D)));
        UnionQuery ucq2 = unionQuery(query(TypeAtom(_a, _C)),
                                     query(TypeAtom(_b, _D)));

        testQuery(ucq1, true);
        testQuery(ucq2, true);
    }

    @Test
    public void literalTest()
    {
        classes(_A, _B, _C);
        individuals(_a, _b);
        objectProperties(_r, _p);
        dataProperties(_q);

        _kb.addSubClass(_A, or(_B, _C));
        _kb.addType(_a, _A);
        _kb.addPropertyValue(_q, _a, literal(10));

        UnionQuery ucq1 = unionQuery(query(PropertyValueAtom(_a, _q, literal(10)), TypeAtom(_a, _A)));
        UnionQuery ucq2 = unionQuery(query(PropertyValueAtom(_a, _q, literal(10))));
        UnionQuery ucq3 = unionQuery(query(PropertyValueAtom(x, _q, literal(10))));
        UnionQuery ucq4 = unionQuery(query(TypeAtom(_a, _B), PropertyValueAtom(_a, _q, literal(9))),
                                     query(TypeAtom(_a, _C)));
        UnionQuery ucq5 = unionQuery(query(TypeAtom(_a, _B), PropertyValueAtom(_a, _q, literal(10))),
                                     query(TypeAtom(_a, _C)));

        testQuery(ucq1, true);
        testQuery(ucq2, true);
        try { testQuery(ucq3, true); } catch (UnsupportedOperationException ignored) {}
        testQuery(ucq4, false);
        testQuery(ucq5, true);
    }

    @Test
    public void cycleTest()
    {
        classes(_A, _B, _C);
        individuals(_a, _b);
        objectProperties(_p, _q, _r);

        UnionQuery ucq1 = unionQuery(query(PropertyValueAtom(x, _p, y), PropertyValueAtom(z, _q, y),
                PropertyValueAtom(z, _r, x)));
        UnionQuery ucq2 = unionQuery(query(PropertyValueAtom(x, _p, y), PropertyValueAtom(z, _q, y),
                PropertyValueAtom(_a, _r, x)));
        UnionQuery ucq3 = unionQuery(query(PropertyValueAtom(x, _p, y), PropertyValueAtom(z, _q, y)),
                query(PropertyValueAtom(y, _r, x)));
        UnionQuery ucq4 = unionQuery(query(PropertyValueAtom(x, _p, y), PropertyValueAtom(y, _q, z),
                PropertyValueAtom(z, _r, x)));
        UnionQuery ucq5 = unionQuery(query(PropertyValueAtom(x, _p, y), PropertyValueAtom(y, _q, z),
                PropertyValueAtom(z, _r, z)));
        UnionQuery ucq6 = unionQuery(query(PropertyValueAtom(x, _p, y), PropertyValueAtom(y, _q, z)));
        UnionQuery ucq7 = unionQuery(query(PropertyValueAtom(x, _p, y), PropertyValueAtom(y, _p, z),
                PropertyValueAtom(z, _p, x)));
        UnionQuery ucq8 = unionQuery(query(PropertyValueAtom(x1, _p, y1), PropertyValueAtom(y1, _p, z1),
                PropertyValueAtom(x, _p, y), PropertyValueAtom(y, _p, z), PropertyValueAtom(z, _p, x)));
        UnionQuery ucq9 = unionQuery(query(PropertyValueAtom(x, _p, _a), PropertyValueAtom(z, _q, _a),
                PropertyValueAtom(z, _r, x)));
        UnionQuery ucq10 = unionQuery(
                query(PropertyValueAtom(x, _p, _a), PropertyValueAtom(z, _q, _a), PropertyValueAtom(z, _r, x)),
                query(PropertyValueAtom(x, _p, y), PropertyValueAtom(z, _q, y), PropertyValueAtom(z, _r, x)));

        BooleanUnionQueryEngineSimple eng = new BooleanUnionQueryEngineSimple();
        assertFalse(eng.supports(ucq1));
        assertTrue(eng.supports(ucq2));
        assertTrue(eng.supports(ucq3));
        assertFalse(eng.supports(ucq4));
        assertFalse(eng.supports(ucq5));
        assertTrue(eng.supports(ucq6));
        assertFalse(eng.supports(ucq7));
        assertFalse(eng.supports(ucq8));
        assertTrue(eng.supports(ucq9));
        assertFalse(eng.supports(ucq10));
    }
}
