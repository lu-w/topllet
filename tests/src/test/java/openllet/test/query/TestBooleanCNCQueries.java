package openllet.test.query;

import openllet.query.sparqldl.model.cncq.CNCQQuery;
import org.junit.Test;

import static openllet.core.utils.TermFactory.*;
import static openllet.core.utils.TermFactory.TOP;
import static openllet.query.sparqldl.model.cq.QueryAtomFactory.PropertyValueAtom;
import static openllet.query.sparqldl.model.cq.QueryAtomFactory.TypeAtom;
import static org.junit.Assert.*;

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

        CNCQQuery cncqq1 = cncqQuery(query(TypeAtom(x, _B), PropertyValueAtom(x, _r, _b)),
                query(TypeAtom(y, _C), PropertyValueAtom(y, _r, _b)));

        testCNCQQuery(cncqq1, true);
    }
}