package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.ucq.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static openllet.core.utils.TermFactory.*;
import static openllet.query.sparqldl.model.cq.QueryAtomFactory.PropertyValueAtom;
import static openllet.query.sparqldl.model.cq.QueryAtomFactory.TypeAtom;

public class TestUnionQueries extends AbstractQueryTest
{
    private void setupKB1()
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
    }

    @Test
    public void testUnionQueries1()
    {
        setupKB1();
        UnionQuery ucq1 = unionQuery(select(x), where(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _r, _b), TypeAtom(y, _D)),
                query(TypeAtom(y, _C), PropertyValueAtom(y, _r, _b))));
        testUnionQuery(ucq1, new ATermAppl[][]{{_a}});
    }

    @Test
    public void testUnionQueries2()
    {
        setupKB1();
        UnionQuery ucq2 = unionQuery(select(x, y), where(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _p, _b)),
                query(TypeAtom(y, _C), PropertyValueAtom(y, _r, _b))));
        testUnionQuery(ucq2);
    }

    @Test
    public void testUnionQueries3()
    {
        setupKB1();
        UnionQuery ucq3 = unionQuery(select(y, z), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x)),
                query(TypeAtom(z, _D), PropertyValueAtom(y, _r, z))));
        testUnionQuery(ucq3, allResults(List.of(_a, _b, _c), 2));
    }

    @Test
    public void testUnionQueries4()
    {
        setupKB1();
        UnionQuery ucq4 = unionQuery(select(x, y), where(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _p, z)),
                query(TypeAtom(z, _E), PropertyValueAtom(y, _r, z))));
        testUnionQuery(ucq4, new ATermAppl[][]{{_a, _a}, {_a, _b}, {_a, _c}});
    }

    @Test
    public void testUnionQueries5()
    {
        setupKB1();
        UnionQuery ucq5 = unionQuery(select(y1), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, x), PropertyValueAtom(y, _p, z), TypeAtom(y, _D)),
                query(TypeAtom(_a, _C)),
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x1), PropertyValueAtom(_a, _r, y1), TypeAtom(_b, _D)))
        );
        testUnionQuery(ucq5, new ATermAppl[][]{{_b}});
    }

    @Test
    public void testUnionQueries6()
    {
        setupKB1();
        UnionQuery ucq6 = unionQuery(select(x1, y1), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, x), PropertyValueAtom(y, _p, z), TypeAtom(y, _D)),
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x1), PropertyValueAtom(_a, _r, y1), TypeAtom(_b, _D)))
        );
        testUnionQuery(ucq6);
    }

    @Test
    public void testUnionQueries7()
    {
        setupKB1();
        UnionQuery ucq7 = unionQuery(select(x, y), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, x), PropertyValueAtom(y, _p, z)),
                query(TypeAtom(x, _D)))
        );
        testUnionQuery(ucq7, new ATermAppl[][]{{_b, _a}, {_b, _b}, {_b, _c}});
    }

    @Test
    public void testUnionQueries8()
    {
        setupKB1();
        UnionQuery ucq8 = unionQuery(select(x), where(query(TypeAtom(x, _C))));
        testUnionQuery(ucq8);
    }

    @Test
    public void testUnionQueries9()
    {
        setupKB1();
        UnionQuery ucq9 = unionQuery(select(x), where(query(TypeAtom(x, _A)), query(TypeAtom(x, _B))));
        testUnionQuery(ucq9, new ATermAppl[][]{{_a}});
    }

    @Test
    public void testUnionQueries10()
    {
        setupKB1();
        UnionQuery ucq10 = unionQuery(select(x), where(query(PropertyValueAtom(x, _p, y))));
        testUnionQuery(ucq10, new ATermAppl[][]{{_a}});
    }

    @Test
    public void testUnionQueries11()
    {
        setupKB1();
        UnionQuery ucq11 = unionQuery(select(x, y), where(query(PropertyValueAtom(x, _p, y))));
        testUnionQuery(ucq11);
    }

    @Test
    public void testUnionQueries12()
    {
        setupKB1();
        UnionQuery ucq12 = unionQuery(select(y, z), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, y)),
                query(TypeAtom(z, _D), PropertyValueAtom(y, _r, z))));
        testUnionQuery(ucq12, new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testUnionQueries13()
    {
        setupKB1();
        UnionQuery ucq3 = unionQuery(select(x, y, z), where(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _r, y), PropertyValueAtom(x1, _r, z)),
                query(TypeAtom(_a, _B)))
        );
        testUnionQuery(ucq3, allResults(List.of(_a, _b, _c), 3));
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

        UnionQuery ucq1 = unionQuery(select(x, y, x1, y1), where(
                query(TypeAtom(x, Patricide), PropertyValueAtom(iokaste, hasChild, x), TypeAtom(y, not(Patricide)),
                        PropertyValueAtom(x, hasChild, y)),
                query(TypeAtom(x1, Patricide), PropertyValueAtom(iokaste, hasChild, x1), TypeAtom(y1, not(Patricide)),
                        PropertyValueAtom(x1, hasChild, y1))));
        UnionQuery ucq2 = unionQuery(select(x, y), where(query(TypeAtom(x, Patricide), PropertyValueAtom(iokaste,
                        hasChild, x), TypeAtom(y, not(Patricide)), PropertyValueAtom(x, hasChild, y))));

        testUnionQuery(ucq1, new ATermAppl[][] {
                { oedipus, polyneikes, polyneikes, thersandros },
                { polyneikes, thersandros, oedipus, polyneikes }
        });
        testUnionQuery(ucq2);
    }

    @Test
    public void splitCNFQueryTest()
    {
        ATermAppl x2 = var("x2");
        CNFQuery q1 = new CNFQueryImpl(_kb, false);
        DisjunctiveQuery d1 = new DisjunctiveQueryImpl(_kb, false);
        DisjunctiveQuery d2 = new DisjunctiveQueryImpl(_kb, false);
        DisjunctiveQuery d3 = new DisjunctiveQueryImpl(_kb, false);
        DisjunctiveQuery d4 = new DisjunctiveQueryImpl(_kb, false);
        DisjunctiveQuery d5 = new DisjunctiveQueryImpl(_kb, false);
        DisjunctiveQuery d6 = new DisjunctiveQueryImpl(_kb, false);
        DisjunctiveQuery d7 = new DisjunctiveQueryImpl(_kb, false);
        DisjunctiveQuery d8 = new DisjunctiveQueryImpl(_kb, false);
        d1.add(TypeAtom(x, _A));
        d1.add(TypeAtom(y, _B));
        d1.addDistVar(x, Query.VarType.INDIVIDUAL);
        d1.addDistVar(y, Query.VarType.INDIVIDUAL);
        d1.addResultVar(x);
        d1.addResultVar(y);
        d2.add(TypeAtom(z, _C));
        d2.add(PropertyValueAtom(x, _r, z));
        d2.addDistVar(x, Query.VarType.INDIVIDUAL);
        d2.addDistVar(z, Query.VarType.INDIVIDUAL);
        d2.addResultVar(x);
        d2.addResultVar(z);
        d3.add(TypeAtom(y, _A));
        d3.addDistVar(y, Query.VarType.INDIVIDUAL);
        d3.addResultVar(y);
        d4.add(TypeAtom(x1, _B));
        d4.add(PropertyValueAtom(x1, _r, y1));
        d4.addDistVar(x1, Query.VarType.INDIVIDUAL);
        d4.addDistVar(y1, Query.VarType.INDIVIDUAL);
        d4.addResultVar(x1);
        d4.addResultVar(y1);
        d5.add(PropertyValueAtom(x1, _r, z1));
        d5.addDistVar(x1, Query.VarType.INDIVIDUAL);
        d5.addDistVar(z1, Query.VarType.INDIVIDUAL);
        d5.addResultVar(x1);
        d5.addResultVar(z1);
        d6.add(TypeAtom(x2, _A));
        d6.addDistVar(x2, Query.VarType.INDIVIDUAL);
        d6.addResultVar(x2);
        d7.add(TypeAtom(_a, _A));
        d8.add(TypeAtom(_b, _B));
        d8.add(TypeAtom(_c, _C));
        q1.addQuery(d1);
        q1.addQuery(d2);
        q1.addQuery(d3);
        q1.addQuery(d4);
        q1.addQuery(d5);
        q1.addQuery(d6);
        q1.addQuery(d7);
        q1.addQuery(d8);

        List<CNFQuery> splitQuery = q1.split();
        assertEquals(4, splitQuery.size());
    }

    // TODO Lukas: more test cases
}
