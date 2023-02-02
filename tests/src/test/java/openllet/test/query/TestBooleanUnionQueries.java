package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.engine.UnionQueryEngine;
import openllet.query.sparqldl.model.*;
import openllet.test.AbstractKBTests;
import org.junit.Test;

import java.util.Arrays;

import static openllet.core.utils.TermFactory.*;
import static openllet.core.utils.TermFactory.TOP;
import static openllet.query.sparqldl.model.QueryAtomFactory.PropertyValueAtom;
import static openllet.query.sparqldl.model.QueryAtomFactory.TypeAtom;
import static org.junit.Assert.assertEquals;

public class TestBooleanUnionQueries extends AbstractKBTests
{
    private static final ATermAppl _x = var("x");
    private static final ATermAppl _y = var("y");
    private static final ATermAppl _z = var("z");

    private Query query(final QueryAtom... atoms)
    {
        final Query q = new QueryImpl(_kb, true);
        for (final QueryAtom atom : atoms)
            q.add(atom);
        return q;
    }

    private UnionQuery unionQuery(final Query... queries)
    {
        final UnionQuery q = new UnionQueryImpl(_kb, true);
        q.setQueries(Arrays.stream(queries).toList());
        return q;
    }

    private static void testBooleanABoxQuery(final boolean expected, final UnionQuery query)
    {
        UnionQueryEngine engine = new UnionQueryEngine();
        assertEquals(expected, engine.execBooleanABoxQuery(query));
    }

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

        // UCQ 1: B(x) ^ r(x,b) v C(x) ^ r(x,b) -> is entailed but second disjunct can never be true
        UnionQuery ucq1 = unionQuery(query(TypeAtom(_x, _B), PropertyValueAtom(_x, _r, _b)),
                                     query(TypeAtom(_x, _C), PropertyValueAtom(_x, _r, _b)));

        // UCQ 2: B(x) ^ p(x,b) v C(x) ^ r(x,b) -> not entailed
        UnionQuery ucq2 = unionQuery(query(TypeAtom(_x, _B), PropertyValueAtom(_x, _p, _b)),
                                     query(TypeAtom(_x, _C), PropertyValueAtom(_x, _r, _b)));

