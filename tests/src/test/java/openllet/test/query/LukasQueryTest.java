package openllet.test.query;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.taxonomy.TaxonomyUtils;
import openllet.core.utils.Pair;
import openllet.query.sparqldl.engine.QueryEngine;
import openllet.query.sparqldl.engine.SimpleQueryEngine;
import openllet.query.sparqldl.model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
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

        ucq.addQuery(q1);
        ucq.addQuery(q2);
        ucq.addQuery(q3);

        System.out.println(ucq.toString(true));

        List<DisjunctiveQuery> cnfUcq = ucq.toCNF();

        System.out.println(cnfUcq);
    }

    @Test
    public void simpleUCQTest()
    {
        classes(_A, _B, _C, _D);
        _kb.addSubClass(_A, or(_B, _C));
        individuals(_a);

        _kb.addType(_a, _A);

        List<Pair<ATermAppl, ATermAppl>> q = new ArrayList<>();
        q.add(new Pair<>(_a, _B));
        q.add(new Pair<>(_a, _C));
        q.add(new Pair<>(_a, _D));

        System.out.println("Checking consistency (should be: true)!");

        boolean isCons1 = _kb.isType(q);

        System.out.println("Result:");
        System.out.println(isCons1);

        System.out.println("Checking consistency (should be: false)!");
        q.remove(1);
        boolean isCons2 = _kb.isType(q);

        System.out.println("Result:");
        System.out.println(isCons2);
    }

    @Test
    public void disjQueryEngineTest()
    {
        classes(_A, _B, _C, _D);
        _kb.addSubClass(_A, or(_B, _C));
        individuals(_a, _b, _c);

        _kb.addType(_a, _A);
        _kb.addPropertyValue(_r, _a, _b);

        final UnionQuery q = new UnionQueryImpl(_kb, false);

        final Query q1 = new QueryImpl(_kb, true); // unsat
        final Query q2 = new QueryImpl(_kb, true); // sat
        q1.add(TypeAtom(_a, _C));
        q1.add(TypeAtom(_c, _D));
        q2.add(TypeAtom(_b, _C));
        q2.add(PropertyValueAtom(_r, _a, _b));

        q.addQuery(q1);
        q.addQuery(q2);

        QueryResult res = QueryEngine.exec(q);

        System.out.println(res);
    }
}
