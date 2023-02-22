package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.cncq.CNCQQuery;
import org.junit.Test;

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
        List<List<ATermAppl>> res = allResults(cncqq1, List.of(_a, _b, _c), 3);
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
}