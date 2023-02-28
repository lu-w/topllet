package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import org.junit.Test;

import static openllet.core.utils.TermFactory.*;
import static openllet.query.sparqldl.model.cq.QueryAtomFactory.PropertyValueAtom;
import static openllet.query.sparqldl.model.cq.QueryAtomFactory.TypeAtom;

public class TestBooleanCNCQueries extends AbstractQueryTest
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
    public void testSimpleBooleanQueries1()
    {
        setupKB1();
        CNCQQuery cncqq1 = cncqQuery(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, _b)),
                negatedQuery(TypeAtom(_a, _C), PropertyValueAtom(_a, _r, _b))
        );
        testQuery(cncqq1, true);
    }

    @Test
    public void testSimpleBooleanQueries2()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                query(TypeAtom(_a, _B)),
                negatedQuery(PropertyValueAtom(_a, _p, x))
        );
        testQuery(cncqq, false);
    }

    @Test
    public void testSimpleBooleanQueries3()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                query(TypeAtom(_a, _C)),
                negatedQuery(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, _b))
        );
        testQuery(cncqq, false);
    }

    @Test
    public void testSimpleBooleanQueries4()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                query(TypeAtom(_a, _E)),
                negatedQuery(TypeAtom(_b, _E), PropertyValueAtom(_b, _r, _c))
        );
        testQuery(cncqq, true);
    }

    @Test
    public void testSimpleBooleanQueries5()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                query(TypeAtom(_a, _E))
        );
        testQuery(cncqq, true);
    }

    @Test
    public void testSimpleBooleanQueries6()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                negatedQuery(TypeAtom(_a, _E))
        );
        testQuery(cncqq, true);
    }

    @Test
    public void testSimpleBooleanQueries7()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                negatedQuery(TypeAtom(_a, _B))
        );
        testQuery(cncqq, false);
    }

    @Test
    public void testSimpleQueries8()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                select(),
                where(
                        query(TypeAtom(_a, _C))
                )
        );
        testQuery(cncqq, false);
    }

    @Test
    public void testUndistVarQueries1()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _r, y)),
                negatedQuery(TypeAtom(x1, _C), PropertyValueAtom(x1, _r, y1))
        );
        testQuery(cncqq, true);
    }

    @Test
    public void testUndistVarQueries2()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                query(TypeAtom(x, _C), PropertyValueAtom(x, _r, y)),
                negatedQuery(TypeAtom(x1, _C), PropertyValueAtom(x1, _r, y1))
        );
        testQuery(cncqq, false);
    }
    @Test
    public void testUndistVarQueries3()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _r, y)),
                negatedQuery(TypeAtom(x1, _C), PropertyValueAtom(x1, _r, y1))
        );
        testQuery(cncqq, true);
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

        CNCQQuery cncqq1 = cncqQuery(
                negatedQuery(TypeAtom(oedipus, Patricide), PropertyValueAtom(iokaste, hasChild, oedipus),
                        TypeAtom(polyneikes, not(Patricide)), PropertyValueAtom(oedipus, hasChild, polyneikes)),
                negatedQuery(TypeAtom(polyneikes, Patricide), PropertyValueAtom(iokaste, hasChild, polyneikes),
                        TypeAtom(thersandros, not(Patricide)), PropertyValueAtom(polyneikes, hasChild, thersandros))
        );
        CNCQQuery cncqq2 = cncqQuery(
                query(TypeAtom(oedipus, Patricide), PropertyValueAtom(iokaste, hasChild, oedipus),
                        TypeAtom(polyneikes, not(Patricide)), PropertyValueAtom(oedipus, hasChild, polyneikes)),
                negatedQuery(TypeAtom(polyneikes, Patricide), PropertyValueAtom(iokaste, hasChild, polyneikes),
                        TypeAtom(thersandros, not(Patricide)), PropertyValueAtom(polyneikes, hasChild, thersandros))
        );
        CNCQQuery cncqq3 = cncqQuery(
                query(TypeAtom(oedipus, Patricide), PropertyValueAtom(iokaste, hasChild, oedipus),
                        TypeAtom(polyneikes, not(Patricide)), PropertyValueAtom(oedipus, hasChild, polyneikes))
        );
        CNCQQuery cncqq4 = cncqQuery(
                query(TypeAtom(oedipus, Patricide), PropertyValueAtom(iokaste, hasChild, oedipus),
                        TypeAtom(polyneikes, not(Patricide)), PropertyValueAtom(oedipus, hasChild, polyneikes)),
                negatedQuery(PropertyValueAtom(iokaste, hasChild, polyneikes))
        );
        // Interesting case: both sub-queries of cncqq1 are satisfiable, but cncqq1 itself is not satisfiable.
        CNCQQuery cncqq5 = cncqQuery(
                negatedQuery(TypeAtom(oedipus, Patricide), PropertyValueAtom(iokaste, hasChild, oedipus),
                        TypeAtom(polyneikes, not(Patricide)), PropertyValueAtom(oedipus, hasChild, polyneikes))
        );
        CNCQQuery cncqq6 = cncqQuery(
                negatedQuery(TypeAtom(polyneikes, Patricide), PropertyValueAtom(iokaste, hasChild, polyneikes),
                        TypeAtom(thersandros, not(Patricide)), PropertyValueAtom(polyneikes, hasChild, thersandros))
        );
        testQuery(cncqq1, false);
        testQuery(cncqq2, true);
        testQuery(cncqq3, true);
        testQuery(cncqq4, false);
        testQuery(cncqq5, true);
        testQuery(cncqq6, true);
    }

    @Test
    public void testInconsistentKB()
    {
        individuals(_a, _b);
        classes(_A, _B);
        _kb.addType(_a, or(_A, not(_A)));

        CNCQQuery cncqq = cncqQuery(query(TypeAtom(_b, _B)));

        testQuery(cncqq, true);

        _kb.addType(_b, not(_B));

        testQuery(cncqq, false);
    }

    @Test
    public void testUndistVarOnlyInObjectProperty()
    {
        // PropertyValue(?_undist_y0, listedCourse, Publication3) , Type(UndergraduateStudent416, Chair) , Type(?_undist_y0, GraduateCourse).
        // TODO Lukas: test case for undist var at pos. 1 of a property
        individuals(_a, _b);
        classes(_A, _B, _C);
        objectProperties(_r);
        _kb.addType(_a, _A);

        CNCQQuery cncqq = cncqQuery(query(PropertyValueAtom(x, _r, _a), TypeAtom(_b, _B), TypeAtom(x, _C)));

        testQuery(cncqq, true);
    }
}