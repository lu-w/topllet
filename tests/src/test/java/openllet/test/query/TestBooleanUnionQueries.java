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
    public void testBooleanQueries()
    {
        classes(_A, _B, _C, _D, _E);
        individuals(_a, _b, _c);
        objectProperties(_r, _p);

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

        testBooleanABoxQuery(true, ucq1);
        testBooleanABoxQuery(false, ucq2);
        testBooleanABoxQuery(true, ucq3);
        testBooleanABoxQuery(true, ucq4);
    }

    // TODO Lukas: some more test cases for UCQs here...
}
