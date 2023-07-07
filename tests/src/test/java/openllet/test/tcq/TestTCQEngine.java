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
        testQuery("G(A(?x))", new ATermAppl[][] { { _a } }); // works
        testQuery("G(A(?x) ^ B(?y)) & F(r(?x,?y))", new ATermAppl[][] { { _a, _b } }); // works
        testQuery("G((A(?x)) & (B(?y))) & F(r(?x,?y))", new ATermAppl[][] { { _a, _b } }); // works
        testQuery("G(A(?x) ^ B(?y)) & F!(r(?x,?y))"); // works
        testQuery("G(A(?x) ^ B(?y)) & G!(r(?x,?y))"); // works
        testQuery("G((A(?x)) & (B(?y))) & G!(r(?x,?y))"); // works
        testQuery("G(A(?x)) & G(r(?x,?y))", new ATermAppl[][] { { _a, _b } }); // works
        testQuery("G(A(?x)) & F(p(?x,?y))"); // works
        testQuery("G(A(?x)) & F(r(?x,?y))", new ATermAppl[][] { { _a, _b } }); // works
    }

    @Test
    public void testSimpleQuery2()
    {
        simpleTKB();
        // TODO check if we can optimize: if we check an edge (in full semantics) in a state N then we can add N's sat+unsat bindings to exclude
        //testQuery("(A(?x)) U (B(?y))", new ATermAppl[][] { { _a, _b }, { _b, _b }, { _c, _b } }); // works
        testQuery("(A(?x)) U (B(?y)) | F(C(?y))", new ATermAppl[][] { { _a, _b }, { _b, _b }, { _c, _b } }); // works
        //testQuery("((A(?x)) U (B(?y))) | X(C(?y))", new ATermAppl[][] { { _a, _b }, { _b, _b }, { _c, _b } }); // works
    }

    @Test
    public void testSimpleQuery3()
    {
        simpleTKB2();
        testQuery("(A(?x)) U (B(?y))", new ATermAppl[][] { { _a, _b } }); // works
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
}
