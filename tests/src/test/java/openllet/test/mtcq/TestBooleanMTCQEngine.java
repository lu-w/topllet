package openllet.test.mtcq;

import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import org.junit.Test;

import static openllet.core.utils.TermFactory.not;
import static openllet.core.utils.TermFactory.or;
import static org.junit.Assert.assertThrows;

public class TestBooleanMTCQEngine extends AbstractMTCQTest
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
    public void testDisjunction()
    {
        complexTKB();
        assertQueryEntailed("((B(a)) | (C(a)))");
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
        assertQueryEntailed("X(X(A(a)))");
    }

    @Test
    public void testNotEntailedAndNegationNotEntailedQuery()
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
        assertQueryEntailed("!G(C(a) & r(a,b))");
        // this is due to the axiom "C subclass of not(some(r, TOP))". B does not have such a constraint, therefore:
        assertQueryNotEntailed("!G(B(a) & r(a,b))");
    }

    @Test
    public void testInferenceRequiredForMTCQEntailment()
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
        assertQueryEntailed("G_[0,10] (!(D(a)) | (E(b)))");
        assertQueryEntailed("F_<=3 !(C(a) & r(a,b))");
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
        assertThrows(UnsupportedOperationException.class, () -> testQuery("F(r(x,y) & p(y,z) & q(z,x))", false));
        assertThrows(UnsupportedOperationException.class, () -> testQuery("F(r(x,y) & q(y,x))", false));
        assertThrows(AssertionError.class, () -> testQuery("F(r(x,y)) & F(p(y,x)) & F(q(z,x))", false));
        // TODO specify an example where there is a cycle in the MTCQ but not in the checked BCQs / UCQs
    }

    @Test
    public void testRoleOrClassNotInKB()
    {
        simpleTKB();
        assertThrows(AssertionError.class, () -> testQuery("F((r(x,y) & p(x,z) & q(z,x))", false));
        assertThrows(AssertionError.class, () -> testQuery("F((F(x) & p(x,z))", false));
    }
}
