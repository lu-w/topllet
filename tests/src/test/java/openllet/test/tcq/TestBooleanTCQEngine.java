package openllet.test.tcq;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class TestBooleanTCQEngine extends AbstractTCQTest
{
    @Test
    public void testSimpleQuery1()
    {
        simpleTKB();
        assertQueryEntailed("F(A(a))");
    }

    @Test
    public void testSimpleQuery2()
    {
        simpleTKB();
        assertQueryNotEntailed("!G(A(a))");
    }

    @Test
    public void testEmptyKBAndEmptyQuery()
    {
        fillSimpleTKB(2);
        assertQueryNotEntailed("(A(a))");
        assertQueryEntailed("");
    }

    @Test
    public void testStrongAndWeakNextOverWordLength()
    {
        fillSimpleTKB(2);
        _tkb.get(1).addType(_a, _A);
        assertQueryNotEntailed("(A(a))");
        assertQueryEntailed("X[!](A(a))");
        assertQueryNotEntailed("X[!]X[!](A(a))");
        assertQueryEntailed("X(A(a))");
        assertQueryEntailed("XX(A(a))");
    }

    @Test
    public void testNotEntailedAndNegationNotEntailedAndQuery()
    {
        fillSimpleTKB(2);
        final String query = "X[!](A(a))";
        assertQueryNotEntailed(query);
        assertQueryNotEntailed("!" + query);
    }

    @Test
    public void testNonTrivialSemanticsOfEdgeConstraints()
    {
        complexTKB();
        // a query engine with a naive implementation of semantics will return "not entailed"
        // however, nontrivial semantics can only arise in case of having to check negation of queries of size > 1
        assertQueryEntailed("!G(C(a) ^ r(a,b))");
        // this is due to the axiom "C subclass of not(some(r, TOP))". B does not have such a constraint, therefore:
        assertQueryNotEntailed("!G(B(a) ^ r(a,b))");
    }

    @Test
    public void testInferenceRequiredForTCQEntailment()
    {
        complexTKB();
        assertQueryEntailed("G(!(D(a)) -> X(D(b)))");
        assertQueryEntailed("G((D(a)) -> X(D(b)))");
        assertQueryNotEntailed("G((A(a)) -> X(r(a,b)))");
        assertQueryEntailed("G(!(D(a)) -> X(p(c,x)))");
        assertQueryNotEntailed("G(!(D(a)) -> X(p(b,x)))");
        assertQueryEntailed("G(!(D(a)) U (E(b)))");
    }

    @Test
    public void testMetricsOperators()
    {
        complexTKB();
        assertQueryEntailed("G_[0,9] (!(D(a)) | (E(b)))");
        assertQueryNotEntailed("G_[0,10] (!(D(a)) | (E(b)))");
        assertQueryEntailed("F_<=3 !(C(a) ^ r(a,b))");
        assertQueryEntailed("(!(D(a)) | (E(b))) U_[0,9] (q(c, b))");
        assertQueryNotEntailed("(!(D(a)) | (E(b))) U_[0,7] (q(c, b))");
    }

    @Test
    public void testUndistVars()
    {
        complexTKB();
        assertQueryEntailed("F((r(x,y)))");
        assertQueryEntailed("F((p(x,y)))");
        assertQueryEntailed("G(!(D(x)) -> X(E(y)))");
        // undistinguished variables are assumed to be 'local' to their CQs and can thus be instantiated differently
        assertQueryEntailed("G(!(D(x)) -> X(E(x)))");
        assertQueryEntailed("F(p(c,x))");
        assertQueryEntailed("F(q(c,x))");
        assertQueryNotEntailed("G(q(c,x))");
    }

    @Test
    public void testCyclesInUndistVars()
    {
        complexTKB();
        assertThrows(UnsupportedOperationException.class, () -> testQuery("F(r(x,y) ^ p(y,z) ^ q(z,x))", false));
        assertQueryEntailed("F(r(x,y) ^ q(y,x))");
        assertQueryEntailed("F(r(x,y)) & F(p(y,x)) & F(q(z,x))");
    }

    @Test
    public void testRoleOrClassNotInKB()
    {
        simpleTKB();
        assertThrows(AssertionError.class, () -> testQuery("F((r(x,y) ^ p(x,z) ^ q(z,x))", false));
        assertThrows(AssertionError.class, () -> testQuery("F((F(x) ^ p(x,z))", false));
    }
}