        // UCQ 3: B(a) ^ r(a,x) v D(z) ^ r(y,z) -> entailed (both disjuncts)
        UnionQuery ucq3 = unionQuery(query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, _x)),
                                     query(TypeAtom(_z, _D), PropertyValueAtom(_y, _r, _z)));

        // UCQ 4: B(a) ^ r(a,x) v E(z) ^ r(y,z) -> entailed (only first disjunct)
        UnionQuery ucq4 = unionQuery(query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, _x)),
                                     query(TypeAtom(_z, _E), PropertyValueAtom(_y, _r, _z)));

        // UCQ 5: B(a) ^ r(a,x) ^ p(y,z) ^ D(y) v C(a) v B(a) ^ p(a,x) ^ r(a,y) ^ D(b) -> entailed
        UnionQuery ucq5 = unionQuery(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, _x), PropertyValueAtom(_y, _p, _z), TypeAtom(_y, _D)),
                query(TypeAtom(_a, _C)),
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, _x), PropertyValueAtom(_a, _r, _y), TypeAtom(_b, _D))
        );

        // UCQ 6: B(a) ^ r(a,x) ^ p(y,z) ^ D(y) v B(a) ^ p(a,x) ^ r(a,y) ^ D(b) -> entailed
        UnionQuery ucq6 = unionQuery(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, _x), PropertyValueAtom(_y, _p, _z), TypeAtom(_y, _D)),
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, _x), PropertyValueAtom(_a, _r, _y), TypeAtom(_b, _D))
        );

        // UCQ 7: B(a) ^ r(a,x) ^ p(y,z) ^ D(y) v C(a) -> not entailed (_a can never be in _C)
        UnionQuery ucq7 = unionQuery(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, _x), PropertyValueAtom(_y, _p, _z), TypeAtom(_y, _D)),
                query(TypeAtom(_a, _C))
        );

        // UCQ 8: C(a) -> not entailed
        UnionQuery ucq8 = unionQuery(query(TypeAtom(_a, _C)));

        // UCQ 9: B(a) v C(a) -> entailed
        UnionQuery ucq9 = unionQuery(query(TypeAtom(_a, _B)), query(TypeAtom(_a, _C)));

        // UCQ 10: p(x,y) -> entailed
        UnionQuery ucq10 = unionQuery(query(PropertyValueAtom(_x, _p, _y)));

        // UCQ 11: p(x,y) -> not entailed
        UnionQuery ucq11 = unionQuery(query(PropertyValueAtom(_x, _q, _y)));

        testBooleanABoxQuery(true, ucq1);
        testBooleanABoxQuery(false, ucq2);
        testBooleanABoxQuery(true, ucq3);
        testBooleanABoxQuery(true, ucq4);
        testBooleanABoxQuery(true, ucq5);
        testBooleanABoxQuery(true, ucq6);
        testBooleanABoxQuery(false, ucq7);
        testBooleanABoxQuery(false, ucq8);
        testBooleanABoxQuery(true, ucq9);
        testBooleanABoxQuery(true, ucq10);
        testBooleanABoxQuery(false, ucq11);
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

        testBooleanABoxQuery(false, ucq1);
        testBooleanABoxQuery(true, ucq2);
        testBooleanABoxQuery(true, ucq3);
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

        UnionQuery ucq1 = unionQuery(query(TypeAtom(_x, Patricide), PropertyValueAtom(iokaste, hasChild, _x),
                TypeAtom(_y, not(Patricide)), PropertyValueAtom(_x, hasChild, _y)));

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
        UnionQuery ucq5 = unionQuery(query(TypeAtom(_x, Patricide), PropertyValueAtom(_z, hasChild, _x),
                TypeAtom(_y, not(Patricide)), PropertyValueAtom(_x, hasChild, _y)));

        testBooleanABoxQuery(true, ucq1);
        testBooleanABoxQuery(false, ucq2);
        testBooleanABoxQuery(false, ucq3);
        testBooleanABoxQuery(true, ucq4);
        testBooleanABoxQuery(true, ucq5);
    }

    @Test
    public void sharedUndistinguishedVarTest()
    {
        classes(_A, _B, _C);
        individuals(_a, _b);

        _kb.addType(_a, _A);
        _kb.addType(_b, _B);

        // _kb.addSubClass(_A, or(_B, _C)); // TODO Lukas: this leads to SEVERE warning: Invalid _clash dependency

        UnionQuery ucq1 = unionQuery(query(TypeAtom(_x, _A)),
                                     query(TypeAtom(_x, _B)));
        UnionQuery ucq2 = unionQuery(query(TypeAtom(_x, _A)),
                                     query(TypeAtom(_y, _B)));
        UnionQuery ucq3 = unionQuery(query(TypeAtom(_a, _A)),
                                     query(TypeAtom(_b, _B)));
        UnionQuery ucq4 = unionQuery(query(TypeAtom(_x, _A)),
                                     query(TypeAtom(_b, _B)));
        UnionQuery ucq5 = unionQuery(query(TypeAtom(_b, _A)),
                                     query(TypeAtom(_a, _B)));

        testBooleanABoxQuery(true, ucq1);
        testBooleanABoxQuery(true, ucq2);
        testBooleanABoxQuery(true, ucq3);
        testBooleanABoxQuery(true, ucq4);
        testBooleanABoxQuery(false, ucq5);
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

        UnionQuery ucq1 = unionQuery(query(TypeAtom(_x, _C)),
                                     query(TypeAtom(_y, _D)));
        UnionQuery ucq2 = unionQuery(query(TypeAtom(_a, _C)),
                                     query(TypeAtom(_b, _D)));

        testBooleanABoxQuery(true, ucq1);
        testBooleanABoxQuery(true, ucq2);
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
        UnionQuery ucq3 = unionQuery(query(PropertyValueAtom(_x, _q, literal(10))));
        UnionQuery ucq4 = unionQuery(query(TypeAtom(_a, _B), PropertyValueAtom(_a, _q, literal(9))),
                                     query(TypeAtom(_a, _C)));
        UnionQuery ucq5 = unionQuery(query(TypeAtom(_a, _B), PropertyValueAtom(_a, _q, literal(10))),
                                     query(TypeAtom(_a, _C)));

        testBooleanABoxQuery(true, ucq1);
        testBooleanABoxQuery(true, ucq2);
        try { testBooleanABoxQuery(true, ucq3); } catch (UnsupportedOperationException ignored) {}
        testBooleanABoxQuery(false, ucq4);
        testBooleanABoxQuery(true, ucq5);
    }

    @Test
    public void simpleDistinguishedVariableTest()
    {
        // TODO but only once interface for this is done
    }
}
