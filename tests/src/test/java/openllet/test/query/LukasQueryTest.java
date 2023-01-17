package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.engine.SimpleQueryEngine;
import openllet.query.sparqldl.model.*;
import org.apache.jena.graph.compose.Union;
import org.junit.Test;

import java.util.List;

import static openllet.core.utils.TermFactory.*;
import static openllet.query.sparqldl.model.QueryAtomFactory.PropertyValueAtom;
import static openllet.query.sparqldl.model.QueryAtomFactory.TypeAtom;

public class LukasQueryTest extends AbstractQueryTest
{
    @Test
    public void test1()
    {
        classes(_A, _B, _C);
        objectProperties(_p);
        individuals(_a, _b, _c);

        _kb.addType(_a, _A);
        _kb.addSubClass(_A, some(_p, _B));
        _kb.addSubClass(_B, some(_p, _C));
        _kb.addSubClass(_C, some(_p, oneOf(_a)));

        final Query q = new QueryImpl(_kb, true);

        q.addResultVar(x);
        q.addDistVar(x, UnionQuery.VarType.INDIVIDUAL);

        q.add(PropertyValueAtom(x, _p, y));
        q.add(PropertyValueAtom(y, _p, z));
        q.add(PropertyValueAtom(z, _p, x));

        System.out.println(q.getUndistVars());

        testQuery(q, new ATermAppl[][] { { _a } });
    }

    @Test
    public void test2()
    {
        // level 1: acyclic binary query w/o undist. vars
        // but roles, so it should be rolled up


        classes(_A, _B, _C);
        objectProperties(_p);
        individuals(_a, _b, _c);

        _kb.addType(_a, _A);
        _kb.addSubClass(_A, all(_p, _B));

        final Query q = new QueryImpl(_kb, true);

        q.add(TypeAtom(_a, _A));
        q.add(PropertyValueAtom(_a, _p, _b));

        QueryResult res = new SimpleQueryEngine().execABoxQuery(q);

        System.out.println(res);
    }

    @Test
    public void test3()
    {
        classes(_A, _B, _C);
        objectProperties(_p, _q, _r);
        individuals(_a);

        _kb.addType(_a, _A);

        _kb.addSubClass(_A, some(_p, _B));
        _kb.addSubClass(_B, some(_p, _C));
        _kb.addSubClass(_C, some(_p, _A));

        final Query q = new QueryImpl(_kb, true);

        ATermAppl _x = var("x");
        ATermAppl _y = var("y");
        ATermAppl _z = var("z");
        q.add(PropertyValueAtom(_x, _p, _y));
        q.add(PropertyValueAtom(_y, _q, _z));
        q.add(PropertyValueAtom(_z, _r, _x));

        System.out.println(q.getUndistVars());

        testQuery(q, new ATermAppl[][] { {  } });
    }

    @Test
    public void test4()
    {
        classes(_A, _B, _C, _D);
        objectProperties(_p, _q, _r);
        individuals(_a);
        individuals(_b);
        individuals(_c);

        _kb.addType(_a, _A);
        _kb.addType(_b, _B);
        _kb.addType(_c, _A);
        _kb.addPropertyValue(_p, _a, _b);
        _kb.addPropertyValue(_p, _a, _c);
        _kb.addPropertyValue(_p, _b, _c);

        _kb.addSubClass(_A, _C);
        _kb.addSubClass(_B, _D);

        final Query q = new QueryImpl(_kb, true);

        q.addResultVar(x);
        q.addResultVar(y);
        q.addDistVar(x, UnionQuery.VarType.INDIVIDUAL);
        q.addDistVar(y, UnionQuery.VarType.INDIVIDUAL);

        q.add(TypeAtom(x, _C));
        q.add(TypeAtom(y, _D));
        q.add(PropertyValueAtom(x, _p, y));

        testQuery(q, new ATermAppl[][] { { _a, _b } });
    }
    @Test
    public void cnfTest()
    {
        UnionQueryImpl ucq = new UnionQueryImpl(_kb, true);

        final Query q1 = new QueryImpl(_kb, true);
        q1.addResultVar(x);
        q1.addResultVar(y);
        q1.add(TypeAtom(x, _C));
        q1.add(TypeAtom(y, _D));

        final Query q2 = new QueryImpl(_kb, true);
        q2.addResultVar(x);
        q2.addResultVar(y);
        q2.add(TypeAtom(x, _E));
        q2.add(TypeAtom(y, _F));

        final Query q3 = new QueryImpl(_kb, true);
        q3.addResultVar(x);
        q3.addResultVar(y);
        q3.add(TypeAtom(x, _G));
        q3.add(TypeAtom(y, _H));

        ucq.add(q1);
        ucq.add(q2);
        ucq.add(q3);


        System.out.println(ucq.toString(true));

        List<UnionQuery> cnfUcq = ucq.toCNF();

        System.out.println(cnfUcq);
    }
}
