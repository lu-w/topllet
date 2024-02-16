package openllet.test.mtcq;

import openllet.mtcq.engine.rewriting.DNFTransformer;
import openllet.mtcq.model.query.MTCQFormula;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MTCQTransformerTest extends AbstractMTCQTest
{
    @Test
    public void testNegation()
    {
        simpleTKB();
        MTCQFormula q = temporalQuery("!X[!](A(a) | ((B(b)) & (C(c))))");
        MTCQFormula tq = DNFTransformer.transform(q);
        assertEquals("(last | X[!] ((!((A(a))) & (!((B(b))) | !((C(c)))))))", tq.toString());
    }

    @Test
    public void test()
    {
        simpleTKB();
        //MTCQFormula q = temporalQuery("(A(a)) U (X[!] (G (B(a))) | X[!] (C(a))) | (D(a))");
        MTCQFormula q = temporalQuery("(A(a)) U (B(a))");
        MTCQFormula tq = DNFTransformer.transform(q);
        System.out.println(tq);
    }
}
