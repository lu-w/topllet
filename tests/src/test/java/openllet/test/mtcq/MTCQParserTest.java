package openllet.test.mtcq;

import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.mtcq.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static openllet.core.utils.TermFactory.var;
import static openllet.test.mtcq.QueryUtilities.*;
import static org.junit.Assert.assertThrows;

public class MTCQParserTest extends AbstractMTCQTest
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
    public void testSimpleMTCQ()
    {
        MetricTemporalConjunctiveQuery q = temporalQuery("G(C(a) & C(b))");
        testMTCQ(q, 1, "!(G (a))");
        testCQ(q.getQueries().get(0), atoms(_a, _C), atoms(_b, _C));
    }

    @Test
    public void testRepeatedCQ1()
    {
        MetricTemporalConjunctiveQuery q = temporalQuery("G(C(a)) & X(C(a))");
        testMTCQ(q, 1, "!((G (a)) & (X (a)))");
        testCQ(q.getQueries().get(0), atoms(_a, _C));
    }

    @Test
    public void testRepeatedCQ2()
    {
        MetricTemporalConjunctiveQuery q = temporalQuery("G(C(?x)) & X(C(x))");
        testMTCQ(q, 2, "!((G (a)) & (X (a)))");
        testCQ(q.getQueries().get(0), atoms(x, _C));
        testCQ(q.getQueries().get(1), atoms(x, _C));
        testVars(q, Set.of(), Set.of(x));
    }

    @Test
    public void testRepeatedSubCQ()
    {
        MetricTemporalConjunctiveQuery q = temporalQuery("G(C(a)) & X(C(a) & D(b))");
        testMTCQ(q, 2, "!((G (a)) & (X (b)))");
        testCQ(q.getQueries().get(0), atoms(_a, _C));
        testCQ(q.getQueries().get(1), atoms(_a, _C), atoms(_b, _D));
    }

    @Test
    public void testMTCQ1()
    {
        MetricTemporalConjunctiveQuery q = temporalQuery("G(C(a) & C(b)) | X(r(a,b)) -> F(D(b) & r(a,b))");
        testMTCQ(q, 3, "!((G (a)) | ((X (b)) -> (F (c))))");
        testCQ(q.getQueries().get(0), atoms(_a, _C), atoms(_b, _C));
        testCQ(q.getQueries().get(1), atoms(_a, _r, _b));
        testCQ(q.getQueries().get(2), atoms(_b, _D), atoms(_a, _r, _b));
    }

    @Test
    public void testMTCQ2()
    {
        MetricTemporalConjunctiveQuery q = temporalQuery("!G((C(a) & C(x))) U (r(x,?y)) & (C(?z))");
        testMTCQ(q, 3, "!(((!(G (a))) U (b)) & (c))");
        testCQ(q.getQueries().get(0), atoms(_a, _C), atoms(x, _C));
        testCQ(q.getQueries().get(1), atoms(x, _r, y));
        testCQ(q.getQueries().get(2), atoms(z, _C));
        testVars(q, Set.of(x), Set.of(y, z));
    }

    @Test
    public void testMTCQ3()
    {
        MetricTemporalConjunctiveQuery q = temporalQuery("!G_[0,2] ((C(a) & C(x))) U_<=2 (r(x,?y)) & (C(?z))");
        testMTCQ(q, 3, "!(((!(G_[0,2](a))) U_[0,2] (b)) & (c))");
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
    public void testPrefixesAndComments1()
    {
        String formula = """
        # comment
        PREFIX pref: <http://pref#># c
        PREFIX pref1: <http://pref1#>
        
        #comment2#asd
        #
        !G_[0,2] ((C(pref:a) & C(pref1:x))) U_<=2 (r(pref1:x,?y)) & (C(?z)) # inline comment""";
        MetricTemporalConjunctiveQuery q = temporalQuery(formula);
        testMTCQ(q, 3, "!(((!(G_[0,2](a))) U_[0,2] (b)) & (c))");
        testCQ(q.getQueries().get(0), atoms(var("http://pref#a"), _C), atoms(var("http://pref1#x"), _C));
        testCQ(q.getQueries().get(1), atoms(var("http://pref1#x"), _r, y));
        testCQ(q.getQueries().get(2), atoms(z, _C));
    }

    @Test
    public void testPrefixesAndComments2()
    {
        String formula = """
        # Illegitimate use of pedestrian crossing by bicyclist
        
        PREFIX l1c: <http://purl.org/auto/l1_core#>
        PREFIX l1d: <http://purl.org/auto/l1_de#>
        PREFIX l4c: <http://purl.org/auto/l4_core#>
        PREFIX l4d: <http://purl.org/auto/l4_de#>
        PREFIX phy: <http://purl.org/auto/physics#>
        PREFIX geo: <http://www.opengis.net/ont/geosparql#>
        
        G (A(?x) & B(?y) & C(?z) & C(?x) & A(?y) & r(?x,?l))
            &
        (r(?x,?w))
            U_<=20 # we exclude driving on the walkway and switching to road for a second as a valid behavior
        (G_<=5 (p(?x,?y) & r(?t,?l) & r(?t,?x))) # illegitimately taking right of way has to be sustained for some time to be significant
        """;
        MetricTemporalConjunctiveQuery q = temporalQuery(formula);
        testMTCQ(q, 3, "!((G (a)) & ((b) U_[0,20] (G_[0,5](c))))");
    }

    @Test
    public void testDuplicateConjunctiveQuery()
    {
        MetricTemporalConjunctiveQuery q = temporalQuery("((A(?x)) U (B(?y))) & (A(?x))");
        testMTCQ(q, 2, "!(((a) U (b)) & (a))");
    }
}
