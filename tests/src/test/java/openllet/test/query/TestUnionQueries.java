package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.engine.SimpleBooleanUnionQueryEngine;
import openllet.query.sparqldl.engine.SimpleUnionQueryEngine;
import openllet.query.sparqldl.engine.UnionQueryExec;
import openllet.query.sparqldl.model.*;
import openllet.test.AbstractKBTests;
import org.junit.Test;

import java.util.Arrays;

import static openllet.core.utils.TermFactory.*;
import static openllet.query.sparqldl.model.QueryAtomFactory.PropertyValueAtom;
import static openllet.query.sparqldl.model.QueryAtomFactory.TypeAtom;
import static org.junit.Assert.*;

public class TestUnionQueries extends AbstractKBTests
{
    @Test
    public void testUnionQueries1()
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

        // TODO Lukas
    }
}
