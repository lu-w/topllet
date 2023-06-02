package openllet.test.tcq;

import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.tcq.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static openllet.core.utils.TermFactory.term;
import static openllet.core.utils.TermFactory.var;
import static openllet.test.tcq.QueryUtilities.*;
import static org.junit.Assert.assertThrows;

public class TCQParserTest extends AbstractTCQTest
{
    @Before
    public void simpleTKB()
    {
        fillTKB(3);
    }

    protected void fillTKB(int size)
    {
        timeSteps(size);
        classes(_A, _B, _C, _D);
        individuals(_a, _b, _c);
        objectProperties(_r, _p);
    }

    @Test
    public void testSimpleTCQ()
    {
        TemporalConjunctiveQuery q = temporalQuery("G(C(a) ^ C(b))");
        testTCQ(q, 1, "!(G(a))");
        testCQ(q.getQueries().get(0), atoms(_a, _C), atoms(_b, _C));
    }

    @Test
    public void testRepeatedCQ1()
    {
        TemporalConjunctiveQuery q = temporalQuery("G(C(a)) & X(C(a))");
        testTCQ(q, 2, "!(G(a) & X(a))");
        testCQ(q.getQueries().get(0), atoms(_a, _C));
        testCQ(q.getQueries().get(1), atoms(_a, _C));
    }

    @Test
    public void testRepeatedCQ2()
    {
        TemporalConjunctiveQuery q = temporalQuery("G(C(?x)) & X(C(x))");
        testTCQ(q, 2, "!(G(a) & X(b))");
        testCQ(q.getQueries().get(0), atoms(x, _C));
        testCQ(q.getQueries().get(1), atoms(x, _C));
        testVars(q, Set.of(), Set.of(x));
    }

    @Test
    public void testRepeatedSubCQ()
    {
        TemporalConjunctiveQuery q = temporalQuery("G(C(a)) & X(C(a) ^ D(b))");
        testTCQ(q, 2, "!(G(a) & X(b))");
        testCQ(q.getQueries().get(0), atoms(_a, _C));
        testCQ(q.getQueries().get(1), atoms(_a, _C), atoms(_b, _D));
    }

    @Test
    public void testTCQ1()
    {
        TemporalConjunctiveQuery q = temporalQuery("G(C(a) ^ C(b)) | X(r(a,b)) -> F(D(b) ^ r(a,b))");
        testTCQ(q, 3, "!(G(a) | X(b) -> F(c))");
        testCQ(q.getQueries().get(0), atoms(_a, _C), atoms(_b, _C));
        testCQ(q.getQueries().get(1), atoms(_a, _r, _b));
        testCQ(q.getQueries().get(2), atoms(_b, _D), atoms(_a, _r, _b));
    }

    @Test
    public void testTCQ2()
    {
        TemporalConjunctiveQuery q = temporalQuery("!G((C(a) ^ C(x))) U (r(x,?y)) & (C(?z))");
        testTCQ(q, 3, "!(!G((a)) U (b) & (c))");
        testCQ(q.getQueries().get(0), atoms(_a, _C), atoms(x, _C));
        testCQ(q.getQueries().get(1), atoms(x, _r, y));
        testCQ(q.getQueries().get(2), atoms(z, _C));
        testVars(q, Set.of(x), Set.of(y, z));
    }

    @Test
    public void testTCQ3()
    {
        TemporalConjunctiveQuery q = temporalQuery("!G_[0,2] ((C(a) ^ C(x))) U_<=2 (r(x,?y)) & (C(?z))");
        testTCQ(q, 3, "!(!G_[0,2] ((a)) U_<=2 (b) & (c))");
        testCQ(q.getQueries().get(0), atoms(_a, _C), atoms(x, _C));
        testCQ(q.getQueries().get(1), atoms(x, _r, y));
        testCQ(q.getQueries().get(2), atoms(z, _C));
        testVars(q, Set.of(x), Set.of(y, z));
    }

    @Test
    public void testIllegalMetricsToken()
    {
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("G_>=2 (C(a))"));
    }

    @Test
    public void testPrefixesAndComments()
    {
        String formula = """
# comment
PREFIX pref: <http://pref#>#c
PREFIX pref1: <http://pref1#>

#comment2#asd
#
!G_[0,2] ((C(pref:a) ^ C(pref1:x))) U_<=2 (r(pref1:x,?y)) & (C(?z)) # test""";
        TemporalConjunctiveQuery q = temporalQuery(formula);
        testTCQ(q, 3, "!(!G_[0,2] ((a)) U_<=2 (b) & (c))");
        testCQ(q.getQueries().get(0), atoms(var("http://pref#a"), _C), atoms(var("http://pref1#x"), _C));
        testCQ(q.getQueries().get(1), atoms(var("http://pref1#x"), _r, y));
        testCQ(q.getQueries().get(2), atoms(z, _C));
    }
}
