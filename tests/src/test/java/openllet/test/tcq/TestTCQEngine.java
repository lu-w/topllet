package openllet.test.tcq;

import openllet.aterm.ATermAppl;
import org.junit.Test;

import java.util.List;

public class TestTCQEngine extends AbstractTCQTest
{
    @Test
    public void testSimpleQuery1()
    {
        simpleTKB();
        testQuery("G(A(?x))", new ATermAppl[][] { { _a } });
        testQuery("G(A(?x) ^ B(?y)) & F(r(?x,?y))", new ATermAppl[][] { { _a, _b } });
        testQuery("G((A(?x)) & (B(?y))) & F(r(?x,?y))", new ATermAppl[][] { { _a, _b } });
        testQuery("G(A(?x) ^ B(?y)) & F!(r(?x,?y))");
        testQuery("G(A(?x) ^ B(?y)) & G!(r(?x,?y))");
        testQuery("G((A(?x)) & (B(?y))) & G!(r(?x,?y))");
        testQuery("G(A(?x)) & G(r(?x,?y))", new ATermAppl[][] { { _a, _b } });
        testQuery("G(A(?x)) & F(p(?x,?y))");
        testQuery("G(A(?x)) & F(r(?x,?y))", new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testSimpleQuery2()
    {
        simpleTKB();
        testQuery("(A(?x)) U (B(?y))", new ATermAppl[][] { { _a, _b }, { _b, _b }, { _c, _b } });
        testQuery("((A(?x)) U (B(?y))) | F(C(?y))", new ATermAppl[][] { { _a, _b }, { _b, _b }, { _c, _b } });
        testQuery("((A(?x)) U (B(?y))) | X(C(?y))", new ATermAppl[][] { { _a, _b }, { _b, _b }, { _c, _b } });
    }

    @Test
    public void testSimpleQuery3()
    {
        simpleTKB2();
        testQuery("(A(?x)) U (B(?y))", new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testSimpleQuery4()
    {
        simpleTKB2();
        // TODO optimization: recognize that CQs in TCQ exist that are the same and re-use the proposition for abstraction
        testQuery("((A(?x)) U (B(?y))) & (A(?x))", new ATermAppl[][] { { _a, _b } });
        testQuery("X(A(?x)) U (B(?y)) & (A(?z))", new ATermAppl[][] { { _a, _b, _a } });
    }

    @Test
    public void testSimpleQuery5()
    {
        simpleTKB2();
        testQuery("(F(A(?x)) & G (r(?y,?z)))", new ATermAppl[][] { { _a, _a, _b } });
        testQuery("F(A(?x)) | G (r(?y,?z))", new ATermAppl[][] { { _a, _a, _b }, { _b, _a, _b }, { _c, _a, _b }, { _a, _a, _a }, { _a, _a, _c }, { _a, _b, _a }, { _a, _b, _b }, { _a, _b, _c }, { _a, _c, _a }, { _a, _c, _b }, { _a, _c, _c } });
    }

    @Test
    public void testSimpleQuery6()
    {
        simpleTKB();
        testQuery("!G(A(?x))");
    }

    @Test
    public void testSimpleQuery7()
    {
        simpleTKB();
        testQuery("F(A(?x))",  new ATermAppl[][] { { _a } });
    }

    @Test
    public void testStrongAndWeakNextOverWordLength()
    {
        fillSimpleTKB(2);
        _tkb.get(1).addType(_a, _A);
        testQuery("(A(?x))");
        testQuery("X[!](A(?x))",  new ATermAppl[][] { { _a } });
        testQuery("X[!]X[!](A(a))");
        testQuery("X(A(?x))",  new ATermAppl[][] { { _a } });
        testQuery("XX(A(?x))",  new ATermAppl[][] { { _a }, { _b }, { _c } });
    }

    @Test
    public void testEmptyKBAndEmptyQuery()
    {
        fillSimpleTKB(2);
        testQuery("(A(?x))");
        testQuery("", new ATermAppl[][] { {} });
    }

    @Test
    public void testNotEntailedAndNegationNotEntailedQuery()
    {
        fillSimpleTKB(2);
        final String query = "X[!](A(?x))";
        testQuery(query);
        testQuery("!" + query);
    }

    @Test
    public void testNonTrivialSemanticsOfEdgeConstraints()
    {
        complexTKB();
        // a query engine with a naive implementation of semantics will return no binding.
        testQuery("!G(C(?x) ^ r(?x,?y))", allResults(List.of(_a, _b, _c), 2));
        // this is due to the axiom "C subclass of not(some(r, TOP))". B does not have such a constraint, therefore:
        testQuery("!G(B(?x) ^ r(?x,?y))");
    }

    @Test
    public void testInferenceRequiredForTCQEntailment()
    {
        complexTKB();
        testQuery("!(D(?x))", new ATermAppl[][] { { _a } } );
        testQuery("G(D(?x))", new ATermAppl[][] { { _b } } );
        testQuery("G(!(D(?x)) -> X(D(?y)))", new ATermAppl[][] { { _a, _b }, { _b, _a }, { _b, _b }, { _b, _c },
                { _c, _b } });
        testQuery("G((D(?x)) -> X(D(?y)))", new ATermAppl[][] { { _a, _a }, { _a, _b }, { _a, _c }, { _b, _b },
                { _c, _a }, { _c, _b }, { _c, _c }  });
        testQuery("G((A(?x)) -> X(r(?x,?y)))", new ATermAppl[][] { { _b, _a }, { _b, _b },  { _b, _c } });
        testQuery("G(!(D(?x)) -> X(p(?y,z)))", new ATermAppl[][] { { _b, _a }, { _b, _b }, { _b, _c },
                { _a, _c }, { _c, _c }  });
        testQuery("G(!(D(?x)) U (E(?y)))", new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testMetricsOperators()
    {
        complexTKB();
        testQuery("G_[0,9] (!(D(?x)) | (E(?y)))", new ATermAppl[][] { { _a, _b } });
        testQuery("G_[0,10] (!(D(?x)) | (E(?y)))");
        testQuery("F_<=3 !(C(?x) ^ r(?x,?y))", allResults(List.of(_a, _b, _c), 2));
        testQuery("(!(D(?w)) | (E(?x))) U_[0,9] (q(?y, ?z))", new ATermAppl[][] { { _a, _b, _c, _b },
                { _a, _b, _b, _a } });
        testQuery("(!(D(?w)) | (E(?x))) U_[0,8] (q(?y, ?z))", new ATermAppl[][] { { _a, _b, _b, _a } });
        testQuery("(!(D(?w)) | (E(?x))) U_[0,7] (q(?y, ?z))");
    }

    // TODO a lot more testing... Idea: have every single of the 7 benchmark queries represented here
}
