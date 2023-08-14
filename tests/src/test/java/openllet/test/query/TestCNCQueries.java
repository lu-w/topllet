package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.ABox;
import openllet.core.exceptions.InconsistentOntologyException;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

import static openllet.core.utils.TermFactory.*;
import static openllet.query.sparqldl.model.cq.QueryAtomFactory.PropertyValueAtom;
import static openllet.query.sparqldl.model.cq.QueryAtomFactory.TypeAtom;

public class TestCNCQueries extends AbstractQueryTest
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
    public void testSimpleQueries1()
    {
        setupKB1();
        CNCQQuery cncqq1 = cncqQuery(
            select(x, y, z),
            where(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _r, y)),
                negatedQuery(TypeAtom(x, _C), PropertyValueAtom(z, _r, _b))
            )
        );
        List<List<ATermAppl>> res = allResults(List.of(_a, _b, _c), 3, false);
        testQuery(cncqq1, res);
    }

    @Test
    public void testSimpleQueries2()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
            select(x),
            where(
                query(TypeAtom(_a, _C), PropertyValueAtom(_a, _r, x))
            )
        );
        testQuery(cncqq);
    }

    @Test
    public void testSimpleQueries3()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
            select(x),
            where(
                query(TypeAtom(x, _C))
            )
        );
        testQuery(cncqq, new ATermAppl[][] { {_b}, {_c} });
    }

    @Test
    public void testSimpleQueries4()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
            select(x, y),
            where(
                query(TypeAtom(x, _C)),
                negatedQuery(PropertyValueAtom(x, _r, y))
            )
        );
        testQuery(cncqq, new ATermAppl[][] { {_b, _a}, {_b, _b}, {_b, _c}, {_c, _a}, {_c, _b}, {_c, _c} });
    }

    @Test
    public void testSimpleQueries5()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
            select(x),
            where(
                query(TypeAtom(x, _A)),
                negatedQuery(TypeAtom(x, _C), PropertyValueAtom(x, _r, _b))
            )
        );
        testQuery(cncqq, new ATermAppl[][] { {_a}, {_c} });
    }

    @Test
    public void testVarOnlyInNegativePart()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                select(x, y),
                where(
                        query(TypeAtom(x, _A)),
                        negatedQuery(TypeAtom(y, _C), PropertyValueAtom(y, _r, _b))
                )
        );
        testQuery(cncqq, new ATermAppl[][] { {_a, _a}, {_a, _b}, {_a, _c}, {_c, _a}, {_c, _b}, {_c, _c} });
    }

    @Test
    public void testMultipleNegations1()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
            select(x),
            where(
                query(TypeAtom(x, _A)),
                query(TypeAtom(x, _B)),
                negatedQuery(TypeAtom(x, _C), PropertyValueAtom(x, _r, _b)),
                negatedQuery(TypeAtom(x, not(_D)), PropertyValueAtom(x, _r, _b))
            )
        );
        testQuery(cncqq, new ATermAppl[][] { {_c} });
    }

    @Test
    public void testMultipleNegations2()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
            select(x, y),
            where(
                query(TypeAtom(x, _A)),
                query(TypeAtom(x, _B)),
                query(TypeAtom(y, _A), PropertyValueAtom(y, _p, x)),
                negatedQuery(TypeAtom(x, _C), PropertyValueAtom(x, _r, _b)),
                negatedQuery(TypeAtom(x, not(_D)), PropertyValueAtom(x, _r, _b)),
                negatedQuery(TypeAtom(y, _D))
            )
        );
        testQuery(cncqq, new ATermAppl[][] { {_c, _a}, {_c, _c} });
    }

    @Test
    public void testMultipleNegations3()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                select(x, y, z),
                where(
                        query(TypeAtom(z, _E)),
                        query(TypeAtom(x, _A)),
                        query(TypeAtom(x, _B)),
                        query(TypeAtom(y, _A), PropertyValueAtom(y, _p, x)),
                        negatedQuery(TypeAtom(x, _C), PropertyValueAtom(x, _r, _b)),
                        negatedQuery(TypeAtom(x, not(_D)), PropertyValueAtom(x, _r, _b)),
                        negatedQuery(TypeAtom(y, _D))
                )
        );
        testQuery(cncqq, new ATermAppl[][] { {_c, _a, _a}, {_c, _c, _a}, {_c, _a, _b}, {_c, _c, _b}, {_c, _a, _c},
                {_c, _c, _c} });
    }

    @Test
    public void testUnconstrainedResultVar()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
                select(x, y, z),
                where(
                        query(TypeAtom(x, _A)),
                        query(TypeAtom(x, _B)),
                        query(TypeAtom(y, _A), PropertyValueAtom(y, _p, x)),
                        negatedQuery(TypeAtom(x, _C), PropertyValueAtom(x, _r, _b)),
                        negatedQuery(TypeAtom(x, not(_D)), PropertyValueAtom(x, _r, _b)),
                        negatedQuery(TypeAtom(y, _D))
                )
        );
        testQuery(cncqq, new ATermAppl[][] { {_c, _a, _a}, {_c, _c, _a}, {_c, _a, _b}, {_c, _c, _b}, {_c, _a, _c},
                {_c, _c, _c} });
    }

    @Test
    public void testUndistVars1()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
            select(x),
            where(
                query(TypeAtom(x, _A)),
                negatedQuery(TypeAtom(x, _C), PropertyValueAtom(x, _r, y))
            )
        );
        testQuery(cncqq, new ATermAppl[][] { {_a}, {_c} });
    }

    @Test
    public void testUndistVars2()
    {
        setupKB1();
        CNCQQuery cncqq = cncqQuery(
            select(x, y),
            where(
                query(TypeAtom(x, _A)),
                query(TypeAtom(x, _B)),
                query(TypeAtom(y, _A), PropertyValueAtom(y, _p, z)),
                negatedQuery(TypeAtom(x, _C), PropertyValueAtom(x, _r, _b)),
                negatedQuery(TypeAtom(x, not(_D)), PropertyValueAtom(x, _r, _b)),
                negatedQuery(TypeAtom(y, _D))
            )
        );
        testQuery(cncqq, new ATermAppl[][] { {_c, _a}, {_c, _c} });
    }

    @Test
    public void testNonUNA()
    {
        classes(_A, _B);
        individuals(_a, _b, _c);
        objectProperties(_r);

        _kb.addType(_b, not(_A));
        _kb.addType(_c, not(_A));
        _kb.addEquivalentClass(_A, value(_a));

        CNCQQuery cncqq = cncqQuery(
            select(x),
            where(
                query(TypeAtom(x, _A)),
                query(TypeAtom(y, _A))
            )
        );
        testQuery(cncqq, new ATermAppl[][] { {_a} });
    }

    @Test
    public void testInconsistentKB1()
    {
        individuals(_a, _b);
        classes(_A, _B);
        _kb.addType(_a, or(_A, not(_A)));

        CNCQQuery cncqq = cncqQuery(select(x), where(query(TypeAtom(x, _B))));

        testQuery(cncqq, new ATermAppl[][] { {_a}, {_b} });

        _kb.addType(_b, not(_B));

        testQuery(cncqq, new ATermAppl[][] { {_a} });
    }


    @Test
    public void testInconsistentKB2()
    {
        individuals(_a, _b);
        classes(_A, _B);
        _kb.addType(_a, and(_A, not(_A)));

        CNCQQuery cncqq = cncqQuery(select(x), where(query(TypeAtom(x, _B))));

        assertThrows(InconsistentOntologyException.class, () -> testQuery(cncqq));
    }

    @Test
    public void testMultipleExecsOnSameKB()
    {
        setupKB1();
        CNCQQuery cncqq1 = cncqQuery(
                select(x),
                where(
                        query(TypeAtom(x, _B), PropertyValueAtom(x, _r, y)),
                        negatedQuery(TypeAtom(x, _C), PropertyValueAtom(z, _r, _b))
                )
        );
        int indCount = _kb.getIndividualsCount();
        int clsCount = _kb.getClasses().size();
        int prpCount = _kb.getProperties().size();
        int ndsCount = _kb.getABox().size();
        List<List<ATermAppl>> res = List.of(List.of(_a), List.of(_b), List.of(_c));
        testQuery(cncqq1, res);
        assertEquals(_kb.getIndividualsCount(), indCount);
        assertEquals(_kb.getClasses().size(), clsCount);
        assertEquals(_kb.getProperties().size(), prpCount);
        assertEquals(_kb.getABox().size(), ndsCount + 1); // one anon node is created by the reasoner due to exist
        testQuery(cncqq1, res);
        assertEquals(_kb.getIndividualsCount(), indCount);
        assertEquals(_kb.getClasses().size(), clsCount);
        assertEquals(_kb.getProperties().size(), prpCount);
        assertEquals(_kb.getABox().size(), ndsCount + 1);
        testQuery(cncqq1, res);
        assertEquals(_kb.getIndividualsCount(), indCount);
        assertEquals(_kb.getClasses().size(), clsCount);
        assertEquals(_kb.getProperties().size(), prpCount);
        assertEquals(_kb.getABox().size(), ndsCount + 1);
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
            select(x, y, x1, y1),
            where(
                negatedQuery(TypeAtom(x, Patricide), PropertyValueAtom(iokaste, hasChild, x),
                    TypeAtom(y, not(Patricide)), PropertyValueAtom(x, hasChild, y)),
                negatedQuery(TypeAtom(x1, Patricide), PropertyValueAtom(iokaste, hasChild, x1),
                    TypeAtom(y1, not(Patricide)), PropertyValueAtom(x1, hasChild, y1))
            )
        );
        CNCQQuery cncqq2 = cncqQuery(
            select(x, y),
            where(
                negatedQuery(TypeAtom(x, Patricide), PropertyValueAtom(iokaste, hasChild, x),
                    TypeAtom(y, not(Patricide)), PropertyValueAtom(x, hasChild, y))
            )
        );
        CNCQQuery cncqq3 = cncqQuery(
            select(x, y),
            where(
                query(TypeAtom(x, Patricide), PropertyValueAtom(iokaste, hasChild, x),
                    TypeAtom(y, not(Patricide)), PropertyValueAtom(x, hasChild, y)),
                negatedQuery(PropertyValueAtom(y, hasChild, z))
            )
        );

        List<List<ATermAppl>> res1 = allResults(List.of(oedipus, iokaste, polyneikes, thersandros), 4, false);
        res1.removeAll(
            List.of(
                List.of(oedipus, polyneikes, polyneikes, thersandros),
                List.of(polyneikes, thersandros, oedipus, polyneikes)
            )
        );

        testQuery(cncqq1, res1);
        testQuery(cncqq2, allResults(List.of(oedipus, iokaste, polyneikes, thersandros), 2, false));
        testQuery(cncqq3, new ATermAppl[][]
            {
                {oedipus, thersandros},
                {iokaste, thersandros},
                {polyneikes, thersandros}
            }
        );
    }
}