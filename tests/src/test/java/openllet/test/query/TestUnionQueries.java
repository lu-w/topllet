package openllet.test.query;

import openllet.aterm.ATermAppl;
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
        testQuery(ucq1, new ATermAppl[][]{{_a}});
    }

    @Test
    public void testUnionQueries2()
    {
        setupKB1();
        UnionQuery ucq2 = unionQuery(select(x, y), where(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _p, _b)),
                query(TypeAtom(y, _C), PropertyValueAtom(y, _r, _b))));
        testQuery(ucq2);
    }

    @Test
    public void testUnionQueries3()
    {
        setupKB1();
        UnionQuery ucq3 = unionQuery(select(y, z), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x)),
                query(TypeAtom(z, _D), PropertyValueAtom(y, _r, z))));
        testQuery(ucq3, allResults(List.of(_a, _b, _c), 2));
    }

    @Test
    public void testUnionQueries4()
    {
        setupKB1();
        UnionQuery ucq4 = unionQuery(select(x, y), where(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _p, z)),
                query(TypeAtom(z, _E), PropertyValueAtom(y, _r, z))));
        testQuery(ucq4, new ATermAppl[][]{{_a, _a}, {_a, _b}, {_a, _c}});
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
        testQuery(ucq5, new ATermAppl[][]{{_b}});
    }

    @Test
    public void testUnionQueries6()
    {
        setupKB1();
        UnionQuery ucq6 = unionQuery(select(x1, y1), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, x), PropertyValueAtom(y, _p, z), TypeAtom(y, _D)),
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x1), PropertyValueAtom(_a, _r, y1), TypeAtom(_b, _D)))
        );
        testQuery(ucq6);
    }

    @Test
    public void testUnionQueries7()
    {
        setupKB1();
        UnionQuery ucq7 = unionQuery(select(x, y), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, x), PropertyValueAtom(y, _p, z)),
                query(TypeAtom(x, _D)))
        );
        testQuery(ucq7, new ATermAppl[][]{{_b, _a}, {_b, _b}, {_b, _c}});
    }

    @Test
    public void testUnionQueries8()
    {
        setupKB1();
        UnionQuery ucq8 = unionQuery(select(x), where(query(TypeAtom(x, _C))));
        testQuery(ucq8);
    }

    @Test
    public void testUnionQueries9()
    {
        setupKB1();
        UnionQuery ucq9 = unionQuery(select(x), where(query(TypeAtom(x, _A)), query(TypeAtom(x, _B))));
        testQuery(ucq9, new ATermAppl[][]{{_a}});
    }

    @Test
    public void testUnionQueries10()
    {
        setupKB1();
        UnionQuery ucq10 = unionQuery(select(x), where(query(PropertyValueAtom(x, _p, y))));
        testQuery(ucq10, new ATermAppl[][]{{_a}});
    }

    @Test
    public void testUnionQueries11()
    {
        setupKB1();
        UnionQuery ucq11 = unionQuery(select(x, y), where(query(PropertyValueAtom(x, _p, y))));
        testQuery(ucq11);
    }

    @Test
    public void testUnionQueries12()
    {
        setupKB1();
        UnionQuery ucq12 = unionQuery(select(y, z), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, y)),
                query(TypeAtom(z, _D), PropertyValueAtom(y, _r, z))));
        testQuery(ucq12, new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testUnionQueries13()
    {
        setupKB1();
        UnionQuery ucq3 = unionQuery(select(x, y, z), where(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _r, y), PropertyValueAtom(x1, _r, z)),
                query(TypeAtom(_a, _B)))
        );
        testQuery(ucq3, allResults(List.of(_a, _b, _c), 3));
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

        testQuery(ucq1, new ATermAppl[][] {
                { oedipus, polyneikes, polyneikes, thersandros },
                { polyneikes, thersandros, oedipus, polyneikes }
        });
        testQuery(ucq2);
    }

    @Test
    public void splitCNFQueryTest()
    {
        ATermAppl x2 = var("x2");
        CNFQuery q1 = cnfQuery(select(x, y, z, x1, y1, x2),
                where(
                    disjunctiveQuery(TypeAtom(x, _A), TypeAtom(y, _B)),
                    disjunctiveQuery(TypeAtom(z, _C), PropertyValueAtom(x, _r, z)),
                    disjunctiveQuery(TypeAtom(y, _A)),
                    disjunctiveQuery(TypeAtom(x1, _B)),
                    disjunctiveQuery(PropertyValueAtom(x1, _r, z1)),
                    disjunctiveQuery(TypeAtom(x2, _A)),
                    disjunctiveQuery(TypeAtom(_a, _A)),
                    disjunctiveQuery(TypeAtom(_b, _B), TypeAtom(_c, _C))
                )
        );

        CNFQuery q2 = cnfQuery(select(x, y, z),
                where(
                        disjunctiveQuery(TypeAtom(x, _A), TypeAtom(y, _B)),
                        disjunctiveQuery(TypeAtom(z, _C), PropertyValueAtom(x, _r, z)),
                        disjunctiveQuery(TypeAtom(y, _A))
                )
        );

        CNFQuery q3 = cnfQuery(select(x, y, z, x1, y1, x2),
                where(
                        disjunctiveQuery(TypeAtom(_a, _A), TypeAtom(_b, _B)),
                        disjunctiveQuery(TypeAtom(z, _C), PropertyValueAtom(x, _r, z)),
                        disjunctiveQuery(TypeAtom(y, _A))
                )
        );

        CNFQuery q4 = cnfQuery(
                disjunctiveQuery(TypeAtom(_a, _A), TypeAtom(_b, _B)),
                disjunctiveQuery(TypeAtom(_c, _C)),
                disjunctiveQuery(PropertyValueAtom(_a, _r, _b))
        );

        List<CNFQuery> q1Split = q1.split();
        assertEquals(4, q1Split.size());
        assertEquals(q1Split.get(0).getResultVars(), List.of(x1));
        assertEquals(q1Split.get(0).getDistVars(), Set.of(x1));
        assertEquals(q1Split.get(0).getUndistVars(), Set.of(z1));
        assertEquals(q1Split.get(1).getResultVars(), List.of(x2));
        assertEquals(q1Split.get(1).getDistVars(), Set.of(x2));
        assertEquals(q1Split.get(2).getResultVars(), List.of(x, y, z));
        assertEquals(q1Split.get(2).getDistVars(), Set.of(x, y, z));
        assertEquals(q1Split.get(3).getResultVars(), List.of());
        assertEquals(q1Split.get(3).getDistVars(), Set.of());
        assertEquals(1, q2.split().size());
        assertEquals(3, q3.split().size());
        assertEquals(1, q4.split().size());
    }

    // TODO Lukas: more test cases (also comparing after and before CNF binding engines results)
}
