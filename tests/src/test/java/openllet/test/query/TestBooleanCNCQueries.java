package openllet.test.query;

import openllet.query.sparqldl.model.cncq.CNCQQuery;
import org.junit.Test;

import static openllet.core.utils.TermFactory.*;
import static openllet.core.utils.TermFactory.TOP;
import static openllet.query.sparqldl.model.cq.QueryAtomFactory.PropertyValueAtom;
import static openllet.query.sparqldl.model.cq.QueryAtomFactory.TypeAtom;

public class TestBooleanCNCQueries extends AbstractQueryTest
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

        CNCQQuery cncqq1 = cncqQuery(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, _b)),
                negatedQuery(TypeAtom(_a, _C), PropertyValueAtom(_a, _r, _b))
        );
        CNCQQuery cncqq2 = cncqQuery(
                query(TypeAtom(_a, _B)),
                negatedQuery(PropertyValueAtom(_a, _p, x))
        );
        CNCQQuery cncqq3 = cncqQuery(
                query(TypeAtom(_a, _C)),
                negatedQuery(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, _b))
        );
        CNCQQuery cncqq4 = cncqQuery(
                query(TypeAtom(_a, _E)),
                negatedQuery(TypeAtom(_b, _E), PropertyValueAtom(_b, _r, _c))
        );
        CNCQQuery cncqq5 = cncqQuery(
                query(TypeAtom(_a, _E))
        );

        testQuery(cncqq1, true);
        testQuery(cncqq2, false);
        testQuery(cncqq3, false);
        testQuery(cncqq4, true);
        testQuery(cncqq5, true);
    }

    // TODO Lukas: test undist. var
}