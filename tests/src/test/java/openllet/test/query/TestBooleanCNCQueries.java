package openllet.test.query;

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
    public void testUndistVarQueries1()
    {
        setupKB1();
        CNCQQuery cncqq1 = cncqQuery(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _r, y)),
                negatedQuery(TypeAtom(x1, _C), PropertyValueAtom(x1, _r, y1))
        );
        testQuery(cncqq1, true);
    }

    @Test
    public void testUndistVarQueries2()
    {
        setupKB1();
        CNCQQuery cncqq1 = cncqQuery(
                query(TypeAtom(x, _C), PropertyValueAtom(x, _r, y)),
                negatedQuery(TypeAtom(x1, _C), PropertyValueAtom(x1, _r, y1))
        );
        testQuery(cncqq1, false);
    }
    @Test
    public void testUndistVarQueries3()
    {
        setupKB1();
        CNCQQuery cncqq1 = cncqQuery(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _r, y)),
                negatedQuery(TypeAtom(x1, _C), PropertyValueAtom(x1, _r, y1))
        );
        testQuery(cncqq1, true);
    }
}